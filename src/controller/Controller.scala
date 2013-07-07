package org.nlogo.extensions.gogo.controller

import jssc. {SerialPort, SerialPortEventListener}

import Constants._

import org.nlogo.api.ExtensionException

class Controller(override protected val portName: String)
  extends HasPortsAndStreams
  with PortCloser
  with PortOpener
  with Waiter
  with OutputPortController
  with CommandWriter
  with SensorReader
  with BurstReaderManager
  with SerialPortEventListener {

  def currentPortName = portOpt map (_.getPortName) getOrElse "INVALID"

  def currentPort: Option[SerialPort] = portOpt

  //@ c@ per gogo board protocol doc, ping returns an ack PLUS 3 version bytes.  however, gogo models show lots of retries, 
  // and my own testing shows frequent message fragmentation that falsely suggests failed pings. i have removed the validation
  // of acks as a result. CEB 7/3/13
  def ping():           Boolean = portOpt map { _ => writeAndWait(CmdPing) } getOrElse false
  def beep():           Boolean = portOpt map { _ => writeAndWait(CmdBeep, 0x00.toByte) } getOrElse false
  def led(on: Boolean): Boolean = portOpt map { _ => writeAndWait(if (on) CmdLedOn else CmdLedOff, 0x00.toByte) } getOrElse false

  //@ c@ see above on ping.  acks for motor ports seem to fail much less often (smaller sample size in my tests, though,
  // but the extension actually ignores failures here.  so removing ack validation for these 3 commands
  // changes nothing functionally. CEB 7/7/13
  def talkToOutputPorts(outputPortMask: Int) {
    writeAndWait(CmdTalkToOutputPort, outputPortMask.toByte)
  }

  def setOutputPortPower(level: Int) {
    if ((level < 0) || (level > 7)) throw new ExtensionException("Power level out of range: " + level)
    writeAndWait((CmdOutputPortPower | level << 2).toByte)
  }

  def setServoPosition(value: Int) {
    if ((value < 20) || (value > 40)) throw new ExtensionException("Requested servo position (%s) is out of safe range (20-40)".format(value))
    writeAndWait(CmdPwmServo, value.toByte)
  }


}
