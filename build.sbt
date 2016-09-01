lazy val root = (project in file(".")).
  settings(
    organization := "pl.wavesoftware.hacking",
    name := "rh-harvester",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.8"
  )

libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.23"
libraryDependencies += "pl.wavesoftware" % "eid-exceptions" % "1.2.0"