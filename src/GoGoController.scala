package org.nlogo.extensions.gogo

import org.nlogo.api.ExtensionException
import java.io.PushbackInputStream
import java.io.OutputStream
import java.io.IOException
import java.util.Enumeration
import java.util.List
import java.util.ArrayList
import gnu.io._

object GoGoController {
  def sensorID(sensor: Int): Int = {
    if ((sensor < 1) || (sensor > 8)) throw new RuntimeException("Sensor number out of range: " + sensor)
    return sensorIDs((sensor - 1))
  }

  def findPortByName(portName: String): Nothing = {
    val portList: Enumeration[_] = CommPortIdentifier.getPortIdentifiers
    var id: Nothing = null
    while (portList.hasMoreElements) {
      id = portList.nextElement.asInstanceOf[Nothing]
      if (id.getPortType eq CommPortIdentifier.PORT_SERIAL) {
        if (id.getName == portName) {
          return id
        }
      }
    }
    return null
  }

  def availablePorts: List[String] = {
    return listPorts(true)
  }

  def serialPorts: List[String] = {
    return listPorts(false)
  }

  def listPorts(onlyAvailable: Boolean): List[String] = {
    var portList: Enumeration[_] = null
    var portId: Nothing = null
    val portNames: List[String] = new ArrayList[String]
    portList = CommPortIdentifier.getPortIdentifiers
    while (portList.hasMoreElements) {
      portId = portList.nextElement.asInstanceOf[Nothing]
      if (portId.getPortType eq CommPortIdentifier.PORT_SERIAL && (!onlyAvailable || !portId.isCurrentlyOwned)) {
        portNames.add(portId.getName)
      }
    }
    return portNames
  }

  def main(args: Array[String]) {
    var port: String = null
    {
      var i: Int = 0
      while (i < args.length) {
        {
          if (args(i) == "-l") {
            import scala.collection.JavaConversions._
            for (portName <- serialPorts) {
              System.out.println(portName)
            }
            System.exit(0)
          }
          else if (args(i) == "-p") {
            i += 1
            port = args(i)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
  }

  final val IN_HEADER1: Byte = 0x55.asInstanceOf[Byte]
  final val IN_HEADER2: Byte = 0xFF.asInstanceOf[Byte]
  final val OUT_HEADER1: Byte = 0x54.asInstanceOf[Byte]
  final val OUT_HEADER2: Byte = 0xFE.asInstanceOf[Byte]
  final val ACK_BYTE: Byte = 0xAA.asInstanceOf[Byte]
  final val CMD_PING: Byte = 0x00.asInstanceOf[Byte]
  final val CMD_READ_SENSOR: Byte = 0x20.asInstanceOf[Byte]
  final val CMD_READ_EXTENDED_SENSOR: Byte = 0xE0.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_ON: Byte = 0x40.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_OFF: Byte = 0x44.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_RD: Byte = 0x48.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_THISWAY: Byte = 0x4C.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_THATWAY: Byte = 0x50.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_COAST: Byte = 0x54.asInstanceOf[Byte]
  final val CMD_OUTPUT_PORT_POWER: Byte = 0x60.asInstanceOf[Byte]
  final val CMD_TALK_TO_OUTPUT_PORT: Byte = 0x80.asInstanceOf[Byte]
  final val CMD_SET_BURST_MODE: Byte = 0xA0.asInstanceOf[Byte]
  final val CMD_PWM_SERVO: Byte = 0xC8.asInstanceOf[Byte]
  final val CMD_LED_ON: Byte = 0xC0.asInstanceOf[Byte]
  final val CMD_LED_OFF: Byte = 0xC1.asInstanceOf[Byte]
  final val CMD_BEEP: Byte = 0xC4.asInstanceOf[Byte]
  final val SENSOR_READ_NORMAL: Byte = 0x00.asInstanceOf[Byte]
  final val SENSOR_READ_MAX: Byte = 0x01.asInstanceOf[Byte]
  final val SENSOR_READ_MIN: Byte = 0x02.asInstanceOf[Byte]
  final val OUTPUT_PORT_A: Int = 0x01
  final val OUTPUT_PORT_B: Int = 0x02
  final val OUTPUT_PORT_C: Int = 0x04
  final val OUTPUT_PORT_D: Int = 0x08
  final val SENSOR_1: Int = 0x01
  final val SENSOR_2: Int = 0x02
  final val SENSOR_3: Int = 0x04
  final val SENSOR_4: Int = 0x08
  final val SENSOR_5: Int = 0x10
  final val SENSOR_6: Int = 0x20
  final val SENSOR_7: Int = 0x40
  final val SENSOR_8: Int = 0x80
  final val BURST_SPEED_HIGH: Int = 0x00
  final val BURST_SPEED_LOW: Int = 0x01
  final val BURST_CHUNK_HEADER: Byte = 0x0C.asInstanceOf[Byte]
  private final val sensorIDs: Array[Int] = Array(SENSOR_1, SENSOR_2, SENSOR_3, SENSOR_4, SENSOR_5, SENSOR_6, SENSOR_7, SENSOR_8)

  trait BurstCycleHandler {
    def handleBurstCycle(sensor: Int, value: Int)
  }

}

class GoGoController {
  def this(portName: String) {
    this()
    this.portName = portName
  }

  def currentPortName: String = {
    if (port != null) return port.getName
    return null
  }

  def currentPort: Nothing = {
    return port
  }

  def closePort {
    inputStream synchronized {
      outputStream synchronized {
        if (port != null) {
          port.removeEventListener
          if (inputStream != null) {
            try {
              inputStream.close
              inputStream = null
            }
            catch {
              case e: IOException => {
                e.printStackTrace
              }
            }
          }
          if (outputStream != null) {
            try {
              outputStream.close
              outputStream = null
            }
            catch {
              case e: IOException => {
                e.printStackTrace
              }
            }
          }
          port.close
          port = null
        }
      }
    }
  }

  def openPort: Boolean = {
    if (port != null) {
      return true
    }
    portId = findPortByName(portName)
    if (portId == null) {
      throw new RuntimeException("Cannot find port: " + portName)
    }
    try {
      port = portId.open("GoGoController", 2000).asInstanceOf[Nothing]
    }
    catch {
      case e: Nothing => {
        throw new RuntimeException("Port is already in use: " + e)
      }
      case e: RuntimeException => {
        throw new RuntimeException("Unable to open port: " + e)
      }
    }
    if (port != null) {
      try {
        inputStream = new PushbackInputStream(port.getInputStream)
        outputStream = port.getOutputStream
      }
      catch {
        case e: IOException => {
          e.printStackTrace
        }
      }
      try {
        port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
      }
      catch {
        case e: Nothing => {
          e.printStackTrace
        }
      }
      return true
    }
    else {
      return false
    }
  }

  def setReadTimeout(ms: Int) {
    try {
      inputStream synchronized {
        port.enableReceiveTimeout(ms)
        inputStream = new PushbackInputStream(port.getInputStream)
      }
    }
    catch {
      case e: Nothing => {
        e.printStackTrace
      }
      case e: IOException => {
        e.printStackTrace
      }
    }
  }

  def writeCommand(command: Array[Byte]) {
    outputStream synchronized {
      try {
        writeByte(OUT_HEADER1)
        writeByte(OUT_HEADER2)
        outputStream.write(command)
      }
      catch {
        case e: IOException => {
          e.printStackTrace
        }
      }
    }
  }

  def readByte: Byte = {
    var b: Int = 0
    inputStream synchronized {
      b = inputStream.read
    }
    return b.asInstanceOf[Byte]
  }

  def peekByte: Byte = {
    var b: Int = 0
    inputStream synchronized {
      b = inputStream.read
      inputStream.unread(b)
    }
    return b.asInstanceOf[Byte]
  }

  def readInt: Int = {
    var b: Int = 0
    inputStream synchronized {
      b = inputStream.read
    }
    return b
  }

  def writeByte(b: Byte) {
    outputStream synchronized {
      outputStream.write(b)
    }
  }

  def waitForReplyHeader: Boolean = {
    try {
      var b: Int = 0
      {
        var i: Int = 0
        while (i < 256) {
          {
            inputStream synchronized {
              b = readByte
              if (b == IN_HEADER1) {
                b = readByte
                if (b == IN_HEADER2) return true
              }
              else {
                if (b == IN_HEADER2) {
                  return true
                }
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
      }
    }
    return false
  }

  def waitForByte(target: Byte): Boolean = {
    try {
      var b: Int = 0
      {
        var i: Int = 0
        while (i < 256) {
          {
            inputStream synchronized {
              b = readByte
              if (b == target) {
                return true
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
      }
    }
    return false
  }

  def waitForAck: Boolean = {
    waitForReplyHeader
    return waitForByte(ACK_BYTE)
  }

  def waitForAck(msec: Int): Boolean = {
    waitForReplyHeader
    return waitForByte(ACK_BYTE)
  }

  def ping: Boolean = {
    if (port == null) {
      return false
    }
    writeCommand(Array[Byte](CMD_PING))
    return waitForAck
  }

  def beep: Boolean = {
    if (port == null) {
      return false
    }
    writeCommand(Array[Byte](CMD_BEEP, 0x00.asInstanceOf[Byte]))
    return waitForAck
  }

  def led(on: Boolean): Boolean = {
    if (port == null) {
      return false
    }
    var cmd: Byte = CMD_LED_OFF
    if (on) {
      cmd = CMD_LED_ON
    }
    writeCommand(Array[Byte](cmd, 0x00.asInstanceOf[Byte]))
    return waitForAck
  }

  def _readSensor(sensor: Int, mode: Int): Int = {
    var sensorVal: Int = 0
    if (sensor < 1) throw new RuntimeException("Sensor number out of range: " + sensor)
    if (sensor > 8) return readExtendedSensor(sensor)
    val b: Int = CMD_READ_SENSOR | ((sensor - 1) << 2) | mode
    try {
      writeCommand(Array[Byte](b.asInstanceOf[Byte]))
      inputStream synchronized {
        waitForReplyHeader
        sensorVal = readInt << 8
        sensorVal += readInt
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
      }
    }
    return sensorVal
  }

  def readSensor(sensor: Int): Int = {
    return _readSensor(sensor, SENSOR_READ_NORMAL)
  }

  def readSensorMin(sensor: Int): Int = {
    return _readSensor(sensor, SENSOR_READ_MIN)
  }

  def readSensorMax(sensor: Int): Int = {
    return _readSensor(sensor, SENSOR_READ_MAX)
  }

  def readExtendedSensor(sensor: Int): Int = {
    var sensorVal: Int = 0
    sensor = sensor - 9
    val highByte: Byte = (sensor >> 8).asInstanceOf[Byte]
    val lowByte: Byte = (sensor & 0xFF).asInstanceOf[Byte]
    val command: Array[Byte] = Array(CMD_READ_EXTENDED_SENSOR, highByte, lowByte)
    try {
      writeCommand(command)
      inputStream synchronized {
        waitForReplyHeader
        sensorVal = readInt << 8
        sensorVal += readInt
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
      }
    }
    return sensorVal
  }

  def talkToOutputPorts(outputPortMask: Int) {
    writeCommand(Array[Byte](CMD_TALK_TO_OUTPUT_PORT, outputPortMask.asInstanceOf[Byte]))
    waitForAck
  }

  def setBurstMode(sensorMask: Int) {
    setBurstMode(sensorMask, BURST_SPEED_HIGH)
  }

  def setBurstMode(sensorMask: Int, speed: Int) {
    writeCommand(Array[Byte](((CMD_SET_BURST_MODE | speed.asInstanceOf[Byte]).asInstanceOf[Byte]), sensorMask.asInstanceOf[Byte]))
    waitForAck
    burstModeMask = sensorMask
  }

  def startBurstReader(handler: GoGoController.BurstCycleHandler) {
    burstReader = new GoGoController#BurstReader(this, handler)
    burstReader.start
  }

  def stopBurstReader {
    burstReader.stopReading
  }

  def readBurstCycle: Array[Int] = {
    try {
      var b: Int = 0
      {
        var i: Int = 0
        while (i < 256) {
          {
            inputStream synchronized {
              b = peekByte
              if (b == BURST_CHUNK_HEADER) {
                readByte
                val high: Int = readInt
                val low: Int = readInt
                val sensor: Int = (high >> 5) + 1
                var `val`: Int = (high & 0x03) << 8
                `val` += low
                if (sensor > 0) {
                  return Array[Int](sensor, `val`)
                }
                else {
                  return Array[Int]
                }
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    catch {
      case e: IOException => {
        e.printStackTrace
        return Array[Int]
      }
    }
    return Array[Int]
  }

  def outputPortControl(cmd: Byte) {
    writeCommand(Array[Byte](cmd))
    waitForAck
  }

  def outputPortOn {
    outputPortControl(CMD_OUTPUT_PORT_ON)
  }

  def outputPortOff {
    outputPortControl(CMD_OUTPUT_PORT_OFF)
  }

  def outputPortCoast {
    outputPortControl(CMD_OUTPUT_PORT_COAST)
  }

  def outputPortThatWay {
    outputPortControl(CMD_OUTPUT_PORT_THATWAY)
  }

  def outputPortThisWay {
    outputPortControl(CMD_OUTPUT_PORT_THATWAY)
  }

  def outputPortReverse {
    outputPortControl(CMD_OUTPUT_PORT_RD)
  }

  def setOutputPortPower(level: Int) {
    if ((level < 0) || (level > 7)) throw new RuntimeException("Power level out of range: " + level)
    val comm: Int = CMD_OUTPUT_PORT_POWER | level << 2
    writeCommand(Array[Byte](comm.asInstanceOf[Byte]))
    waitForAck
  }

  def setServoPosition(`val`: Int) {
    if ((`val` < 20) || (`val` > 40)) throw new ExtensionException("Requested servo position (" + `val` + ") is out of safe range (20-40): ")
    writeCommand(Array[Byte](CMD_PWM_SERVO, `val`.asInstanceOf[Byte]))
    waitForAck
  }

  def serialEvent(event: Nothing) {
  }

  private[gogo] var driver: Nothing = null
  private[gogo] var portName: String = null
  private[gogo] var portId: Nothing = null
  private[gogo] var burstReader: GoGoController#BurstReader = null
  private[gogo] var port: Nothing = null
  var inputStream: PushbackInputStream = null
  var outputStream: OutputStream = null
  var burstModeMask: Int = 0

  class DefaultBurstCycleHandler extends BurstCycleHandler {
    def handleBurstCycle(sensor: Int, value: Int) {
      System.out.println("Sensor " + sensor + " value: " + value)
      sensorValues(sensor - 1) = value
    }

    private final val sensorValues: Array[Int] = new Array[Int](8)
  }

  class BurstReader extends Thread {
    private[gogo] def this(cont: GoGoController, handler: GoGoController.BurstCycleHandler) {
      this()
      this.controller = cont
      this.handler = handler
    }

    def stopReading {
      keepRunning = false
    }

    override def run {
      var result: Array[Int] = null
      while (keepRunning) {
        result = controller.readBurstCycle
        if (result.length == 2 && handler != null) {
          handler.handleBurstCycle(result(0), result(1))
        }
      }
    }

    private[gogo] var controller: GoGoController = null
    private[gogo] var handler: GoGoController.BurstCycleHandler = null
    private[gogo] var keepRunning: Boolean = true
  }

}
