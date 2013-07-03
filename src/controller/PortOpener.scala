package org.nlogo.extensions.gogo.controller

import jssc.{SerialPortException, SerialPort}
import org.nlogo.api.ExtensionException

private[controller] trait PortOpener {

  self: HasPortsAndStreams =>

  def openPort() {
    portOpt = portOpt orElse {
      val port = new SerialPort(portName)
      try {
        port.openPort()
        port.setParams( SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE )
        //@ c@ necessary?
        port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR)
        //@ c@ seems to improve reliability
        port.setEventsMask(SerialPort.MASK_RXCHAR)
      }
      catch { case spe: SerialPortException => throw new ExtensionException("Unable to open port " + portName, spe) }
      Option(port)
    }
  }

}
