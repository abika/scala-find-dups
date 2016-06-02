import java.io.File

/**
  * Utility key object that is equal for equal files. Immutable.
  */
class FileKey(file: File) {
  private val file_ = file

  override def equals(that: Any): Boolean =
    that match {
      case that: FileKey => that.file_.length() == file_.length()
      case _ => false
    }

  override def hashCode: Int = 31 + 7 * file_.length().toInt

  override def toString: String = file_ + "#" + hashCode
}
