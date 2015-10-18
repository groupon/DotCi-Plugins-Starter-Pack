package com.groupon.jenkins.dotci.patch;

import com.google.common.base.*;
import com.groupon.jenkins.github.services.*;
import hudson.model.*;
import org.kohsuke.github.*;
import org.wickedsource.diffparser.api.*;
import org.wickedsource.diffparser.api.model.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class PatchParser {
    private final PrintStream logger;

    public PatchParser(BuildListener listener) {
        this.logger = listener.getLogger();
    }

    public  List<PatchFile> getLines(String repoUrl,int prNumber){
        GHRepository repo = new GithubRepositoryService(repoUrl).getGithubRepository();
        List<PatchFile> files = new ArrayList<PatchFile>();

        try {
            DiffParser parser = new UnifiedDiffParser();
            for(GHPullRequestFileDetail file : repo.getPullRequest(prNumber).listFiles()){
                PatchFile patchFile = new PatchFile(file.getFilename());
                files.add(patchFile);
                String fixPatch = fixPatch(file.getPatch());
                printPatches(file, fixPatch);

                String patch = "--- /file/path \n +++ /file/path_new\n"+ fixPatch;
                List<Diff> diffs = parser.parse(patch.getBytes());
                for(Diff diff : diffs){
                    int pos =0;
                    for(Hunk hunk: diff.getHunks()){
                        PatchHunk patchHunk = new PatchHunk(hunk.getToFileRange());
                        patchFile.add(patchHunk);
                        int lineNo = hunk.getToFileRange().getLineStart();

                        for(Line line: hunk.getLines()){
                            pos = pos+1;
                            if(line.getLineType().equals(Line.LineType.NEUTRAL)){
                                lineNo= lineNo + 1;
                            }
                            if(line.getLineType().equals(Line.LineType.TO)){
                                patchHunk.add(new PatchLine(lineNo,pos));
                                lineNo= lineNo + 1;
                            }
                        }
                        pos = pos + 1; //Increment pos for each new hunk
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
      return files;
    }

    private void printPatches(GHPullRequestFileDetail file, String fixPatch) {
        logger.println("--------UnFixed Patch------------");
        logger.println(file.getPatch());
        logger.println("--------UnFixed Patch------------");

        logger.println("--------Fixed Patch------------");
        logger.println(fixPatch);
        logger.println("--------Fixed Patch------------");
    }

    private String fixPatch(String patch){
        String[] lines = patch.split("\n");
        Pattern hunkStartPatternExtra = Pattern.compile("^(@@.*@@)(.+)");
        Pattern hunkStartPatternLegit = Pattern.compile("^(@@.*@@)");
        List fixedLines = new ArrayList<String>() ;
        for(int i=0 ; i< lines.length ; i++){
            Matcher matcher = hunkStartPatternExtra.matcher(lines[i].trim());
            if(matcher.matches()){
                fixedLines.add(matcher.group(1)) ;
//                fixedLines.add(" "+matcher.group(2)) ;
            }else{
                Matcher legitMatcher = hunkStartPatternLegit.matcher(lines[i].trim());
                if(legitMatcher.matches() && fixedLines.size() >0){//Add new line between hunks
                    fixedLines.add("\n");
                }
               fixedLines.add(lines[i]) ;
            }
        }
        fixedLines.add("\n");
        return Joiner.on("\n").join(fixedLines);
    }
}

