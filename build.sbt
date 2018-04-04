name := "akkaHello"

version := "0.1"

scalaVersion := "2.12.4"


lazy val akkaVersion = "2.5.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka"  %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)


addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

exportJars := true

mainClass in Compile := Some("helloakka.HelloAkkaScala")
mainClass in(Compile, run) := Some("helloakka.HelloAkkaScala")
mainClass in(Compile, packageBin) := Some("helloakka.HelloAkkaScala")
mainClass in assembly := Some("helloakka.HelloAkkaScala")
