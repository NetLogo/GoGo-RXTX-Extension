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
  //@ c@ 'stale' values array; sensor results built out of analyzing history array
  val staleValues = Array.fill[Int](16)(-1)  //supporting 16 sensors.
  val MagicTimeoutSensorValue = -666


  protected val portListener = new Actor {
    var byteArrOpt: Option[Array[Byte]] = Option(Array[Byte]())
    var syncRespOpt: Option[Int] = None


    start()

    def dropFirstNEntries(n:Int) {
      byteArrOpt = byteArrOpt.map( arr => arr.drop(n) )
    }

    val sensorCommands = Array[Byte](CmdReadSensor, CmdReadExtendedSensor )

    def processBytesForSensorData(bytesIn: Array[Byte]) : Option[Int] = {
      var bytes = bytesIn
      var cutPoint = 0
      var retn: Option[Int] = None

      var beginOutMsg = bytes.indexOfSlice( OutHeaderSlice )
      var beginInMsg = bytes.indexOfSlice( InHeaderSlice )

      while ( beginOutMsg >= 0 && beginInMsg >= 0 && bytes.length > beginInMsg + InHeaderSlice.length) {

        while ( beginInMsg >= 0 && beginInMsg < beginOutMsg ) {
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
              val sensReading = ((inMsgBytes(0) << 8) + ((inMsgBytes(1) + 256) % 256))
              if ( sensReading >= 0 && sensReading < 1024 ) {
                //only write if sensor value is valid
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
            syncRespOpt = None  //clear the "there's news" variable
            reply(Response(reportOpt))
        }
      }
    }

  }

  def searchForAck : (Int) => (Option[Int]) = {
    b =>
      Option(Constants.AckByte)
  }

  protected def writeAndWait(bytes: Byte*): Boolean = {
    writeCommand(bytes.toArray)
    !getResponse(searchForAck).isEmpty  //always true now -- no more ack/ping validation
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
       //this sleep reduces message fragmentation on return bytes, making it much more likely
       //to return the current value in getResponse() rather than falling back to the last-valid value.  CEB 7/7/13
    }
    catch {
      case _:InterruptedException => System.err.println("thread sleep interrupted")
    }
    getResponse(lookForSensorValue).orElse{ Option(staleValues(sensorArrayIndex)) }
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
    val bytes = portOpt map (_.readBytes(event.getEventValue)) getOrElse (throw new ExtensionException("Lost Port Connection")) //@ c@
    portListener ! Event(bytes)
  }

  private  def setNewEventListener() {
    val port = portOpt.getOrElse(throw new ExtensionException("No port available"))
    port.addEventListener(this)
  }

}
