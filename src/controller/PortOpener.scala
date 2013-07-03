package org.nlogo.extensions.gogo.controller

import java.io.IOException

import gnu.io.{ CommPortIdentifier, PortInUseException, SerialPort, UnsupportedCommOperationException }

import org.nlogo.api.ExtensionException

private[controller] trait PortOpener {

  self: HasPortsAndStreams =>

  def openPort(): Boolean = portOpt map (_ => true) getOrElse generateOpenPort()

  private def generateOpenPort() : Boolean = {
    portIDOpt = findPortByName(portName)
    portIDOpt map setUpPort getOrElse (throw new ExtensionException("Cannot find port: " + portName))
  }

  private def findPortByName(portName: String) : Option[CommPortIdentifier] = {
    import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
    CommPortIdentifier.getPortIdentifiers.asScala collectFirst {
      case id: CommPortIdentifier if (id.getPortType == CommPortIdentifier.PORT_SERIAL && id.getName == portName) => id
    }
  }

  private def setUpPort(portID: CommPortIdentifier) : Boolean = {
    openSerialPort(portID)
    portOpt map {
      port =>
        setUpStreams(port)
        setUpPortParams(port)
        true
    } getOrElse false
  }

  private def openSerialPort(portID: CommPortIdentifier) {
    try {
      portID.open("GoGoController", 2000) match {
        case sp: SerialPort => portOpt = Option(sp)
      }
    }
    catch {
      case e: PortInUseException => throw new ExtensionException("Port is already in use: " + e)
      case e: RuntimeException   => throw new ExtensionException("Unable to open port: " + e)
    }
  }

  private def setUpStreams(port: SerialPort) {
    try {
      inputStreamOpt  = Option(port.getInputStream)
      outputStreamOpt = Option(port.getOutputStream)
    }
    catch {
      case e: IOException => e.printStackTrace()
    }
  }

  private def setUpPortParams(port: SerialPort) {
    import SerialPort._
    try port.setSerialPortParams(9600, DATABITS_8, STOPBITS_1, PARITY_NONE)
    catch {
      case e: UnsupportedCommOperationException => e.printStackTrace()
    }
  }

}
