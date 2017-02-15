name := "n2sgen"

version := "2.2.0"
scalaVersion := "2.12.1"

val Versions = new {
  val jodaTime = "2.9.7"
  val pegDown = "1.6.0"
  val typeSafeConfig = "1.3.1"
  val jetty =  "9.1.4.v20140401"
}

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % Versions.jodaTime,
  "org.pegdown" % "pegdown" % Versions.pegDown,
  "com.typesafe" % "config" % Versions.typeSafeConfig,
  "org.eclipse.jetty.aggregate" % "jetty-all" % Versions.jetty
)