package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoOpen extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.StringType))
  override def perform(args: Array[Argument], context: Context) {

    try close()
    catch {
      case e: RuntimeException => throw new EE("Cannot close port: %s : %s".format(controller.currentPortName, e.getLocalizedMessage))
    }

    try {
      initController(args(0).getString)
      controller.openPort()
      controller.setReadTimeout(50)
    }
    catch {
      case e: NoClassDefFoundError => throw new EE("Could not initialize GoGo Extension.  Please ensure that you have installed RXTX correctly.  Full error message: %s : %s".format(args(0).getString, e.getLocalizedMessage))
      case e: RuntimeException     => throw new EE("Could not open port %s : %s".format(args(0).getString, e.getLocalizedMessage))
    }

    try if (!controller.ping()) throw new EE("GoGo board not responding.")
    catch {
      case e: RuntimeException => throw new EE("GoGo board not responding: " + e.getLocalizedMessage)
    }

  }
}
