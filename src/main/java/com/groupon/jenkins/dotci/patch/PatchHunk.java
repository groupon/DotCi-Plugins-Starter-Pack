package com.groupon.jenkins.dotci.patch;

import org.wickedsource.diffparser.api.model.*;

import java.util.*;

public class PatchHunk {
    public Range getFileRange() {
        return fileRange;
    }

    private Range fileRange;
    private List<PatchLine> lines ;

    public PatchHunk(Range fileRange) {
        this.fileRange = fileRange;
        this.lines = new ArrayList<PatchLine>();
    }

    public void add(PatchLine patchLine) {
        lines.add(patchLine);
    }
}
