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
package org.beeherd.zip

import java.io.File
import java.util.regex._
import java.util.zip._

/**
 *
 * @author scox
 */
trait Archive {
  /**
   * Find the entries matching the supplied pattern.
   */
  def entryNames(pattern: Pattern): List[String]

  /**
   * List all the archive's entries.
   */
  def entryNames: List[String]

  /**
   * Return the contents of an entry as a string.
   */
  def entryAsString(name: String, encoding: String): String

  /**
   * Explode the archive into the given directory.
   */
  def explode(dir: File): Unit

  /**
   * Add the file to the archive using the path as a pointer
   * to a directory within the archive.
   */
   def add(f: File, path: String): Unit
}

import java.io.{BufferedOutputStream, FileOutputStream}

import org.apache.commons.io.IOUtils

/**
 *
 * @author scox
 */
class Zip(zipFile: ZipFile) extends Archive {
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

  def explode(dir: File): Unit = {
    require(!dir.exists || dir.isDirectory);

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

  def add(f: File, path: String = "."): Unit = {
  }
}

object Zip {
  val DefaultEncoding = System.getProperty("file.encoding")
}
