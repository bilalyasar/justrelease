package com.justrelease.project.type;

/**
 * Created by bilal on 25/07/15.
 */
public interface ProjectInfo {

    String getVersion();

    void setup() throws Exception;
    
    String getLocalDirectory();
    
    String getCurrentVersion();
    
    String getRepoUrl();

    String getName();
    
    String getPassword();

}
