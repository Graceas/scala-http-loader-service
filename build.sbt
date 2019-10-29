lazy val akkaHttpVersion  = "10.1.5"
lazy val akkaVersion      = "2.5.18"

lazy val scalaTestVersion = "3.0.4"
lazy val slickVersion     = "3.2.1"
lazy val sttpVersion      = "1.1.5"
lazy val nettyVersion     = "4.1.23.Final"

resolvers += "Jitpack" at "https://jitpack.io"
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "graceas",
      scalaVersion    := "2.12.7"
    )),
    name := "Graceas-Loader",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
//      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion  % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion      % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion      % Test,
      "org.scalatest"     %% "scalatest"            % scalaTestVersion % Test,

      // Dependency injection
      "org.scaldi" %% "scaldi" % "0.5.8",

      // Sugar for serialization and deserialization in akka-http with jackson
      "de.heikoseeberger" %% "akka-http-jackson" % "1.19.0",
      "com.google.code.gson" % "gson" % "2.3.1",

      // Datetime
      "joda-time" % "joda-time" % "2.9.9",

      // Auth
      "com.auth0" % "java-jwt" % "3.2.0",

      // Config file parser
      "com.github.pureconfig" %% "pureconfig" % "0.9.0",

      // Apache commons-lang
      "org.apache.commons" % "commons-lang3" % "3.1",

      // Validation library
      "com.wix" %% "accord-core" % "0.7.2",

      // Parsing and generating of JWT tokens
      "com.pauldijou" %% "jwt-core" % "0.14.0",

      // Logging
      "org.apache.logging.log4j" % "log4j-api" % "2.8.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    )
  )
