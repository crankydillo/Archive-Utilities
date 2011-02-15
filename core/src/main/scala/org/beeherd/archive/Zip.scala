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

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.util.regex._
import java.util.zip._

import org.apache.commons.io.IOUtils

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
    require(file != null && file.isFile && dir != null && dir.isDirectory);
    use(file) { _.explode(dir) }
  }

  /**
   * Create a zip from some directory.
  def archive(name: String, d: File = new File("."), overwrite: Boolean = false): File = {
    require(name != null && name.trim.size > 0);
    val target = new File(d, name);
    if (target.exists && !overwrite)
      throw new IllegalArgumentException(target.getAbsolutePath + " already exists " +
      "and overwrite is set to false.");

  }
   */

  /**
   * Create a new zip by adding a file to an existing zip in a directory
   * specified by the path.
  def add(zipFile: ZipFile, file: File, path: String = ".") 
    = add(zipFile, List(file), path)
   */

  /**
   * Create a new zip by adding files to an existing zip in a directory
   * specified by the path.
  def add(zipFile: ZipFile, files: List[File], path: String = "."): File = {
    require(zipFile != null);
    require(files != null && files.size > 0);
    require(path != null);
    // TODO research modifying a zip without explode/modify/zip/replace

    if (!zipFile.exists(path))
      throw new IllegalArgumentException(path + ", which serves as the base " +
      "directory to which to add files, does not exist in the archive.");

    TempDir.use[File] {d =>
      zipFile.explode(d);
      val target = new File(d, "/" + path);
      if (!target.exists)
        target.mkdirs();
      else if (!target.isDirectory)
        throw new IllegalArgumentException(path + " must refer to some directory "
        + "within the archive.");

      if (f.isDirectory)
        FileUtils.copyDirectoryToDirectory(f, target);
      else 
        FileUtils.copyFileToDirectory(f, target);
      Zip.archive(d);
    }
  }
   */
}
