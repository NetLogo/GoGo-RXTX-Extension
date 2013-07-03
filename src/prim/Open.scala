package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, DefaultCommand, Syntax },
    extensions.gogo.controller.ControllerManager

class Open(manager: ControllerManager) extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.StringType))
  override def perform(args: Array[Argument], context: Context) {

    import manager.{ getControllerOpt => controllerOpt }

    manager.close()

    try {
      manager.init(args(0).getString)
      controllerOpt foreach {
        controller =>
          controller.openPort()
          //@ c@ controller.setReadTimeout(50)
      }
    }
    catch {
      case e: NoClassDefFoundError => throw new EE("Could not initialize GoGo Extension.  Please ensure that you have installed RXTX correctly.  Full error message: %s : %s".format(args(0).getString, e.getLocalizedMessage))
      case e: Exception            => throw new EE("Could not open port %s : %s".format(args(0).getString, e.getLocalizedMessage))
    }

    if (!(controllerOpt map (_.ping()) getOrElse false))
      throw new EE("GoGo board not responding.")

  }
}
