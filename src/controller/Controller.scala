package org.nlogo.extensions.gogo.controller

import java.io.IOException

import gnu.io.{ SerialPort, UnsupportedCommOperationException }

import Constants._

import org.nlogo.api.ExtensionException

//@ Rename WindowsInstaller
//@ Rename primitives => name - "GoGo"
//@ Change colon style on methods (no space)
//@ Coreygogo?
//@ Title case the constants

class Controller(override protected val portName: String)
  extends HasPortsAndStreams
  with PortCloser
  with PortOpener
  with Reader
  with Waiter
  with OutputPortController
  with CommandWriter
  with SensorReader
  with BurstReaderManager {

  def currentPortName = portOpt map (_.getName) getOrElse "INVALID"

  def currentPort: Option[SerialPort] = portOpt

  def beep():           Boolean = portOpt map { _ => writeAndWait(CMD_BEEP, 0x00.toByte) } getOrElse false
  def ping():           Boolean = portOpt map { _ => writeAndWait(CMD_PING) } getOrElse false
  def led(on: Boolean): Boolean = portOpt map { _ => writeAndWait(if (on) CMD_LED_ON else CMD_LED_OFF, 0x00.toByte) } getOrElse false

  def talkToOutputPorts(outputPortMask: Int) {
    writeAndWait(CMD_TALK_TO_OUTPUT_PORT, outputPortMask.toByte)
  }

  def setOutputPortPower(level: Int) {
    if ((level < 0) || (level > 7)) throw new ExtensionException("Power level out of range: " + level)
    writeAndWait((CMD_OUTPUT_PORT_POWER | level << 2).toByte)
  }

  def setServoPosition(value: Int) {
    if ((value < 20) || (value > 40)) throw new ExtensionException("Requested servo position (%s) is out of safe range (20-40)".format(value))
    writeAndWait(CMD_PWM_SERVO, value.toByte)
  }

  def setReadTimeout(ms: Int) {
    try {
      inputStreamOpt foreach {
        _ synchronized {
          portOpt foreach {
            port =>
              port.enableReceiveTimeout(ms)
              inputStreamOpt = Option(port.getInputStream)
          }
        }
      }
    }
    catch {
      case e: UnsupportedCommOperationException => e.printStackTrace()
      case e: IOException                       => e.printStackTrace()
    }
  }

}
