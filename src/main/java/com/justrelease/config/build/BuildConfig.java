package com.justrelease.config.build;

import java.util.ArrayList;

/**
 * Created by bilal on 14/08/15.
 */
public class BuildConfig {
    private ArrayList<ExecConfig> execConfigs = new ArrayList<ExecConfig>();

    public void addExecConfig(ExecConfig execConfig) {
        execConfigs.add(execConfig);
    }

    public ArrayList<ExecConfig> getExecConfigs() {
        return execConfigs;
    }

    public void setExecConfigs(ArrayList<ExecConfig> execConfigs) {
        this.execConfigs = execConfigs;
    }
}
