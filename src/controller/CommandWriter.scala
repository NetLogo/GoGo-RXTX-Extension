package org.nlogo.extensions.gogo.controller

import jssc.{SerialPortEvent, SerialPortEventListener, SerialPortException }
import org.nlogo.extensions.gogo.controller.Constants._
import org.nlogo.api.ExtensionException
import scala.Some
import java.lang.System

trait CommandWriter {

  self: Waiter with HasPortsAndStreams with SerialPortEventListener =>

  import scala.actors.Actor

  val OutHeaderSlice: Array[Byte] = Array(OutHeader1, OutHeader2 )
  val InHeaderSlice:  Array[Byte] = Array(InHeader1,  InHeader2 )

  case class  Event(bytes: Array[Byte])
  case class  Response(value: Option[Int])
  case object Request


  var notListening = true
  //@ c@ stale values array; alternative to returning -1
  val staleValues = Array.fill[Int](16)(-1)  //supporting 16 sensors.
  val MagicTimeoutSensorValue = -666


  protected val portListener = new Actor {
    var byteArrOpt: Option[Array[Byte]] = Option(Array[Byte]())
    var syncRespOpt: Option[Int] = None
    var cachedAck = false


    start()

    def dropFirstNEntries(n:Int) {
      System.err.println("request to drop " + n + " entries.")
      if (byteArrOpt.isEmpty) { System.err.println("!!NOT YET INITIALIZED") }

      byteArrOpt.foreach(arr => System.err.println("Before dropping, history array=" + arr.mkString("==")) )
      byteArrOpt = byteArrOpt.map( arr => arr.drop(n) )
      byteArrOpt.foreach(arr => System.err.println("After drop, data history array=" + arr.mkString("__")) )
    }

    /*
    def takeAnAck() : Boolean = {
      if ( cachedAck ) {
        cachedAck = false
        true
      } else {
        false
      }
    }
    */

    val sensorCommands = Array[Byte](CmdReadSensor, CmdReadExtendedSensor )

    def processBytesForSensorData(bytesIn: Array[Byte]) : Option[Int] = {
      var bytes = bytesIn
      var cutPoint = 0
      var retn: Option[Int] = None

      var beginOutMsg = bytes.indexOfSlice( OutHeaderSlice )
      var beginInMsg = bytes.indexOfSlice( InHeaderSlice )

      while ( beginOutMsg >= 0 && beginInMsg >= 0 && bytes.length > beginInMsg + InHeaderSlice.length) {

        while ( beginInMsg >= 0 && beginInMsg < beginOutMsg ) {
          println( "NOTE: there was a hanging In Message")
          dropFirstNEntries(beginInMsg + InHeaderSlice.length)
          bytes = bytes.drop(beginInMsg + InHeaderSlice.length)

          beginOutMsg = bytes.indexOfSlice( OutHeaderSlice )
          beginInMsg = bytes.indexOfSlice( InHeaderSlice )
        }

        if ( beginOutMsg >= 0 && beginInMsg > beginOutMsg ) {
          val outMsgBytes = bytes.slice( beginOutMsg + OutHeaderSlice.length, beginInMsg )
          val inMsgBytes = bytes.slice( beginInMsg + InHeaderSlice.length, beginInMsg + InHeaderSlice.length + 4 ) //maxlen of in but sure not to get to a next in.

          val command = outMsgBytes(0)
          if ( !sensorCommands.contains(command) && inMsgBytes.contains(AckByte) ) {
            cutPoint = beginInMsg + InHeaderSlice.length + inMsgBytes.indexOf(AckByte) + 1
            retn = Option(AckByte)
          }
          else if ( command > 31 && command < 93) {  //supporting 16 sensors.
            //it's a sensor read command.
            val sensor = (command - 32)/4
            if ( inMsgBytes.length > 1 ) {

              //only write if we were told what sensor was coming
              val sensReading = ((inMsgBytes(0) << 8) + ((inMsgBytes(1) + 256) % 256))
              if ( sensReading >= 0 && sensReading < 1024 ) {
                staleValues(sensor) = sensReading
                cutPoint = beginInMsg + InHeaderSlice.length + 2
                retn = Option(sensReading)
              }
            } else {
              cutPoint = beginInMsg + InHeaderSlice.length
            }
          }
        }

        dropFirstNEntries(cutPoint)
        bytes = bytes.drop(cutPoint)
        beginOutMsg = bytes.indexOfSlice( OutHeaderSlice )
        beginInMsg = bytes.indexOfSlice( InHeaderSlice )
        cutPoint = 0

      }
      retn
    }

    /*
    def OLDprocessBytesForSensorData(bytes: Array[Byte]) : Option[Int] = {
      var sensor = -1
      var pointer = bytes.indexOfSlice( OutHeaderSlice )
      var cutPoint = 0
      var retn: Option[Int] = None
      if ( pointer >= 0 ) {
        System.err.println("FOUND OUTHEADER at pointer val " + pointer)
        var workingCopy = bytes.slice(pointer + OutHeaderSlice.length, bytes.length)
        System.err.println("My bytes are " + bytes.mkString(",") + "and my working copy is " + workingCopy.mkString(";") )
        if (workingCopy.size > 0) {
          val command = workingCopy(0)
          if ( command > 31 && command < 93) {  //supporting 16 sensors.
            //it's a sensor read command.
            sensor = (command - 32)/4
            System.err.println("Found sensor command - read sensor# " + (sensor + 1).toString )
          }
          pointer = bytes.indexOfSlice( InHeaderSlice, pointer + OutHeaderSlice.length)
          if ( pointer > 0) {
            System.err.println("FOUND INHEADER at pointer val " + pointer)
            workingCopy = bytes.slice(pointer + InHeaderSlice.length, bytes.length)
            System.err.println("My bytes are " + bytes.mkString(",") + "and my working copy is " + workingCopy.mkString(";") )
            if ( workingCopy.size > 0 ) {
              if ( workingCopy(0) == (Constants.AckByte) ) {
                val relAckLoc = workingCopy.indexOf(Constants.AckByte)
                cachedAck = true
                retn = Option(Constants.AckByte)
                System.err.println("cached an ack")
                cutPoint = pointer + InHeaderSlice.length + relAckLoc  + 1///cut the ack
              }
              else if ( workingCopy.size > 1 ) {
                if ( sensor != -1 ) {
                  //only write if we were told what sensor was coming
                  val sensReading = ((workingCopy(0) << 8) + ((workingCopy(1) + 256) % 256))
                  if ( sensReading >= 0 && sensReading < 1024 ) {
                    staleValues(sensor) = sensReading
                    System.err.println("Found valid sensor value: " + sensReading)
                    retn = Option(sensReading)
                  }
                }
                cutPoint = pointer + InHeaderSlice.length + 2 //but regardless, cut the inheader and the data just read
              }
            }
          }
        }
      }
      dropFirstNEntries(cutPoint)
      System.err.println("-------->Preprocessing work is about to return... " + retn.getOrElse("A NONE"))
      retn
    }
    */


    override def act() {
      loop {
        receive {
          case Event(bytes) =>
            val priorMessageFrag = byteArrOpt.getOrElse(Array[Byte]())
            val accumulationArr = priorMessageFrag ++ bytes
            byteArrOpt = Option(accumulationArr)
            val toReturn = processBytesForSensorData( accumulationArr ).getOrElse(MagicTimeoutSensorValue)
            syncRespOpt = Option(toReturn)
          case Request =>
            val reportOpt = syncRespOpt
            System.err.println("Actor is about to Respond with... " + reportOpt.getOrElse("A NONE"))
            syncRespOpt = None  //clear this "there's news" variable
            reply(Response(reportOpt))
        }
      }
    }

  }

  def searchForAck : (Int) => (Option[Int]) = {
    b =>
      if (b != Constants.AckByte) {
        System.err.println("NOTE: didn't get an ack right away")
      }
      Option(Constants.AckByte)
  }

  protected def writeAndWait(bytes: Byte*): Boolean = {
    writeCommand(bytes.toArray)
    !getResponse(searchForAck).isEmpty  //always true now -- keep for protocol behavior analysis
  }

  protected def getResponse(f: (Int) => (Option[Int])) : Option[Int] = {
    (portListener !? Request) match {
      case Response(None)        => getResponse(f)
      case Response(Some(i))     => f(i)
      case _                     => {System.err.println("GOT TO THE CATCHALL UNEXPECTEDLY")
                                    None}
    }
  }

  def lookForSensorValue: (Int) => (Option[Int]) = {
    i =>
      if ( i != MagicTimeoutSensorValue )
        Option(i)
      else
        None
  }

  protected def writeAndWaitForReplyHeader(sensorArrayIndex: Int, bytes: Byte*): Option[Int] = {
    writeCommand(bytes.toArray)
    try {
       Thread.sleep(10)
    }
    catch {
      case _:InterruptedException => System.out.println("thread sleep interrupted")
    }
    val retVal = getResponse(lookForSensorValue).orElse{
      System.err.println("Got a NONE back from getResponse()")
      Option(staleValues(sensorArrayIndex))
    }
    System.err.println("+!+!+!!+!+RETURNING TO NETLOGO: " + retVal.getOrElse("NONE"))
    retVal
  }


  private def writeCommand(command: Array[Byte]) {
    val myPort = portOpt.getOrElse( throw new ExtensionException("Error in writing to " + portName))
    if ( notListening ) {
      notListening = false
      setNewEventListener()
    }
    myPort synchronized {
      try {
        myPort.writeBytes(OutHeaderSlice ++ command)
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
    System.err.println( "serial event: " + bytes.mkString("::") )
    portListener ! Event(bytes)
  }

  private  def setNewEventListener() {
    val port = portOpt.getOrElse(throw new ExtensionException("No port available"))
    port.addEventListener(this)
  }

}
