scalaVersion := "2.13.18"

libraryDependencies += "org.scalatest" %% "scalatest-freespec" % "3.2.20" % Test

enablePlugins(ScalaNativePlugin)

evictionErrorLevel := Level.Warn
