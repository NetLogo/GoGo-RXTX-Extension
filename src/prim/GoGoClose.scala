package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoClose extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax
  override def perform(args: Array[Argument], context: Context) {
    try close()
    catch {
      case e: RuntimeException => throw new EE("Cannot close port: %s : %s".format(controller.currentPortName, e.getLocalizedMessage))
    }
  }
}
