package com.justrelease.project.type;

/**
 * Created by bilal on 25/07/15.
 */
public interface ProjectInfo {

    String getCurrentVersion();

    void setup() throws Exception;

    void createArtifacts();

}
