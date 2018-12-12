package org.programmiersportgruppe.sbt.testreporter

import java.io.File
import java.nio.file.{Files, Path, Paths}

sealed trait LatestResultMode {
  def updateLatestResult(resultPath: String, target: File)
}

object LatestResultMode {

  case object Symlink extends LatestResultMode {
    override def updateLatestResult(resultPath: String, target: File): Unit = {
        try {
          val targetPath = target.toPath
          if (target.exists()) {
            Files.delete(targetPath)
          }

          Files.createSymbolicLink(targetPath, Paths.get(resultPath))
        } catch {case ex: java.nio.file.FileSystemException => (

        )}
    }
  }

  case object Skip extends LatestResultMode {
    override def updateLatestResult(resultPath: String, target: File): Unit = {
      //skip
    }
  }

  case object Copy extends LatestResultMode {
    override def updateLatestResult(resultPath: String, target: File): Unit = {
      if (target.exists()) {
        target.delete()
      }

      Files.copy(new File(resultPath).toPath, target.toPath)
    }
  }

}