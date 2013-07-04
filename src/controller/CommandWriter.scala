package org.nlogo.extensions.gogo.controller

import jssc.{SerialPortEvent, SerialPortEventListener, SerialPortException, SerialPort }
import org.nlogo.extensions.gogo.controller.Constants._
import org.nlogo.api.ExtensionException
import scala.Some

trait CommandWriter {

  self: Waiter with HasPortsAndStreams with SerialPortEventListener =>

  import scala.actors.Actor

  case class  Event(bytes: Array[Byte])
  case class  Response(bytes: Option[Array[Byte]])
  case object Request
  //case object Cleaner

  //@ c@ stale values array; alternative to returning -1
  val staleValues = Array.fill[Int](16)(-2)

  protected val portListener = new Actor {

    var byteArrOpt: Option[Array[Byte]] = None
    var counter: Int = 0

    start()

    //@ c@ added because a message can come in after the first 'harvest' of it.  this leads to stale bytes in the ArrOpt.
    def purgeMe() {
      byteArrOpt = None
      counter = 0
    }

    def hasWaitedSincePurge: Boolean = {
      System.err.println("+++++++++++CHECKING value of counter, which = " + counter)
      counter != 0
    }
    def deferReply() { counter = counter + 1 }

    override def act() {
      loop {
        receive {
          case Event(bytes) =>
            val priorMessageFrag = byteArrOpt.getOrElse(Array[Byte]())
            val accumulationArr = priorMessageFrag ++ bytes
            byteArrOpt = Option(accumulationArr)
          case Request =>
            val result = byteArrOpt
            //byteArrOpt = None
            //@ c@ PROBLEM --> it's possible (and it happens) for a late-arriving second serial event to come in at this point
            //this loads the actor with bytes pertinent to the PRIOR read (which would fail, lacking those important bytes)
            //We need 2 solutions to this . first, clearing old stuff before writing our serial request so we don't have initial garbage.
            //purgeMe added for this reason. CEB 7/3/13
            //Second, the reverse.  waiting till a packet completes, if possible.  for this reason, a 2-try solution is attempted
            //in the wait for replyheader below, and the clear of byteArrayOpt is deferred till the data is accepted..
            reply(Response(result))
          //case Cleaner =>
          //  purgeMe()
        }
      }
    }

  }

  protected def writeAndWait(bytes: Byte*): Boolean = {
    setNewEventListener()
    writeCommand(bytes.toArray)
    !getResponse {
      bytes =>
        if (bytes.contains(Constants.AckByte)) {
          portListener.purgeMe()
          Option(Constants.AckByte)
        }
        else {
          //@ c@ for debugging and protocol analysis. CEB 7/3/13
          println("Expected ACK, got:" + bytes.mkString("::"))
          portListener.purgeMe()
          None
        }
    }.isEmpty
  }

  protected def getResponse(f: (Array[Byte]) => (Option[Int])) : Option[Int] = {
    (portListener !? Request) match {
      case Response(None)        => getResponse(f)
      case Response(Some(bytes)) => f(bytes)
      case _                     => None
    }
  }

  def lookForSensorValue: (Array[Byte]) => (Option[Int]) = {
    bytes =>
      val output = bytes.dropWhile(_ != InHeader2).drop(1)
      if (output.length >= 2) {
        val reading = ((output(0) << 8) + ((output(1) + 256) % 256))
        println(output.mkString("::"))
        portListener.purgeMe()
        Option(reading)
      }
      else {
        System.err.println("====>ABOUT TO USE COUNTER MECHANISM where bytes were: " + output.mkString("::"))
        None
      }
  }

  protected def writeAndWaitForReplyHeader(sensorArrayIndex: Int, bytes: Byte*): Option[Int] = {
    setNewEventListener()
    writeCommand(bytes.toArray)
    val candidate = getResponse(lookForSensorValue)
    candidate.foreach( value => staleValues(sensorArrayIndex) = value )
    candidate.orElse{
      if (portListener.hasWaitedSincePurge) {
        System.err.println("=======>USING STALE VALUE:" + staleValues(sensorArrayIndex))
        Option(staleValues(sensorArrayIndex))
      }
      else {
        portListener.deferReply()
        System.err.println( "=================>re-called into getResponse" )
        getResponse(lookForSensorValue)
      }
    }
  }


  private def writeCommand(command: Array[Byte]) {
    val myPort = portOpt.getOrElse( throw new ExtensionException("Error in writing to " + portName))
    myPort synchronized {
      try {
        myPort.writeBytes(Array(OutHeader1,OutHeader2) ++ command)
      }
      catch {
        case e: SerialPortException  => e.printStackTrace()
      }
    }
  }


  override def serialEvent(event: SerialPortEvent) {
    val bytes = portOpt map (_.readBytes(event.getEventValue)) getOrElse (throw new ExtensionException("Boom")) //@ c@
    //@ c@ remove debugging --> but this is how to see that we sometimes get partial replies in multiple serial events.
    // the jssc branch code handles this by keeping around a "leftover" array and by looking for all possible messages always.
    // different logic is needed here (see purgeMe), given the difference in communications architecture. CEB 7/3/13
    println( "serial event: " + bytes.mkString("::") )
    portListener ! Event(bytes)
  }

  private def setNewEventListener() {
    val port = portOpt.getOrElse(throw new ExtensionException("No port available"))
    try {
      port.removeEventListener()
      //@ c@ These two lines are to clean the port from any data that has come in while there were no listeners attached. CEB 7/3/13
      portListener.purgeMe()
      port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR)
    }
    catch {
      case ex: SerialPortException =>
    }
    port.addEventListener(this)
  }

}
