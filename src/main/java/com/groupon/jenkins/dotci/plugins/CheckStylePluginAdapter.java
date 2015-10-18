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
import com.groupon.jenkins.dotci.patch.*;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.execution.*;
import com.groupon.jenkins.github.services.*;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.analysis.util.model.*;
import hudson.plugins.checkstyle.*;
import org.kohsuke.github.*;

import java.io.*;
import java.util.*;

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
        boolean result =false ;
        try {
            result = publisher.perform(((AbstractBuild) dynamicBuild), launcher, listener);
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        }
        if(wantsLineComments() && dynamicBuild.isPullRequest()){
            int prNumber = Integer.parseInt(dynamicBuild.getCause().getPullRequestNumber());
            List<PatchFile> patchFiles = new PatchParser(listener).getLines(dynamicBuild.getGithubRepoUrl(), prNumber);
            CheckStyleResult checkStyleResult = dynamicBuild.getAction(CheckStyleResultAction.class).getResult();
            PrintStream logger = listener.getLogger();
            GHRepository repo = new GithubRepositoryService(dynamicBuild.getGithubRepoUrl()).getGithubRepository();
            try {
                GHPullRequest pullRequest = repo.getPullRequest(prNumber);
                for (PatchFile file : patchFiles) {
                    String fileName = file.getFilename();
                    for (PatchHunk hunk : file.getHunks()) {
                        for (PatchLine line : hunk.getLines()) {
                            FileAnnotation annotation = findAnnotation(checkStyleResult, fileName, line.getLineNo());
                            logger.println(line.getLineNo() + " : ");
                            if (annotation != null) {
                                logger.println(line.getLineNo() + " : " + annotation );
                                String message = annotation.getMessage();
                                logger.println("Commeting on " + line.getLineNo() + " at Pos: " + line.getPos());
                                pullRequest.createReviewComment(message, pullRequest.getHead().getSha(), file.getFilename(), line.getPos());
                            }
                        }

                    }

                }
                String warningMessage = String.format("Checkstyle Summary: \n\n * Added:  __%s__\n * Fixed: __%s__ \n * Total: __%s__", checkStyleResult.getNumberOfNewWarnings(), checkStyleResult.getNumberOfFixedWarnings(), checkStyleResult.getNumberOfWarnings());
                pullRequest.comment(warningMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private boolean wantsLineComments() {
        return options != null && options instanceof Map && ((Map)options).containsKey("line_comments");
    }

    private FileAnnotation findAnnotation(CheckStyleResult checkStyleResult, String fileName, int lineNo) {
        for(FileAnnotation annotation: checkStyleResult.getAnnotations()){
            if(annotation.getFileName().endsWith(fileName) ){
                if(annotation.getPrimaryLineNumber() == lineNo){
                    return annotation;
                }
//                for(LineRange range : annotation.getLineRanges()){
////                   if(range.getStart() <= lineNo && range.getEnd() < lineNo){
////                       return annotation;
////                   }
//                }
            }
        }
        return null;
    }
}
