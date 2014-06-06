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
import hudson.plugins.checkstyle.CheckStylePublisher;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.buildconfiguration.plugins.DotCiPluginAdapter;

@Extension
public class CheckStylePluginAdapter extends DotCiPluginAdapter {

	public CheckStylePluginAdapter() {
		super("checkstyle", "**/checkstyle-result.xml");
	}

	@Override
	public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {

		/*
		 * public CheckStylePublisher(final String healthy, final String
		 * unHealthy, final String thresholdLimit, final String defaultEncoding,
		 * final boolean useDeltaValues, final String unstableTotalAll, final
		 * String unstableTotalHigh, final String unstableTotalNormal, final
		 * String unstableTotalLow, final String unstableNewAll, final String
		 * unstableNewHigh, final String unstableNewNormal, final String
		 * unstableNewLow, final String failedTotalAll, final String
		 * failedTotalHigh, final String failedTotalNormal, final String
		 * failedTotalLow, final String failedNewAll, final String
		 * failedNewHigh, final String failedNewNormal, final String
		 * failedNewLow, final boolean canRunOnFailed, final boolean
		 * useStableBuildAsReference, final boolean shouldDetectModules, final
		 * boolean canComputeNew, final String pattern)
		 */
		CheckStylePublisher publisher = new CheckStylePublisher(null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, pluginInputFiles);
		try {
			return publisher.perform(((AbstractBuild) dynamicBuild), launcher, listener);
		} catch (Exception e) {
			e.printStackTrace(listener.getLogger());
			return false;
		}
	}

}
