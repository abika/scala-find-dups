name := "DupFinder"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.4.0"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
libraryDependencies += "ch.qos.logback" %  "logback-classic" % "1.1.7"
// scala-arm is better
//libraryDependencies += "net.java.truecommons" %  "truecommons-io" % "1.0"
libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.4"

resolvers += Resolver.sonatypeRepo("public")

//addCommandAlias("make-idea", ";update-classifiers; update-sbt-classifiers; gen-idea sbt-classifiers")

