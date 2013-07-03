package org.nlogo.extensions.gogo.controller

import java.io.IOException

import Constants.{ CmdReadExtendedSensor, CmdReadSensor, SensorReadNormal }

import org.nlogo.api.ExtensionException

trait SensorReader {

  self: CommandWriter with Reader =>

  def readSensor(sensor: Int): Int = readSensorHelper(sensor, SensorReadNormal)

  private def readSensorHelper(sensor: Int, mode: Int): Int = {

    if (sensor < 1)
      throw new ExtensionException("Sensor number out of range: " + sensor)
    else {

      val arr = {
        if (sensor > 8) {
          val highByte = ((sensor - 9) >> 8).toByte
          val lowByte  = ((sensor - 9) & 0xFF).toByte
          Array(CmdReadExtendedSensor, highByte, lowByte)
        }
        else
          Array((CmdReadSensor | ((sensor - 1) << 2) | mode).toByte)
      }

      try {
        writeAndWaitForReplyHeader(arr: _*)
        (readInt() << 8) + readInt()
      }
      catch {
        case e: IOException =>
          e.printStackTrace()
          0
      }
    }

  }

}
