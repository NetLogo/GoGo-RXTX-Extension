package org.nlogo.extensions.gogo.controller

object Constants {

  // Bytes
  val InHeader1             = 0x55.toByte
  val InHeader2             = 0xFF.toByte
  val OutHeader1            = 0x54.toByte
  val OutHeader2            = 0xFE.toByte
  val AckByte               = 0xAA.toByte
  val CmdPing               = 0x00.toByte
  val CmdReadSensor         = 0x20.toByte
  val CmdReadExtendedSensor = 0xE0.toByte
  val CmdOutputPortOn       = 0x40.toByte
  val CmdOutputPortOff      = 0x44.toByte
  val CmdOutputPortRd       = 0x48.toByte
  val CmdOutputPortThisWay  = 0x4C.toByte
  val CmdOutputPortThatWay  = 0x50.toByte
  val CmdOutputPortCoast    = 0x54.toByte
  val CmdOutputPortPower    = 0x60.toByte
  val CmdTalkToOutputPort   = 0x80.toByte
  val CmdSetBurstMode       = 0xA0.toByte
  val CmdPwmServo           = 0xC8.toByte
  val CmdLedOn              = 0xC0.toByte
  val CmdLedOff             = 0xC1.toByte
  val CmdBeep               = 0xC4.toByte
  val SensorReadNormal      = 0x00.toByte
  val SensorReadMax         = 0x01.toByte
  val SensorReadMin         = 0x02.toByte
  val BurstChunkHeader      = 0x0C.toByte

  // Ints
  val OutputPortA    = 0x01
  val OutputPortB    = 0x02
  val OutputPortC    = 0x04
  val OutputPortD    = 0x08
  val Sensor1        = 0x01
  val Sensor2        = 0x02
  val Sensor3        = 0x04
  val Sensor4        = 0x08
  val Sensor5        = 0x10
  val Sensor6        = 0x20
  val Sensor7        = 0x40
  val Sensor8        = 0x80
  val BurstSpeedHigh = 0x00
  val BurstSpeedLow  = 0x01

}
