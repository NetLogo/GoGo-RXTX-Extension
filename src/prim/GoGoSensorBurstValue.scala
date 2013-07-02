package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultReporter, Syntax }

class GoGoSensorBurstValue extends DefaultReporter {
  override def getSyntax = Syntax.reporterSyntax(Array(Syntax.NumberType), Syntax.NumberType)
  override def report(args: Array[Argument], context: Context) = {
    val sensor = args(0).getIntValue
    if (burstCycleHandler != null) {
      if (sensor > 0 && sensor < 9)
        Double.box(burstCycleHandler.sensorValue(sensor))
      else
        throw new EE("Sensor id %s is out of range, should be 1-8.".format(sensor))
    }
    else
      throw new EE("Burst Mode is not set, use set-burst-mode to turn on burst mode for specific sensors.")
  }
}
