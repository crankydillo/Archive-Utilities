/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
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
import java.util.regex._
import java.util.zip._

import org.apache.commons.io.{FileUtils, IOUtils}

import org.beeherd.io.TempDir

/**
 *
 * @author scox
 */
class Zip(val zipFile: ZipFile) extends Archive {
  import scala.collection.JavaConversions._

  def entryNames(pattern: Pattern): List[String] = 
    entryNames.filter {e => pattern.matcher(e).matches}

  def entryNames: List[String] = zipFile.entries.map {_.getName}.toList

  def entryAsString(
    name: String
    , encoding: String = Zip.DefaultEncoding
  ): String = {
    val entry = zipFile.getEntry(name);
    val in = zipFile.getInputStream(entry);
    try {
      IOUtils.toString(in, encoding)
    } finally {
      try { in.close } catch { case e:Exception => /* TODO: Log */ e.printStackTrace}
    }
  }

  def exists(path: String): Boolean = zipFile.entries.exists {_ == path}

  /**
   * @throws IllegalArgumentException if dir exists and is not directory.
   */
  def explode(dir: File): Unit = {
    require(!dir.exists || dir.isDirectory);

    if (!dir.exists)
      dir.mkdirs();

    val entries = zipFile.entries;
    entries.foreach {e =>
      val newFile = new File(dir, e.getName);
      if (e.isDirectory) {
        newFile.mkdirs();
      } else {
        val dir = newFile.getParentFile();
        if (dir != null && !dir.exists)
          dir.mkdirs();
        newFile.createNewFile();
        val out = new BufferedOutputStream(new FileOutputStream(newFile));
        try {
          IOUtils.copy(zipFile.getInputStream(e), out);
        } finally {
          out.close();
        }
      }
    }
  }
}

object Zip {
  val DefaultEncoding = System.getProperty("file.encoding");

  def use[T](file: File)(f: Zip => T): T = {
    require(file != null && file.isFile);
    val zipFile = new ZipFile(file);
    try {
      val zip = new Zip(zipFile);
      f(zip)
    } finally {
      try {zipFile.close();} catch { case e:Exception => {}}
    }
  }

  def explode(file: File, dir: File): Unit = {
    require(file != null && file.isFile)
    require(dir != null && dir.isDirectory);
    use(file) { _.explode(dir) }
  }

  /**
   * Create a zip from some files.
   * 
   */
  def archive(
      target: File
      , overwrite: Boolean = false
      , dir: File
    ): File = {

    require(dir != null && dir.exists);
    require(dir.isDirectory);

    if (target.exists && !overwrite) {
      throw new IllegalArgumentException(target.getAbsolutePath + " already exists " +
      "and overwrite is set to false.");
    } else if (target.exists) {
      FileUtils.forceDelete(target);
    }

    target.createNewFile();

    write(target) {out =>
      def write(f: File, path: String): Unit = {
        if (f.isFile) {
          val zipEntry = new ZipEntry(path + f.getName);
          out.putNextEntry(zipEntry);
          val in = new FileInputStream(f);
          try {
            IOUtils.copy(in, out);
          } finally {
            try { in.close } catch {case e:Exception => {}}
          }
          out.closeEntry();
        } else {
          val newPath = path + f.getName + "/";
          val zipEntry = new ZipEntry(newPath)
          out.putNextEntry(zipEntry);
          out.closeEntry();
          f.listFiles.foreach {write(_, newPath)}
        }
      }

      dir.listFiles.foreach {write(_, "")}
    }
    target
  }

  /**
   * Create a new zip by adding files to an existing zip in a directory
   * specified by the path.
   */
  def add(zip: File, path: String, files: File*): File = {
    require(zip != null);
    require(files != null && files.size > 0);
    require(path != null);
    // TODO research modifying a zip without explode/modify/zip/replace

    TempDir.use[File] {d =>
      explode(zip, d);
      val target = new File(d, "/" + path);
      if (!target.exists)
        target.mkdirs();
      else if (!target.isDirectory)
        throw new IllegalArgumentException(path + " must refer to some directory "
        + "within the archive.");

      files.foreach {f =>
        if (f.isDirectory)
          FileUtils.copyDirectoryToDirectory(f, target);
        else 
          FileUtils.copyFileToDirectory(f, target);
      }

      val newZip = archive(zip, true, d)
      newZip
    }
  }

  /**
  * Control structure for writing to a ZipOutputStream.
  */
  def write[T](zip: File)(f: (ZipOutputStream) => T): T = {
    val out = new ZipOutputStream(new BufferedOutputStream(
        new FileOutputStream(zip)));
    try {
      f(out);
    } finally {
      try {out.flush} catch { case e:Exception => {}}
      try {out.close} catch { case e:Exception => {}}
    }
  }
}
