package com.justrelease.project.type;

import com.jcraft.jsch.Session;
import org.apache.commons.cli.CommandLine;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportHttp;

/**
 * Created by bilal on 25/07/15.
 */
public abstract class AbstractProjectInfo {
    String repo = "";
    String localDirectory = "release";
    public String currentVersion = "";
    String name = "";
    String password = "";
    public CommandLine cmd;


    public void setup() throws Exception {
        if (cmd.hasOption("name")) {
            name = cmd.getOptionValue("name");
        }
        if (cmd.hasOption("password")) {
            password = cmd.getOptionValue("password");
        }
        if (cmd.hasOption("repo")) {
            repo = createGithubUrl(cmd.getOptionValue("repo"));
            System.out.println("repo url:" + repo);
        }
        if (cmd.hasOption("localDirectory")) {
            localDirectory = cmd.getOptionValue("localDirectory");
        }
        System.out.println("local directory:" + localDirectory);

        if (cmd.hasOption("c")) {
            currentVersion = cmd.getOptionValue("c");
        } else {
            currentVersion = getVersion();
        }
        // options end, now we have necessary infos

    }

    protected abstract String getVersion();

    private String createGithubUrl(String repo) {
        if (cmd.hasOption("username") && cmd.hasOption("password"))
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

    public String getLocalDirectory() {
        return localDirectory;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getRepoUrl() {
        return repo;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
