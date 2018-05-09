lazy val reotools = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(ScalaJSPlugin, WorkbenchPlugin)
    .aggregate(local_script)
  .settings(
    name := "reotools",
    version := "1.0",
    scalaVersion := "2.12.4",
    scalacOptions ++= Seq("-unchecked", "-deprecation","-feature"),
    resolvers ++= Seq(
      "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
      "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12",
      "org.choco-solver" % "choco-solver" % "4.0.6",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
      "com.typesafe.play" %% "play" % "2.6.11",
      jdbc , ehcache , ws , specs2 % Test , guice
    ),
    unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" ),
    unmanagedSourceDirectories in Compile += baseDirectory.value / "lib/preo/src/main/scala"
  )

lazy val local_script = (project  in file("localJS"))
 .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
 .disablePlugins(PlayScala)
 .settings(
    name := "local_script",
    version := "1.0",
    scalaVersion := "2.12.4",
    scalacOptions ++= Seq("-unchecked", "-deprecation","-feature"),
   libraryDependencies ++= Seq(
     "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
     "junit" % "junit" % "4.12",
     //  "org.choco-solver" % "choco-solver" % "3.3.1-j7",
     "org.choco-solver" % "choco-solver" % "4.0.6",
     "org.scala-lang" % "scala-compiler" % scalaVersion.value,
     "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.0.5",
     /////
     "org.scala-js" %%% "scalajs-dom" % "0.9.1",
     "com.lihaoyi" %%% "scalatags" % "0.6.7",
     "org.singlespaced" %%% "scalajs-d3" % "0.3.4"
   ),
    unmanagedSourceDirectories in Compile += baseDirectory.value / "../lib/preo/src/main/scala"
 )

// todo: add here a task for, when compiling the server, copying the content into the app/...

