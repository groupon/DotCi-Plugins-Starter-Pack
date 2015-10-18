package com.groupon.jenkins.dotci.plugins;

import com.groupon.jenkins.buildtype.plugins.*;
import com.groupon.jenkins.dotci.patch.*;
import com.groupon.jenkins.dynamic.build.*;
import com.groupon.jenkins.github.services.*;
import hudson.*;
import hudson.model.*;
import hudson.plugins.analysis.collector.*;
import hudson.plugins.analysis.core.*;
import hudson.plugins.analysis.util.model.*;
import hudson.plugins.checkstyle.*;
import hudson.plugins.cobertura.*;
import hudson.plugins.cobertura.targets.*;
import org.kohsuke.github.*;

import java.io.*;
import java.util.*;

@Extension
public class AnalysisCollectorPluginAdapter extends DotCiPluginAdapter {
    public AnalysisCollectorPluginAdapter() {
        super("analysis", null);
    }

    @Override
    public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
        try {
            new AnalysisPublisher().perform(((AbstractBuild)dynamicBuild), launcher, listener);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(  dynamicBuild.isPullRequest()){
            try {
                int prNumber = Integer.parseInt(dynamicBuild.getCause().getPullRequestNumber());
                List<PatchFile> patchFiles = new PatchParser(listener).getLines(dynamicBuild.getGithubRepoUrl(), prNumber);
                List<LineComment> lineComments = new ArrayList<LineComment>();
                AnalysisResult anaylsisResult = dynamicBuild.getAction(AnalysisResultAction.class).getResult();
                CoverageResult coberturaResult = dynamicBuild.getAction(CoberturaBuildAction.class).getResult();
                PrintStream logger = listener.getLogger();
                for (PatchFile file : patchFiles) {
                    String fileName = file.getFilename();
                    for (PatchHunk hunk : file.getHunks()) {
                        for (PatchLine line : hunk.getLines()) {
                            FileAnnotation annotation = findAnnotation(anaylsisResult, fileName, line.getLineNo());
                            logger.println(line.getLineNo() + " : ");
                            if (annotation != null) {
                                logger.println(line.getLineNo() + " : " + annotation );
                                String message = annotation.getMessage();
                                lineComments.add(new LineComment(line,message,fileName));
                            }
                        }

                    }

                }
                GHRepository repo = new GithubRepositoryService(dynamicBuild.getGithubRepoUrl()).getGithubRepository();
                GHPullRequest pullRequest = repo.getPullRequest(prNumber);
                String warningMessage = String.format("Analysis Summary: \n\n * Added:  __%s__\n * Fixed: __%s__ \n * Total: __%s__", anaylsisResult.getNumberOfNewWarnings(), anaylsisResult.getNumberOfFixedWarnings(), anaylsisResult.getNumberOfWarnings());
                for(LineComment comment : lineComments){

                    logger.println("Commenting on " + comment.line.getLineNo() + " at Pos: " + comment.line.getPos());
                    pullRequest.createReviewComment(comment.comment, pullRequest.getHead().getSha(), comment.fileName, comment.line.getPos());
                }
                pullRequest.comment(warningMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
    //    private List<FileAnnotation> findAnnotations( String fileName, int lineNo,BuildResult... buildResults) {
//        List<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
//       for(BuildResult result :  buildResults){
//           FileAnnotation annotation = findAnnotation(result, fileName, lineNo);
//           if(annotation != null) annotations.add(annotation);
//       }
//        return annotations;
//
//    }
    private FileAnnotation findAnnotation(BuildResult buildResult, String fileName, int lineNo) {
        for(FileAnnotation annotation: buildResult.getAnnotations()){
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
    private static class LineComment{
        public PatchLine line;
        public String comment;
        public String fileName;

        public LineComment(PatchLine line, String comment, String fileName) {
            this.line = line;
            this.comment = comment;
            this.fileName = fileName;
        }
    }
}
