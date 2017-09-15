package scalajsbundler

object Compat {

  type Process = sbt.Process
  val Process = sbt.Process
  type ProcessLogger = sbt.ProcessLogger

}
