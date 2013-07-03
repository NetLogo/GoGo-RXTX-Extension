package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, DefaultCommand, DefaultReporter },
    extensions.gogo.controller.{ Controller, ControllerManager }

abstract class ManagedReporter(manager: ControllerManager) extends DefaultReporter {

  final override def report(args: Array[Argument], context: Context) = {
    manager.withController {
      controller => managedReport(args, context, controller)
    }
  }

  def managedReport(args: Array[Argument], context: Context, controller: Controller) : AnyRef

}

abstract class ManagedCommand(manager: ControllerManager) extends DefaultCommand {

  final override def perform(args: Array[Argument], context: Context) {
    manager.withController {
      controller => managedPerform(args, context, controller)
    }
  }

  def managedPerform(args: Array[Argument], context: Context, controller: Controller)

}
