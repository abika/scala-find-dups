import java.io.File
import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Utility functions.
  */
object Utils {
  val LOG = Logger(LoggerFactory.getLogger(DupFinder.getClass.getName))

  def listFiles(d: File): Array[File] =
    if (isDirectory(d)) {
      d.listFiles
    } else {
      Array[File]()
    }

  def listFilesRec(d: File): Array[File] =
    if (isDirectory(d)) {
      val these = d.listFiles
      these ++ these.filter(f => f.isDirectory && !Files.isSymbolicLink(f.toPath)).flatMap(listFilesRec)
    } else {
      Array[File]()
    }

  // not used
  def filesInPath(p: Path) : Iterator[Path] =
    if (Files.isDirectory(p)) {
      Files.walk(p).iterator().asScala.filter(Files.isRegularFile(_))
    } else {
      Iterator()
    }

  private def isDirectory(d: File): Boolean =
    if (!d.isDirectory) {
      LOG.debug("not a directory: " + d)
      false
    } else {
      true
    }
}
