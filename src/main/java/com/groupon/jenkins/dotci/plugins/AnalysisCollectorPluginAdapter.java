package com.groupon.jenkins.dotci.plugins;

import com.google.common.base.*;
import com.google.common.collect.*;
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
import org.wickedsource.diffparser.api.model.*;

import java.io.*;
import java.util.*;

@Extension
public class AnalysisCollectorPluginAdapter extends DotCiPluginAdapter {
    public AnalysisCollectorPluginAdapter() {
        super("review_line_comments", null);
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
                CoberturaBuildAction cobeturaAction = dynamicBuild.getAction(CoberturaBuildAction.class);
                CoverageResult coberturaResult = cobeturaAction == null ? null : cobeturaAction.getResult();
                PrintStream logger = listener.getLogger();
                for (PatchFile file : patchFiles) {
                    String fileName = file.getFilename();
                    for (PatchHunk hunk : file.getHunks()) {
                        if(coberturaResult!=null){
                            LineComment coverageComment = coverageComment(hunk,fileName,coberturaResult);
                            if(coverageComment !=null){
                                lineComments.add(coverageComment);
                            }
                        }
                        List<LineComment> analysisComments = getAnalysisComments(anaylsisResult, fileName, hunk);
                        lineComments.addAll(analysisComments);
                    }

                }
                GHRepository repo = new GithubRepositoryService(dynamicBuild.getGithubRepoUrl()).getGithubRepository();
                GHPullRequest pullRequest = repo.getPullRequest(prNumber);
                for(LineComment comment : lineComments){

                    logger.println("Commenting on " + comment.line.getLineNo() + " at Pos: " + comment.line.getPos());
                    pullRequest.createReviewComment(comment.comment, pullRequest.getHead().getSha(), comment.fileName, comment.line.getPos());
                }

                if(anaylsisResult.getNewWarnings().size() > 0){
                    String warningMessage = String.format("Analysis Summary: \n\n * Added:  __%s__\n * Fixed: __%s__ \n * Total: __%s__", anaylsisResult.getNumberOfNewWarnings(), anaylsisResult.getNumberOfFixedWarnings(), anaylsisResult.getNumberOfWarnings());
                    pullRequest.comment(warningMessage);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private List<LineComment> getAnalysisComments(AnalysisResult anaylsisResult, String fileName, PatchHunk hunk) {
        List<LineComment> lineComments = new ArrayList<LineComment>();
        for (PatchLine line : hunk.getLines()) {
            FileAnnotation annotation = findAnnotation(anaylsisResult, fileName, line.getLineNo());
            if (annotation != null) {
                String message = annotation.getMessage();
                lineComments.add(new LineComment(line,message,fileName));
            }
        }
        return lineComments;
    }

    private LineComment coverageComment(PatchHunk hunk, String fileName, CoverageResult coverageResult) {
        Map<String, CoveragePaint> paintedSources = coverageResult.getPaintedSources();
        for(String coverageFileName: paintedSources.keySet()){
            if(fileName.contains(coverageFileName)){
                CoveragePaint coverage = paintedSources.get(coverageFileName);
                List<String> unCoveredLines = new ArrayList<String>();
                for(PatchLine line:hunk.getLines()){
                   if(coverage.getHits(line.getLineNo()) == 0) unCoveredLines.add(line.getLineNo()+"");
                }
                if(unCoveredLines.size() > 0){
                   String message = "Missing coverage for line(s) :  ```"+ Joiner.on(" ").join(unCoveredLines)+"```";
                   return new LineComment(Iterables.getLast(hunk.getLines()),message,fileName) ;
                }
            }

        }
        return null;
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
