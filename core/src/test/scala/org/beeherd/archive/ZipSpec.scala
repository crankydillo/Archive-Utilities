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
import java.util.regex.Pattern
import java.util.zip.ZipFile

import org.apache.commons.io.FileUtils

import org.specs._
import org.specs.runner.JUnit4
        
class ZipSpecTest extends JUnit4(ZipSpec)
object ZipSpec extends Specification {
  val zipFile = new ZipFile(getClass.getResource("/foo.zip").getFile);
  val zip = new Zip(zipFile);

  "Zip" should {
    "list the entries in a zip file" in {
      val entries = zip.entryNames;
      entries must haveSize(6);
      entries.first must beEqual("dir1/");
      entries.last must beEqual("foo.txt");
    }

    "list the entries that match a given Pattern" in {
      val p = Pattern.compile(".*\\.txt");
      val entries = zip.entryNames(p);
      entries must haveSize(4);
      entries.foreach {e => e must endWith(".txt")}
    }

    "spit out the text of an entry" in {
      // newline because windows/unix thing
      zip.entryAsString("dir1/foo.txt") must beEqual("foo\n");
    }

    "explode a zip to some directory" in {
      val dir = File.createTempFile("zip-tst", "dir");
      dir.delete();
      dir.mkdir();
      try {
        zip.explode(dir);
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

    "throw an IllegalArgumentException if the target directory exists but is not a directory" in {
    }

    "add a file at the root level of a zip" in {
    }

    "add a file into some directory within a zip" in {
    }

    "add a directory at the root level of a zip" in {
    }

    "throw an IllegalArgumentException if the path supplied does not point to either the root level or some directory of the zip" in {
    }
  }
}
