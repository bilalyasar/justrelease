package com.justrelease.project.type;

import com.jcraft.jsch.Session;
import com.justrelease.config.GithubRepo;
import com.justrelease.config.ReleaseConfig;
import org.apache.commons.cli.CommandLine;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportHttp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by bilal on 25/07/15.
 */
public abstract class AbstractProjectInfo {
    ReleaseConfig releaseConfig;

    public CommandLine cmd;


    public void setup() throws Exception {
        if (cmd.hasOption("name")) {
            releaseConfig.setGithubName(cmd.getOptionValue("name"));
        }
        if (cmd.hasOption("password")) {
            releaseConfig.setGithubPassword(cmd.getOptionValue("password"));
        }
        if (cmd.hasOption("repo")) {
            releaseConfig.setMainRepo(cmd.getOptionValue("repo"));
        }
        releaseConfig.getMainRepo().setRepoUrl(createGithubUrl(releaseConfig.getMainRepo().getRepoName()));
        for (GithubRepo githubRepo : releaseConfig.getDependencyRepos()) {
            githubRepo.setRepoUrl(createGithubUrl(githubRepo.getRepoName()));
        }


        System.out.println("repo url:" + releaseConfig.getMainRepo().getRepoUrl());
        if (cmd.hasOption("localDirectory")) {
            releaseConfig.setLocalDirectory(cmd.getOptionValue("localDirectory"));
        }
        System.out.println("local directory:" + releaseConfig.getLocalDirectory());

        if (cmd.hasOption("c")) {
            releaseConfig.setCurrentVersion(cmd.getOptionValue("c"));
            ;
        }
        // options end, now we have necessary infos
    }

    private String createGithubUrl(String repo) {
        if (!releaseConfig.getGithubName().equals("") && !releaseConfig.getGithubPassword().equals(""))
            return String.format("https://github.com/%s.git", repo);
        return String.format("git@github.com:%s.git", repo);
    }

    public static TransportConfigCallback getTransportConfigCallback() {
        final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
            }
        };
        return new TransportConfigCallback() {

            public void configure(Transport transport) {
                if (transport instanceof TransportHttp) return;
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        };
    }

    public void runCommand(String[] cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
