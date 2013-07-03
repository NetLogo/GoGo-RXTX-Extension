package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, Syntax },
    extensions.gogo.controller.{ Controller, ControllerManager }

class Ping(manager: ControllerManager) extends ManagedReporter(manager) {
  override def getSyntax = Syntax.reporterSyntax(Syntax.BooleanType)
  override def managedReport(args: Array[Argument], context: Context, controller: Controller) =
    Boolean.box(controller.ping())
}
