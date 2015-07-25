package com.justrelease.project.type;

import org.apache.commons.cli.CommandLine;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileInputStream;

/**
 * Created by bilal on 25/07/15.
 */
public class MavenProject extends AbstractProjectInfo implements ProjectInfo {

    public MavenProject(CommandLine cmd) {
        this.cmd = cmd;
    }

    public String getVersion() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model result = null;
        try {
            result = reader.read(new FileInputStream(localDirectory + "/pom.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.getVersion();
    }
}
