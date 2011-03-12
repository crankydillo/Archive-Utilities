/**
* Copyright 2010 Samuel Cox
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.beeherd.archive

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream}
import java.util.regex.Pattern

import org.apache.commons.io.IOUtils
import org.apache.commons.vfs._

/**
* Put a simplified face on Apache Commons VFS project.  I'm mkaing NO attempt at efficiency!
*
* @author scox
*/
class VfsFacade(file: File, ftype: String) extends Archived {
  /**
   * @inheritDoc
   */
  def entryNames: List[String] = {
    val fsManager = VFS.getManager();
    val fileObj = fsManager.resolveFile(ftype + ":" + file.toURI.getPath);

    def fileObjNames(fileObj: FileObject): List[String] = {
      // I'm going to try to make this look like a zip"
      val path = toZipPath(fileObj)
      fileObj.getType match {
        case FileType.FOLDER => 
        path +: fileObj.getChildren.flatMap {fileObjNames _}.toList
        case FileType.FILE_OR_FOLDER => 
        path +: fileObj.getChildren.flatMap {fileObjNames _}.toList
        case _ => List(path)
      }
    }

    fileObj.getChildren.flatMap {fileObjNames _}.toList
  }

  /**
   * @inheritDoc
   */
  def entryAsString(name: String, encoding: String): String = {
    if (name == null) 
      throw new NullPointerException("The entry name may not be null.");

    // Do I really need to do this?
    val matches = entryNames(Pattern.compile(name));
    if (matches.isEmpty)
      throw new IllegalArgumentException("The entry name, " + name + 
        " was not found.");
    if (matches.size > 1)
      throw new IllegalArgumentException("The entry name, " + name +
        " matched more than 1 entry.");

    val path = {
      val tmp = matches(0);
      "/" + (if (tmp.endsWith("/")) tmp.dropRight(1) else tmp);
    }

    val mgr = VFS.getManager();
    val fileObj = mgr.resolveFile(ftype + ":" + file.toURI.getPath + "!" + path);
    val in = fileObj.getContent.getInputStream;
    try {
      IOUtils.toString(in, encoding)
    } finally {
      try { in.close } catch { case e:Exception => /* TODO: Log */ e.printStackTrace}
    }

  }

  /**
   * @inheritDoc
   */
  def explode(dir: File): Unit = {
    require(!dir.exists || dir.isDirectory);

    if (!dir.exists)
      dir.mkdirs();

    val fsManager = VFS.getManager();
    val fileObj = fsManager.resolveFile(ftype + ":" + file.toURI.getPath);

    def writeFileObj(fileObj: FileObject): Unit = {
      // I'm going to try to make this look like a zip"
      val path = toZipPath(fileObj);
      val newFile = new File(dir, path);
      fileObj.getType match {
        case FileType.FOLDER => newFile.mkdirs();
        case FileType.FILE_OR_FOLDER => newFile.mkdirs();
        case _ => {
          val dir = newFile.getParentFile();
          if (dir != null && !dir.exists)
            dir.mkdirs();
          newFile.createNewFile();
          val out = new BufferedOutputStream(new FileOutputStream(newFile));
          val in = fileObj.getContent.getInputStream;
          try {
            IOUtils.copy(in, out);
          } finally {
            try { in.close } catch { case e:Exception => /* TODO: Log */ }
            try {out.close() } catch { case e:Exception => /* TODO: Log */}
          }
        }
      }
    }

    fileObj.getChildren.foreach {writeFileObj _}
  }

  private def toZipPath(fileObj: FileObject): String = {
    val path = fileObj.getName.getPath;
    fileObj.getType match {
      case FileType.FOLDER => (path.drop(1) + "/")
      case FileType.FILE_OR_FOLDER => (path.drop(1) + "/")
      case _ => path.drop(1)
    }
  }

}
