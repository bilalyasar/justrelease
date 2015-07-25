package com.justrelease.project.type;


import org.apache.commons.cli.CommandLine;

public class GruntProject extends AbstractProjectInfo implements ProjectInfo {

    public GruntProject(CommandLine cmd) {
        this.cmd = cmd;
    }

    public String getVersion() {
        return null;
    }
}
