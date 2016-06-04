/*
 * DupFinder
 * Copyright (C) 2016 A.B.
 */

import java.io.File
import java.nio.file.{Files, Path}
import java.security.{DigestInputStream, MessageDigest}
import java.util.logging.LogManager

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/** Utility functions.
  *
  * @author Alexander Bikadorov { @literal <bikaejkb@mail.tu-berlin.de>}
  */
object Utils {
  val Log = Logger(LoggerFactory.getLogger(Utils.getClass.getName))

  def setDebugLevel(level: Level): Unit = {
    val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    root.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
  }

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
      Log.warn(s"not a directory: $d")
      false
    } else {
      true
    }

  def md5sum(f: File): Array[Byte] = {
    val md = MessageDigest.getInstance("MD5")

    // whole file at once in memory, no good
    //md.update(Files.readAllBytes(file.toPath))

    val buffer = new Array[Byte](8192)
    // Scala doesn't know try-with-resource, but...
    for {
      is <- resource.managed(Files.newInputStream(f.toPath))
      dis <- resource.managed(new DigestInputStream(is, md))
    } {
      while (dis.read(buffer) != -1) {}
    }

    md.digest()
  }
}
