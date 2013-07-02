package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoOutputPortPower extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.NumberType))
  override def perform(args: Array[Argument], context: Context) {
    ensureGoGoPort()
    try controller.setOutputPortPower(args(0).getIntValue)
    catch {
      case e: RuntimeException => throw new EE("Cannot set output port power: " + e.getLocalizedMessage)
    }
  }
}
