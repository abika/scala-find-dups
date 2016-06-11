/*
 * DupFinder
 * Copyright (C) 2016 A.B.
 */

import java.io.File

sealed case class FileGroup(originals: Array[File], duplicates: Array[File]) {
  override def toString(): String =
    s"${originals.mkString(" ")}" +
    s" ### ${duplicates.mkString(" ")}"
}

/**
  * Scan folder and find duplicate files.
  *
  * @author Alexander Bikadorov { @literal <bikaejkb@mail.tu-berlin.de>}
  */
object FileScanner {
  // oho!
  implicit class FileGroups(val fileGroups: List[Array[File]]) {
    /** For each group, split files into "keep" and "delete" bins based on modification time and
      * optional regular expression.
      */
    def classify(regex: String = ""): List[FileGroup] =
      fileGroups
        .map(_.sortBy(_.lastModified()))
        .map(fs => {
        fs.partition(f =>
          if (regex.isEmpty)
            f == fs.head
          else
            f == fs.find(!_.getName.matches(regex)).getOrElse(fs.head)
              // ... and all files not to delete
              || !f.getName.matches(regex))
      }).map(t => FileGroup(t._1, t._2))
  }

  /** List all files in folder grouped by content. */
  def scan(rootFolder: File, recursive: Boolean): List[Array[File]] =
    (if (recursive)
      // disappointing: no duck typing possible here
      Utils.filesInPath(rootFolder.toPath).map(_.toFile).toArray
    else
      Utils.listFiles(rootFolder)
    )
      .filter(_.isFile)
      // grouping
      .groupBy(new FileKey(_))
      .values
      .toList
}
