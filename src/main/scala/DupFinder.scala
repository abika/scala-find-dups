/*
 * DupFinder
 * Copyright (C) 2016 A.B.
 */



import java.io.File
import java.util.regex.Pattern

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.Try

import FileScanner.FileGroups

/** Find duplicate files.
  *
  * @author Alexander Bikadorov { @literal <bikaejkb@mail.tu-berlin.de>}
  */
object DupFinder {
  val Log = Logger(LoggerFactory.getLogger(DupFinder.getClass.getName))

  // argument configuration
  case class Config(verbose: Boolean = false,
                    recursive: Boolean = false,
                    printDeletable: Boolean = false,
                    delete: Boolean = false,
                    regex: Option[String] = None,
                    dir: File = new File("."))

  private def parseArgs(args: Array[String]): Option[Config] = {
    val parser = new scopt.OptionParser[Config]("dupfinder") {
      head("Duplicate finder", "0.1")

      opt[Unit]('v', "verbose") action { (_, c) =>
        c.copy(verbose = true)
      } text ("enable debug output")

      opt[Unit]('r', "recursive") action { (_, c) =>
        c.copy(recursive = true)
      } text ("include subdirectories (recursive search)")

      opt[Unit]('d', "print-deletable") action { (_, c) =>
        c.copy(printDeletable = true)
      } text ("print only duplicate groups with deletable files")

      opt[Unit]('D', "delete") action { (_, c) =>
        c.copy(delete = true)
      } text ("delete redundant duplicate files(!)")

      opt[String]('x', "regex") action { (x, c) =>
        c.copy(regex = Some(x)) } validate { x =>
        if (Try(Pattern.compile(x)).isSuccess) {
          success
        } else {
          failure("Value <regex> not a valid regular expression")
        }
      } valueName("<regex>") text ("regular expression that must match file name for deletion")

      arg[File]("<search-directory>") required() valueName ("dir") action { (x, c) =>
        c.copy(dir = x)
      } text ("directory to search in")

      help("help") text ("prints this usage text")

      note("Find duplicate files. Duplicates are identified by file content" +
        " and sorted by modification time (oldest first).\n")
    }
    parser.parse(args, Config())
  }

  def main(args: Array[String]): Unit = {
    parseArgs(args) map { config =>
      // set global log level
      Utils.setDebugLevel(if (config.verbose) Level.DEBUG else Level.INFO)

      val groups = FileScanner.scan(config.dir, config.recursive)

      // get all none-single groups
      val dupGroups = groups.filter {_.length > 1}

      val judged = Utils.definedOrDefault(config.regex, dupGroups.classify(), dupGroups.classify)

      // print what to keep and what to delete
      judged
        .filter(config.printDeletable || !_.duplicates.isEmpty)
        .foreach(t => println(s"${t.originals.mkString(" ")}" +
          s" ### ${t.duplicates.mkString(" ")}"))

      val deletable = judged.map(_.duplicates).flatten

      println(s"Scanned ${groups.map(_.length).sum} files." +
        s" Found ${dupGroups.map(_.length).sum - dupGroups.length} duplicates" +
        s" in ${dupGroups.size} groups." +
        s" ${deletable.length} file(s) (${Utils.hByteCount(deletable.map(_.length()).sum)})" +
        s" marked for deletion.")

    } getOrElse {
      // arguments are bad, usage message will have been displayed
    }
  }
}
