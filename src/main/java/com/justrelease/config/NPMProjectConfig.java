package com.justrelease.config;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;

public class NPMProjectConfig extends AbstractProjectConfig {

    public NPMProjectConfig(InputStream projectConfigurationIS, InputStream justreleaseConfigIS, ReleaseConfig releaseConfig) throws Exception {
        super(projectConfigurationIS, justreleaseConfigIS, releaseConfig);
    }

    @Override
    public void readCurrentVersion() {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new InputStreamReader(projectConfigurationIS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;

        this.currentVersion = (String) jsonObject.get("version");
    }

}
