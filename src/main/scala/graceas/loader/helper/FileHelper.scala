package graceas.loader.helper

import java.io.File
import java.util.UUID

object FileHelper {
  def createDir(directory: String): Boolean = {
    val dir: File = new File(directory)

    if (!dir.exists()) {
      dir.mkdirs()

      true
    } else {
      false
    }
  }

  def tempDir(): String = {
    val property: String = "java.io.tmpdir"

    s"${System.getProperty(property)}_loader"
  }

  def uniqueFileName(): String = {
    UUID.randomUUID().toString
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
}
