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

import org.apache.commons.io.FileUtils
import org.specs._
import org.specs.runner.JUnit4

import org.beeherd.io.TempDir

class ArchiveSpecTest extends JUnit4(ArchiveSpec)
object ArchiveSpec extends Specification {

  "An Archive" should {
    "handle Zip files" in {
      "that end in .zip" in {
        "list entries" in {
          val file = new File(getClass.getResource("/foo.zip").getFile);
          val archive = new Archive(file);
          val entries = archive.entryNames;
          entries must haveSize(6);
          entries.head must beEqual("dir1/");
          entries.last must beEqual("foo.txt");
        }
      }
    }

    "handle TarGz files" in {
      "that end in .tar.gz" in {
        "list entries" in {
          val file = new File(getClass.getResource("/foo.tar.gz").getFile);
          val archive = new Archive(file);
          val entries = archive.entryNames;
          entries must haveSize(6);
          entries.head must beEqual("dir1/");
          entries.last must beEqual("foo.txt");
        }
      }
    }

    "handle Tar files" in {
      "that end in .tar" in {
        "list entries" in {
          val file = new File(getClass.getResource("/foo.tar").getFile);
          val archive = new Archive(file);
          val entries = archive.entryNames;
          entries must haveSize(6);
          entries.head must beEqual("dir1/");
          entries.last must beEqual("foo.txt");
        }
      }
    }
  }

  "The Archive object" should {
    "explode a archived files to some directory" in {
      "zip" in {
        val file = new File(getClass.getResource("/foo.zip").getFile);
        TempDir.use[Unit] {dir =>
          Archive.explode(file, dir);
          val f = new File(dir, "foo.txt");
          f must exist;
          f must beFile;
          FileUtils.readFileToString(f) must beEqual("hi from foo\n");
          new File(dir, "dir2") must beDirectory;
        }
      }

      "tar.gz" in {
        val file = new File(getClass.getResource("/foo.tar.gz").getFile);
        TempDir.use[Unit] {dir =>
          Archive.explode(file, dir);
          val f = new File(dir, "foo.txt");
          f must exist;
          f must beFile;
          FileUtils.readFileToString(f) must beEqual("hi from foo\n");
          new File(dir, "dir2") must beDirectory;
        }
      }

      "tar" in {}
    }
  }
}
