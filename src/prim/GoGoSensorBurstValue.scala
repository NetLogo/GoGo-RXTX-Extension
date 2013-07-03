package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, Syntax },
    extensions.gogo.controller.{ Controller, ControllerManager }

class GoGoSensorBurstValue(manager: ControllerManager) extends ManagedReporter(manager) {
  override def getSyntax = Syntax.reporterSyntax(Array(Syntax.NumberType), Syntax.NumberType)
  override def managedReport(args: Array[Argument], context: Context, controller: Controller) =
    Double.box(controller.getBurstValue(args(0).getIntValue))
}
