package org.nlogo.extensions.gogo

package object prim {

  private[prim] type EE = org.nlogo.api.ExtensionException

  private[prim] def buildMask(xs: Seq[_], maskMap: Map[Char, Int]): Int =
    xs.map(_.toString.toLowerCase.head).distinct.map(maskMap).foldLeft(0){ case (acc, x) => acc | x }

}
