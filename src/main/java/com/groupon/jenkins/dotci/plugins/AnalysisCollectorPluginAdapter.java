package com.groupon.jenkins.dotci.plugins;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dotci.patch.PatchFile;
import com.groupon.jenkins.dotci.patch.PatchHunk;
import com.groupon.jenkins.dotci.patch.PatchLine;
import com.groupon.jenkins.dotci.patch.PatchParser;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.analysis.collector.AnalysisPublisher;
import hudson.plugins.analysis.collector.AnalysisResultAction;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.targets.CoveragePaint;
import hudson.plugins.cobertura.targets.CoverageResult;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Extension
public class AnalysisCollectorPluginAdapter extends DotCiPluginAdapter {
    public AnalysisCollectorPluginAdapter() {
        super("review_line_comments", null);
    }

    @Override
    public boolean perform(final DynamicBuild dynamicBuild, final Launcher launcher, final BuildListener listener) {
        try {
            new AnalysisPublisher().perform(((AbstractBuild) dynamicBuild), launcher, listener);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        if (dynamicBuild.isPullRequest()) {
            try {
                final int prNumber = Integer.parseInt(dynamicBuild.getCause().getPullRequestNumber());
                final List<PatchFile> patchFiles = new PatchParser(listener).getLines(dynamicBuild.getGithubRepoUrl(), prNumber);
                final List<LineComment> lineComments = new ArrayList<>();
                final AbstractResultAction action = dynamicBuild.getAction(AnalysisResultAction.class);
                if (action == null) return true;
                final BuildResult anaylsisResult = action.getResult();
                final CoberturaBuildAction cobeturaAction = dynamicBuild.getAction(CoberturaBuildAction.class);
                final CoverageResult coberturaResult = cobeturaAction == null ? null : cobeturaAction.getResult();
                final PrintStream logger = listener.getLogger();
                for (final PatchFile file : patchFiles) {
                    final String fileName = file.getFilename();
                    for (final PatchHunk hunk : file.getHunks()) {
                        if (coberturaResult != null) {
                            final LineComment coverageComment = coverageComment(hunk, fileName, coberturaResult);
                            if (coverageComment != null) {
                                lineComments.add(coverageComment);
                            }
                        }
                        final List<LineComment> analysisComments = getAnalysisComments(anaylsisResult, fileName, hunk);
                        lineComments.addAll(analysisComments);
                    }

                }
                final GHRepository repo = new GithubRepositoryService(dynamicBuild.getGithubRepoUrl()).getGithubRepository();
                final GHPullRequest pullRequest = repo.getPullRequest(prNumber);
                final PagedIterable<GHPullRequestReviewComment> allReviewComments = pullRequest.listReviewComments();
                for (final LineComment comment : lineComments) {
                    makeComment(allReviewComments, logger, pullRequest, comment);
                }

            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private void makeComment(final PagedIterable<GHPullRequestReviewComment> allReviewComments, final PrintStream logger, final GHPullRequest pullRequest, final LineComment comment) throws IOException {
        final GHPullRequestReviewComment existingComment = Iterables.find(allReviewComments, new Predicate<GHPullRequestReviewComment>() {
            @Override
            public boolean apply(final GHPullRequestReviewComment reviewComment) {
                return comment.isSameAs(reviewComment);
            }
        }, null);
        if (existingComment == null) {
            logger.println("Commenting on " + comment.line.getLineNo() + " at Pos: " + comment.line.getPos());
            pullRequest.createReviewComment(comment.comment, pullRequest.getHead().getSha(), comment.fileName, comment.line.getPos());
        }
    }

    private List<LineComment> getAnalysisComments(final BuildResult anaylsisResult, final String fileName, final PatchHunk hunk) {
        final List<LineComment> lineComments = new ArrayList<>();
        for (final PatchLine line : hunk.getLines()) {
            final FileAnnotation annotation = findAnnotation(anaylsisResult, fileName, line.getLineNo());
            if (annotation != null) {
                final String message = annotation.getMessage();
                lineComments.add(new LineComment(line, message, fileName));
            }
        }
        return lineComments;
    }

    private LineComment coverageComment(final PatchHunk hunk, final String fileName, final CoverageResult coverageResult) {
        final Map<String, CoveragePaint> paintedSources = coverageResult.getPaintedSources();
        for (final String coverageFileName : paintedSources.keySet()) {
            if (fileName.contains(coverageFileName)) {
                final CoveragePaint coverage = paintedSources.get(coverageFileName);
                final List<String> unCoveredLines = new ArrayList<>();
                for (final PatchLine line : hunk.getLines()) {
                    if (coverage.getHits(line.getLineNo()) == 0)
                        unCoveredLines.add(line.getLineNo() + "");
                }
                if (unCoveredLines.size() > 0) {
                    final String message = "Missing coverage for line(s) :  ```" + Joiner.on(" ").join(unCoveredLines) + "```";
                    return new LineComment(Iterables.getLast(hunk.getLines()), message, fileName);
                }
            }

        }
        return null;
    }

    private FileAnnotation findAnnotation(final BuildResult buildResult, final String fileName, final int lineNo) {
        for (final FileAnnotation annotation : buildResult.getAnnotations()) {
            if (annotation.getFileName().endsWith(fileName)) {
                if (annotation.getPrimaryLineNumber() == lineNo) {
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

    private static class LineComment {
        public PatchLine line;
        public String comment;
        public String fileName;

        public LineComment(final PatchLine line, final String comment, final String fileName) {
            this.line = line;
            this.comment = comment;
            this.fileName = fileName;
        }

        public boolean isSameAs(final GHPullRequestReviewComment reviewComment) {
            return reviewComment.getPosition() == this.line.getPos()
                    && this.comment.equals(reviewComment.getBody())
                    && this.fileName.equals(reviewComment.getPath());
        }
    }
}
