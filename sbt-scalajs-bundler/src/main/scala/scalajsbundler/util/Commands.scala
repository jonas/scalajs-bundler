package scalajsbundler.util

import sbt._
import scalajsbundler.Compat.Process

object Commands {

  def run(cmd: Seq[String], cwd: File, logger: Logger): Unit = {
    val process = Process(cmd, cwd)
    val code = process ! logger
    if (code != 0) {
      sys.error(s"Non-zero exit code: $code")
    }
    ()
  }

}
