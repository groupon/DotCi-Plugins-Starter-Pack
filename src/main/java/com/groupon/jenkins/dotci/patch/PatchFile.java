package com.groupon.jenkins.dotci.patch;

import java.util.*;

public class PatchFile {
    private String filename;


    private List<PatchHunk> hunks;

    public PatchFile(String filename) {
        this.filename = filename;
        hunks = new ArrayList<PatchHunk>();
    }

    public void add(PatchHunk patchHunk) {
        hunks.add(patchHunk);
    }
    public List<PatchHunk> getHunks() {
        return hunks;
    }

    public String getFilename() {
        return filename;
    }
}
