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
package com.groupon.jenkins.dotci.listeners;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import hudson.plugins.ansicolor.AnsiColorBuildWrapper;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;

import java.io.IOException;

import com.groupon.jenkins.dynamic.build.DynamicProject;

@Extension
public class DotCiProjectSetupListener extends ItemListener {

	@Override
	public void onCreated(Item item) {
		if (item instanceof DynamicProject) {
			DynamicProject project = (DynamicProject) item;
			try {
				project.getBuildWrappersList().add(new BuildTimeoutWrapper(60, true, true, 0, 0, "absolute"));
				project.getBuildWrappersList().add(new AnsiColorBuildWrapper("xterm"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
