package scalajsbundler

object Compat {

  type Process = scala.sys.process.Process
  val Process = scala.sys.process.Process
  type ProcessLogger = scala.sys.process.ProcessLogger

}
