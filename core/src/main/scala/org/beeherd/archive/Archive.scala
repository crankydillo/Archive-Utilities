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

import java.io.File
import java.util.regex._
import java.util.zip._

/**
 * An {@link Archived} implementation that guesses at what the archived
 * implementation is.
 *
 * @author scox
 */
class Archive(file: File) extends Archived {

  require(file != null)
  require(file.exists)
  require(file.isFile)

  private val delegate = {
    val name = file.getName;
    if (name.endsWith(".zip"))
      new Zip(new ZipFile(file))
    else if (name.endsWith(".tar.gz"))
      new TarGz(file)
    else if (name.endsWith(".tar"))
      new Tar(file)
    else
      throw new IllegalArgumentException("Cannot determine what type of archive, " +
      file.getAbsolutePath + ", is.  Determination is done by extension.  Currently, " +
      "only .zip, .tar.gz, and .tar are supported.");
  }

  /**
   * @inheritDoc
   */
  override def entryNames(pattern: Pattern): List[String] = 
    delegate.entryNames(pattern)

  /**
   * @inheritDoc
   */
  def entryNames: List[String] = delegate.entryNames

  /**
   * @inheritDoc
   */
  def entryAsString(name: String, 
      encoding: String = Archive.DefaultEncoding): String
    = delegate.entryAsString(name, encoding)

  /**
   * @inheritDoc
   */
  def explode(dir: File): Unit = {
    delegate.explode(dir);
  }
}

object Archive {
  val DefaultEncoding = System.getProperty("file.encoding")

  def explode(file: File, toDir: File): Unit = new Archive(file).explode(toDir);
}
