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

import org.specs._
import org.specs.runner.JUnit4

class TarSpecTest extends JUnit4(TarSpec)
object TarSpec extends Specification {
  val file = new File(getClass.getResource("/foo.tar").getFile);
  val tar = new Tar(file);

  "Tar" should {
    "list the entries in a tar file" in {
      val entries = tar.entryNames;
      entries must haveSize(6);
      entries.head must beEqual("dir1/");
      entries.last must beEqual("foo.txt");
    }

    "list the entries that match a given Pattern" in {
      val p = Pattern.compile(".*\\.txt");
      val entries = tar.entryNames(p);
      entries must haveSize(4);
      entries.foreach {e => e must endWith(".txt")}
    }
  }
}
