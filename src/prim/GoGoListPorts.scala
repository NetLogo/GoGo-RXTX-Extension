package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultReporter, LogoList, Syntax }

class GoGoListPorts extends DefaultReporter {
  override def getSyntax = Syntax.reporterSyntax(Syntax.ListType)
  override def report(args: Array[Argument], context: Context) = {
    try LogoList.fromJava(GoGoController.availablePorts)
    catch {
      case e: NoClassDefFoundError => throw new EE("Could not initialize GoGo Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + e.getLocalizedMessage)
    }
  }
}
