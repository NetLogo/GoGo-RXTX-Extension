scalaVersion := "2.10.0"

scalaSource in Compile <<= baseDirectory(_ / "src")

javaSource in Compile <<= baseDirectory(_ / "src")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                      "-encoding", "us-ascii")

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
                     "-encoding", "us-ascii",
                     "-source", "1.7", "-target", "1.7")

libraryDependencies ++= Seq(
  "org.nlogo" % "NetLogoHeadless" % "6.0-M1" from
    "http://ccl.northwestern.edu/devel/6.0-M1/NetLogoHeadless.jar",
  "org.nlogo" % "NetLogo" % "6.0-M1" from
    "http://ccl.northwestern.edu/devel/6.0-M1/NetLogo.jar",
  "rxtx" % "rxtx" % "2.2pre2" from
    "http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/RXTXcomm.jar",
  "org.picocontainer" % "picocontainer" % "2.13.6"
)

name := "gogo"

NetLogoExtension.settings

NetLogoExtension.classManager := "org.nlogo.extensions.gogo.GoGoExtension"
