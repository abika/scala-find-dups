/*
 * DupFinder
 * Copyright (C) 2016 A.B.
 */

import java.io.File

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level

// argument configuration
case class Config(verbose: Boolean = false,
                  recursive: Boolean = false,
                  dir: File = new File("."))

/** Find duplicate files.
  *
  * @author Alexander Bikadorov { @literal <bikaejkb@mail.tu-berlin.de>}
  */
object DupFinder {
  val Log = Logger(LoggerFactory.getLogger(DupFinder.getClass.getName))

  def main(args: Array[String]): Unit = {

    val parser = new scopt.OptionParser[Config]("dupfinder") {
      head("dupfinder", "0.1")

      opt[Unit]('v', "verbose") action { (_, c) =>
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
      // set global log level
      Utils.setDebugLevel(if (config.verbose) Level.DEBUG else Level.INFO)

      // get all files
      val files = if (config.recursive) {
        Utils.listFilesRec(config.dir)
      } else {
        Utils.listFiles(config.dir)
      }
      // grouping
      val groups = files.groupBy(new FileKey(_))

      // print all none-single groups
      groups
        .filter{case (fk, fs) => fs.length > 1}
        .foreach(t => Log.debug(t._1 + " : " + t._2.mkString(",")))

      Log.info("#duplicates found: " + groups.map{case (fk, fs) => fs.length - 1}.sum)
    } getOrElse {
      // arguments are bad, usage message will have been displayed
    }
  }
}
