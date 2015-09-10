package com.justrelease.project.type;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class NPMProject implements ProjectInfo {


    private String localDirectory;

    public NPMProject(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getCurrentVersion() {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(localDirectory + "/package.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;

        String version = (String) jsonObject.get("version");
        return version;

    }

}
