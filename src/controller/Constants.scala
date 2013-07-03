package org.nlogo.extensions.gogo.controller

object Constants {

  // Bytes
  val IN_HEADER1               = 0x55.toByte
  val IN_HEADER2               = 0xFF.toByte
  val OUT_HEADER1              = 0x54.toByte
  val OUT_HEADER2              = 0xFE.toByte
  val ACK_BYTE                 = 0xAA.toByte
  val CMD_PING                 = 0x00.toByte
  val CMD_READ_SENSOR          = 0x20.toByte
  val CMD_READ_EXTENDED_SENSOR = 0xE0.toByte
  val CMD_OUTPUT_PORT_ON       = 0x40.toByte
  val CMD_OUTPUT_PORT_OFF      = 0x44.toByte
  val CMD_OUTPUT_PORT_RD       = 0x48.toByte
  val CMD_OUTPUT_PORT_THISWAY  = 0x4C.toByte
  val CMD_OUTPUT_PORT_THATWAY  = 0x50.toByte
  val CMD_OUTPUT_PORT_COAST    = 0x54.toByte
  val CMD_OUTPUT_PORT_POWER    = 0x60.toByte
  val CMD_TALK_TO_OUTPUT_PORT  = 0x80.toByte
  val CMD_SET_BURST_MODE       = 0xA0.toByte
  val CMD_PWM_SERVO            = 0xC8.toByte
  val CMD_LED_ON               = 0xC0.toByte
  val CMD_LED_OFF              = 0xC1.toByte
  val CMD_BEEP                 = 0xC4.toByte
  val SENSOR_READ_NORMAL       = 0x00.toByte
  val SENSOR_READ_MAX          = 0x01.toByte
  val SENSOR_READ_MIN          = 0x02.toByte
  val BURST_CHUNK_HEADER       = 0x0C.toByte

  // Ints
  val OUTPUT_PORT_A    = 0x01
  val OUTPUT_PORT_B    = 0x02
  val OUTPUT_PORT_C    = 0x04
  val OUTPUT_PORT_D    = 0x08
  val SENSOR_1         = 0x01
  val SENSOR_2         = 0x02
  val SENSOR_3         = 0x04
  val SENSOR_4         = 0x08
  val SENSOR_5         = 0x10
  val SENSOR_6         = 0x20
  val SENSOR_7         = 0x40
  val SENSOR_8         = 0x80
  val BURST_SPEED_HIGH = 0x00
  val BURST_SPEED_LOW  = 0x01

}
