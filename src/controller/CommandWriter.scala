package org.nlogo.extensions.gogo.controller

import jssc.{SerialPortEvent, SerialPortEventListener, SerialPortException }
import org.nlogo.extensions.gogo.controller.Constants._
import org.nlogo.api.ExtensionException
import scala.Some

trait CommandWriter {

  self: Waiter with HasPortsAndStreams with SerialPortEventListener =>

  import scala.actors.Actor

  case class  Event(bytes: Array[Byte])
  case class  Response(bytes: Option[Array[Byte]])
  case object Request

  protected val portListener = new Actor {

    var byteArrOpt: Option[Array[Byte]] = None

    start()

    override def act() {
      loop {
        receive {
          case Event(bytes) =>
            byteArrOpt = Option(bytes)
          case Request =>
            val result = byteArrOpt
            byteArrOpt = None
            reply(Response(result))
        }
      }
    }

  }

  protected def writeAndWait(bytes: Byte*): Boolean = {
    setNewEventListener()
    writeCommand(bytes.toArray)
    !getResponse {
      bytes =>
        if (bytes.contains(Constants.AckByte))
          Option(Constants.AckByte)
        else {
          println("Expected ACK, got:" + bytes.mkString("::"))
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

  protected def writeAndWaitForReplyHeader(bytes: Byte*): Option[Int] = {
    setNewEventListener()
    writeCommand(bytes.toArray)
    getResponse {
      bytes =>
        val output = bytes.dropWhile(_ != InHeader2).drop(1)
        if (output.length >= 2) {
          val reading = ((output(0) << 8) + ((output(1) + 256) % 256))
          println(output.mkString("::"))
          Option(reading)
        }
        else
          None
    }
  }

  private def writeCommand(command: Array[Byte]) {
    val myPort = portOpt.getOrElse( throw new ExtensionException("hi"))
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
    portListener ! Event(bytes)
  }

  private def setNewEventListener() {
    val port = portOpt.getOrElse(throw new ExtensionException("No port available"))
    try port.removeEventListener()
    catch {
      case ex: SerialPortException =>
    }
    port.addEventListener(this)  //put right mask
  }

}
