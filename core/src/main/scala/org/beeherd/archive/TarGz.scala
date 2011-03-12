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

class TarGz(val file: File) extends Archive {
  private val delegate = new VfsFacade(file, "tgz");
  /**
   * @inheritDoc
   */
  def entryNames: List[String] = delegate.entryNames

  /**
   * @inheritDoc
   */
  def entryAsString(name: String, encoding: String): String = 
    delegate.entryAsString(name, encoding);

  /**
   * @inheritDoc
   */
  def explode(dir: File): Unit = delegate.explode(dir);
}
