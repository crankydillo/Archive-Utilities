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

import java.io._

import org.apache.commons.compress.archivers._
import org.apache.commons.compress.archivers.tar._
import org.apache.commons.io.IOUtils

class Tar(val file: File) extends Archived {

  /**
   * @inheritDoc
   */
  def entryNames: List[String] = use { _.entryNames }

  /**
   * @inheritDoc
   */
  def entryAsString(
    name: String
    , encoding: String 
  ): Option[String] = use { _.entryAsString(name, encoding) }

  def exists(path: String): Boolean = entryNames.exists {_ == path}

  /**
   * @inheritDoc
   * @throws IllegalArgumentException if dir exists and is not directory.
   */
  def explode(dir: File): Unit = use { _.explode(dir) }

  private def use[T](fn: (TarInputStreamArchive) => T): T = {
    val in = new TarArchiveInputStream(new BufferedInputStream(
      new FileInputStream(file)));
    val streamArchive = new TarInputStreamArchive(in);
    try {
      fn(streamArchive)
    } finally {
      try { in.close } catch { case e:Exception => {} }
    }
  }


}

class TarInputStreamArchive(in: TarArchiveInputStream) 
extends Archived {

  /**
   * @inheritDoc
   */
  def entryNames: List[String] = {
    var entry = in.getNextTarEntry();

    val buffer = new scala.collection.mutable.ArrayBuffer[String]();
    while (entry != null) {
      buffer += entry.getName
      entry = in.getNextTarEntry();
    }

    buffer.toList
  }

  /**
   * @inheritDoc
   */
  def entryAsString(
    name: String
    , encoding: String 
  ): Option[String] = {
    var entry: TarArchiveEntry = in.getNextTarEntry();

    while (entry != null) {
      if (entry.getName == name) {
        val bytes = new Array[Byte](entry.getSize.asInstanceOf[Int]);
        in.read(bytes);
        return Some(new String(bytes, encoding))
      }

      entry = in.getNextTarEntry();
    }
    None
  }

  def exists(path: String): Boolean = entryNames.exists {_ == path}

  /**
   * @inheritDoc
   * @throws IllegalArgumentException if dir exists and is not directory.
   */
  def explode(dir: File): Unit = {
    require(!dir.exists || dir.isDirectory);

    if (!dir.exists)
      dir.mkdirs();

    var entry: TarArchiveEntry = in.getNextTarEntry();
      
    while (entry != null) {
      val newFile = new File(dir, entry.getName);
      if (entry.isDirectory) {
        newFile.mkdirs();
      } else {
        val dir = newFile.getParentFile();
        if (dir != null && !dir.exists)
          dir.mkdirs();
        val bytes = new Array[Byte](entry.getSize.asInstanceOf[Int]);
        in.read(bytes);
        val bytesIn = new BufferedInputStream(new ByteArrayInputStream(bytes));
        val out = new BufferedOutputStream(new FileOutputStream(newFile));
        try {
          IOUtils.copy(bytesIn, out);
        } finally {
          try {bytesIn.close()} catch { case e:Exception => {}}
          try {out.close()} catch { case e:Exception => {}}
        }
      }

      entry = in.getNextTarEntry();
    }
  }
}
