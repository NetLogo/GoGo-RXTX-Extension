package org.nlogo.extensions.gogo.controller

import org.nlogo.api.ExtensionException

import Constants._

trait BurstReaderManager {

  self: CommandWriter with Reader with Waiter =>

  private var burstReaderOpt: Option[BurstReader] = None

  def startBurstReader(handler: BurstCycleHandler) {
    burstReaderOpt = {
      val reader = new BurstReader(handler, () => readBurstCycle())
      reader.start()
      Option(reader)
    }
  }

  def stopBurstReader() {
    burstReaderOpt foreach (_.stopReading())
    setBurstMode(0, BurstSpeedHigh)
    setBurstMode(0, BurstSpeedLow) // Umm.. WHAT?! --JAB (7/2/13)
  }

  def getBurstValue(sensorNum: Int): Int =
    if (sensorNum > 0 && sensorNum < 9)
      burstReaderOpt map (_.handler.getValue(sensorNum)) getOrElse (throw new ExtensionException("No burst reader to read from"))
    else
      throw new ExtensionException("Sensor id %s is out of range, should be 1-8.".format(sensorNum))

  def setBurstMode(sensorMask: Int, speed: Int = BurstSpeedHigh) {
    writeAndWait((CmdSetBurstMode | speed).toByte, sensorMask.toByte)
  }

  private def readBurstCycle(): Option[(Int, Int)] =
    if (waitForByte(BurstChunkHeader)) {

      val (high, low) = (readInt(), readInt())
      val sensor      = (high >> 5) + 1
      val value       = ((high & 0x03) << 8) + low

      if (sensor > 0)
        Some((sensor, value))
      else
        None

    }
    else
      None

}
