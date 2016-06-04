/*
 * DupFinder
 * Copyright (C) 2016 A.B.
 */

import java.io.File

/**
  * Utility key object that is equal for equal files. Immutable.
  *
  * @author Alexander Bikadorov { @literal <bikaejkb@mail.tu-berlin.de>}
  */
class FileKey(val file: File) {
  private lazy val hash = Utils.md5sum(file)

  override def equals(that: Any): Boolean =
    that match {
      // no element comparison with "==" for Scala "Array"s
      case that: FileKey => file.length() == that.file.length() && hash.sameElements(that.hash)
      case _ => false
    }

  override def hashCode: Int = 31 + 7 * file.length().toInt

  override def toString: String = s"K{${hashCode.toString}}"
}
