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

// argument configuration
case class Config(verbose: Boolean = false,
                  recursive: Boolean = false,
                  regex: Option[String] = None,
                  dir: File = new File("."))

/** Find duplicate files.
  *
  * @author Alexander Bikadorov { @literal <bikaejkb@mail.tu-berlin.de>}
  */
object DupFinder {
  val Log = Logger(LoggerFactory.getLogger(DupFinder.getClass.getName))

  private def parseArgs(args: Array[String]): Option[Config] = {
    val parser = new scopt.OptionParser[Config]("dupfinder") {
      head("dupfinder", "0.1")

      opt[Unit]('v', "verbose") action { (_, c) =>
        c.copy(verbose = true)
      } text ("enable debug output")

      opt[Unit]('r', "recursive") action { (_, c) =>
        c.copy(recursive = true)
      } text ("include subdirectories (recursive)")

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

      // get all files
      val files = (if (config.recursive) {
        // disappointing: no duck typing possible here
        Utils.filesInPath(config.dir.toPath).map(_.toFile).toArray
      } else {
        Utils.listFiles(config.dir)
      }).filter(_.isFile)

      // grouping and sorting
      val groups = files.groupBy(new FileKey(_)).mapValues(_.sortBy(_.lastModified()))

      // get all none-single groups
      val dupGroups = groups.filter { case (_, fs) => fs.length > 1 }
      //dupGroups.foreach(t => println(t._2.mkString(" ")))

      val divGroups = dupGroups.values.map(fs => {
        // take the one file to keep...
        fs.partition(f => f == (config.regex match {
          case Some(regex) => fs.find(!_.getName.matches(regex)).getOrElse(fs.head)
          case None => fs.head
        // ... and all files not to delete
        }) || config.regex.isDefined && !f.getName.matches(config.regex.get))
      }).toList

      // print what to keep and what to delete
      divGroups.foreach(t => println(s"${t._1.mkString(" ")} ### ${t._2.mkString(" ")}"))

      println(s"Scanned ${files.length} files." +
        s" Found ${dupGroups.map { case (_, fs) => fs.length }.sum - dupGroups.size} duplicates" +
        s" in ${dupGroups.size} groups.")



    } getOrElse {
      // arguments are bad, usage message will have been displayed
    }
  }
}
