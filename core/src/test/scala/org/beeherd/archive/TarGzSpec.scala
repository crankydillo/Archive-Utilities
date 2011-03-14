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

class TarGzSpecTest extends JUnit4(TarGzSpec)
object TarGzSpec extends Specification {
  val file = new File(getClass.getResource("/foo.tar.gz").getFile);
  val tar = new TarGz(file);

  "TarGz" should {
    "list the entries in a tar.gz file" in {
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
      val txt = tar.entryAsString("dir1/foo.txt");
      txt must beSomething;
      txt.get must beEqual("foo\n");
    }

    "explode a tar.gz to some directory" in {
      val dir = File.createTempFile("targz-tst", "dir");
      dir.delete();
      dir.mkdir();
      try {
        tar.explode(dir);
        val f = new File(dir, "foo.txt");
        f must exist;
        f must beFile;
        FileUtils.readFileToString(f) must beEqual("hi from foo\n");
        val dir2 = new File(dir, "dir2") 
        dir2 must beDirectory;
        new File(dir2, "bar.txt") must exist
      } finally {
        if (dir.isDirectory)
          FileUtils.deleteDirectory(dir);
        else
          dir.delete();
      }
    }
  }
}
