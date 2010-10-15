/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
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

import java.util.regex.Pattern
import java.util.zip.ZipFile

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
  }
}
