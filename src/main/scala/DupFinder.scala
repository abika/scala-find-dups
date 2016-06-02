/**
  * Created by zeta on 31.05.16.
  */

import java.io.File

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

// argument configuration
case class Config(verbose: Boolean = false,
                  recursive: Boolean = false,
                  dir: File = new File("."))

object DupFinder {
  val LOG = Logger(LoggerFactory.getLogger(DupFinder.getClass.getName))

  def main(args: Array[String]): Unit = {

    val parser = new scopt.OptionParser[Config]("dupfinder") {
      head("dupfinder", "0.1")

      opt[Unit]("verbose") action { (_, c) =>
        c.copy(verbose = true)
      } text ("enable debug output")

      opt[Unit]('r', "recursive") action { (_, c) =>
        c.copy(recursive = true)
      } text ("include subdirectories (recursive)")

      arg[File]("<search-directory>") required() valueName ("dir") action { (x, c) =>
        c.copy(dir = x)
      } text ("directory to search in")

      help("help") text ("prints this usage text")

      //note("some notes.\n")
    }

    // parser.parse returns Option[C]
    parser.parse(args, Config()) map { config =>
      // get all files
      val files = if (config.recursive) Utils.listFilesRec(config.dir) else Utils.listFiles(config.dir)
      val groups = files.groupBy(new FileKey(_))

      // print all none-single groups
      groups
        .filter{case (fk, fs) => fs.length > 1}
        .foreach(t => println(t._1 + " : " + t._2.mkString(",")))

      println("#duplicates found: " + groups.map{case (fk, fs) => fs.length - 1}.sum)
    } getOrElse {
      // arguments are bad, usage message will have been displayed
    }
  }
}
