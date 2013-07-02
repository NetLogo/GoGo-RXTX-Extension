package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoSetServo extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.NumberType))
  override def perform(args: Array[Argument], context: Context) {
    ensureGoGoPort()
    controller.setServoPosition(args(0).getIntValue)
  }
}
