package org.nlogo.extensions.gogo.controller

import java.io.IOException

import Constants.{ CMD_READ_EXTENDED_SENSOR, CMD_READ_SENSOR, SENSOR_READ_NORMAL }

import org.nlogo.api.ExtensionException

trait SensorReader {

  self: CommandWriter =>

  def readSensor(sensor: Int): Int = readSensorHelper(sensor, SENSOR_READ_NORMAL)

  private def readSensorHelper(sensor: Int, mode: Int): Int = {

    if (sensor < 1)
      throw new ExtensionException("Sensor number out of range: " + sensor)
    else {

      val arr = {
        if (sensor > 8) {
          val highByte = ((sensor - 9) >> 8).toByte
          val lowByte  = ((sensor - 9) & 0xFF).toByte
          Array(CMD_READ_EXTENDED_SENSOR, highByte, lowByte)
        }
        else
          Array((CMD_READ_SENSOR | ((sensor - 1) << 2) | mode).toByte)
      }

      try {
        writeAndWaitForReplyHeader(arr: _*)
        (readInt << 8) + readInt
      }
      catch {
        case e: IOException =>
          e.printStackTrace()
          0
      }
    }

  }

}
