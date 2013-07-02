package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultReporter, Syntax }

class GoGoSensor extends DefaultReporter {
  override def getSyntax = Syntax.reporterSyntax(Array(Syntax.NumberType), Syntax.NumberType)
  override def report(args: Array[Argument], context: Context) = {
    try Double.box(controller.readSensor(args(0).getIntValue))
    catch {
      case e: RuntimeException => Double.box(0)
    }
  }
}
