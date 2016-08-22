/*
   The MIT License (MIT)

   Copyright (c) 2014, Groupon, Inc.

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
   */
package com.groupon.jenkins.dotci.plugins;

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;

import com.google.common.base.Joiner;

import java.util.List;

public abstract class FileBasedPluginAdapter extends DotCiPluginAdapter {
  protected FileBasedPluginAdapter(String name, String pluginInputFiles) {
    super(name, pluginInputFiles);
  }

  protected FileBasedPluginAdapter(String name) {
    super(name);
  }

  // Example with junit
  //
  // plugins:
  //   - junit
  //
  // OR
  //
  // plugins:
  //   - junit: "**/dir1/*.ext1,dir2/*.ext2"
  //
  // OR
  //
  // plugins:
  //   - junit:
  //     - **/dir1/*.ext1
  //     - dir2/*.ext2
  //
  @Override
  public String getPluginInputFiles() {
    // If we have options override pluginInputFiles potentially
    if (options instanceof String) {
      // options is string, just return
      return (String)options;
    } else if (options instanceof List) {
      return Joiner.on(",").join((List)options);
    } else {
      return pluginInputFiles;
    }
  }
}
