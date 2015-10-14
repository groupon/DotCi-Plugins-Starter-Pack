package com.groupon.jenkins.dotci.patch;

public class PatchLine {
    public int getLineNo() {
        return lineNo;
    }

    private int lineNo;

    public int getPos() {
        return pos;
    }

    private int pos;

    public PatchLine(int lineNo, int pos) {
        this.lineNo = lineNo;
        this.pos = pos;
    }
}
