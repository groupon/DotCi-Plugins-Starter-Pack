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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.FindBugsPublisher;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.buildconfiguration.plugins.DotCiPluginAdapter;

@Extension
public class FindbugsPluginAdapter extends DotCiPluginAdapter {

	public FindbugsPluginAdapter() {
		super("findbugs", "**/findbugsXml.xml");
	}

	@Override
	public boolean perform(DynamicBuild build, Launcher launcher, BuildListener listener) {
		FindBugsPublisher publisher = new FindBugsPublisher("", "", "low", "", false, "", "", "", "", null, null, null, null, "", "", "", "", "", null, null, null, false, false, false, pluginInputFiles, false, false);
		try {
			return publisher.perform(((AbstractBuild) build), launcher, listener);
		} catch (Exception e) {
			return false;
		}
	}

}
