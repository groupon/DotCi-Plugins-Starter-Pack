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
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.pmd.PmdPublisher;

@Extension
public class PmdPluginAdapter extends DotCiPluginAdapter
{
    private static final String UNSTABLE_TOTAL_THRESHOLD="100";
    private static final String FAILED_TOTAL_THRESHOLD="200";
    private static final String UNSTABLE_NEW_THRESHOLD="25";
    private static final String FAILED_NEW_THRESHOLD="200";

    public PmdPluginAdapter()
    {
        super("pmd", "**/pmd.xml");
    }

    @Override
    public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener)
    {
        PmdPublisher publisher = new PmdPublisher("0",
                                                  "0",
                                                  "normal",
                                                  "UTF-8",
                                                  true,
                                                  UNSTABLE_TOTAL_THRESHOLD,
                                                  "0",
                                                  "0",
                                                  UNSTABLE_TOTAL_THRESHOLD,
                                                  UNSTABLE_NEW_THRESHOLD,
                                                  "0",
                                                  "0",
                                                  UNSTABLE_NEW_THRESHOLD,
                                                  FAILED_TOTAL_THRESHOLD,
                                                  "0",
                                                  "0",
                                                  FAILED_TOTAL_THRESHOLD,
                                                  FAILED_NEW_THRESHOLD,
                                                  "0",
                                                  "0",
                                                  FAILED_NEW_THRESHOLD,
                                                  true,
                                                  false,
                                                  true,
                                                  true,
                                                  pluginInputFiles);

        try {
            return publisher.perform(((AbstractBuild<?,?>) dynamicBuild), launcher, listener);
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return false;
        }
    }
}
