package org.nlogo.extensions.gogo.controller

trait BurstCycleHandler {
  def getValue(sensor: Int) : Int
  def handleBurstCycle(sensor: Int, value: Int)
}

