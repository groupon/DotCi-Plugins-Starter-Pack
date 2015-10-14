package com.groupon.jenkins.dotci.patch;

import com.groupon.jenkins.github.services.*;
import org.kohsuke.github.*;
import org.wickedsource.diffparser.api.*;
import org.wickedsource.diffparser.api.model.*;

import java.io.*;
import java.util.*;

public class PatchParser {
    public  List<PatchFile> getLines(String repoUrl,int prNumber){
        GHRepository repo = new GithubRepositoryService(repoUrl).getGithubRepository();
        List<PatchFile> files = new ArrayList<PatchFile>();

        try {
            DiffParser parser = new UnifiedDiffParser();
            for(GHPullRequestFileDetail file : repo.getPullRequest(prNumber).listFiles()){
                PatchFile patchFile = new PatchFile(file.getFilename());
                String patch = "--- /file/path \n +++ /file/path_new\n"+ file.getPatch() + "\n";
                List<Diff> diffs = parser.parse(patch.getBytes());
                for(Diff diff : diffs){
                    for(Hunk hunk: diff.getHunks()){
                        PatchHunk patchHunk = new PatchHunk(hunk.getToFileRange());
                        patchFile.add(patchHunk);
                        int lineNo = hunk.getToFileRange().getLineStart();

                        for(int i=0; i < hunk.getLines().size() ; i++){
                            Line line = hunk.getLines().get(i);
                            if(line.getLineType().equals(Line.LineType.NEUTRAL)){
                                lineNo= lineNo + 1;
                            }
                            if(line.getLineType().equals(Line.LineType.TO)){
                                patchHunk.add(new PatchLine(lineNo,i+1));
                                lineNo= lineNo + 1;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
      return files;
    }
}

