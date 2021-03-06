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
import java.util.regex.Pattern

import org.specs._
import org.specs.runner.JUnit4

import org.apache.commons.io.FileUtils

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

    "spit out the text of an entry" in {
      // newline because windows/unix thing
      val opt = tar.entryAsString("dir1/foo.txt");
      opt must beSomething;
      opt.get must beEqual("foo\n");
    }

    "explode a tar to some directory" in {
      val dir = File.createTempFile("tar-tst", "dir");
      dir.delete();
      dir.mkdir();
      try {
        tar.explode(dir);
        val f = new File(dir, "foo.txt");
        f must exist;
        f must beFile;
        FileUtils.readFileToString(f) must beEqual("hi from foo\n");
        new File(dir, "dir2") must beDirectory;
      } finally {
        if (dir.isDirectory)
          FileUtils.deleteDirectory(dir);
        else
          dir.delete();
      }
    }

  }
}
