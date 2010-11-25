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
package org.beeherd.zip

import java.io.File
import java.util.regex._

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
  * The underlying file
  */
  def file: File
}
