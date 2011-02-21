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
package org.beeherd.archive.cli

import java.io.File

import org.apache.commons.cli.{BasicParser, OptionGroup => OptGroup,
Option => Opt, Options => Opts, OptionBuilder => OptBuilder, ParseException, 
HelpFormatter, CommandLine}

import org.beeherd.archive.Zip

/**
* CLI for adding files to an existing zip.
*/
object UpdateZip {
  def main(args: Array[String]): Unit = {
    val opts = new Opts();

    def addRequired(name: String, desc: String): Unit = {
      val opt = new Opt(name, true, desc);
      opt.setRequired(true);
      opts.addOption(opt);
    }

    addRequired("z", "The zip to be updated");
    addRequired("p", "The path to a directory within the zip to which the files will be added");

    val opt = new Opt("f", true, "The files to add.");
    opt.setRequired(true);
    opt.setArgs(100);
    opts.addOption(opt);

    val parser = new BasicParser();
    try {
      val cmd = parser.parse(opts, args);

      val zip = new File(cmd.getOptionValue("z"));
      println(zip.getAbsolutePath)
      val path = cmd.getOptionValue("p");
      val files = cmd.getOptionValues("f").map { new File(_) }

      Zip.add(zip, path, files:_*);
    } catch {
      case pe: ParseException => usage(opts)
      case e:Exception => e.printStackTrace
    }
  }

  private def usage(opts: Opts): Unit = {
    val formatter = new HelpFormatter();
    formatter.printHelp("UpdateZip", opts);
  }
}
