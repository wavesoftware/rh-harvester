lazy val root = (project in file(".")).
  settings(
    organization := "pl.wavesoftware.hacking",
    name := "rh-harvester",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.8"
  )

libraryDependencies += "org.jsoup" % "jsoup" % "1.9.2"