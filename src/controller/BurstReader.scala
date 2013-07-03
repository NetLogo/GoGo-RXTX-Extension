package org.nlogo.extensions.gogo.controller

class BurstReader(val handler: BurstCycleHandler, readBurstCycle: () => Option[(Int, Int)]) extends Thread {

  private var keepRunning: Boolean = true

  override def run() {
    while (keepRunning) {
      readBurstCycle() foreach {
        case (sensor, value) => handler.handleBurstCycle(sensor, value)
      }
    }
  }

  def stopReading() {
    keepRunning = false
  }

}

