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
import java.util.zip.GZIPInputStream

import org.apache.commons.compress.archivers.tar._

class TarGz(val file: File) extends Archived {

  /**
   * @inheritDoc
   */
  def entryNames: List[String] = use { _.entryNames }

  /**
   * @inheritDoc
   */
  def entryAsString(name: String, encoding: String): Option[String] = 
    use { _.entryAsString(name, encoding) }

  /**
   * @inheritDoc
   */
  def explode(dir: File): Unit = use { _.explode(dir) }

  private def use[T](fn: (TarInputStreamArchive) => T): T = {
    val in = new TarArchiveInputStream(new BufferedInputStream(
      new GZIPInputStream(new FileInputStream(file))));
    val streamArchive = new TarInputStreamArchive(in);
    try {
      fn(streamArchive)
    } finally {
      try { in.close } catch { case e:Exception => {} }
    }
  }
}
