package com.justrelease;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

public class Cli {
    private static final Logger log = Logger.getLogger(Cli.class.getName());
    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) throws VersionParseException {


        this.args = args;

        options.addOption("repo", true, "repo url");
        options.addOption("localDirectory", true, "local source repository directory");
        options.addOption("name", true, "github username");
        options.addOption("password", true, "github password");
        options.addOption("c", true, "current snapshot version");
        options.addOption("h", false, "help");

    }

    public void parse() {
        CommandLineParser parser = new BasicParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            String repo="";
            String localDirectory="";
            String currentVersion="";
            String name="";
            String password="";


            if (cmd.hasOption("h")) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("OptionsTip", options);
            }

            if (cmd.hasOption("name")) {
                name=cmd.getOptionValue("name");
            }
            if (cmd.hasOption("password")) {
                password=cmd.getOptionValue("password");
            }
            if (cmd.hasOption("repo")) {
                repo=cmd.getOptionValue("repo");
                System.out.println("repo url:"+cmd.getOptionValue("repo"));
            }
            if (cmd.hasOption("localDirectory")) {
                localDirectory=cmd.getOptionValue("localDirectory");
                System.out.println("local directory:"+cmd.getOptionValue("localDirectory"));
            }
            DefaultVersionInfo versionInfo = null;


            if (cmd.hasOption("c")) {
                currentVersion=cmd.getOptionValue("c");
                versionInfo = new DefaultVersionInfo(currentVersion);
                System.out.println("current version:"+versionInfo);
            }
                String releaseVersion=versionInfo.getReleaseVersionString();
                System.out.println("releasing to the version:"+releaseVersion);

                String nextVersion=versionInfo.getNextVersion().getSnapshotVersionString();
                System.out.println("updating to the next version:"+nextVersion);


            FileUtils.deleteDirectory(new File(localDirectory));

            CredentialsProvider cp =
                    new UsernamePasswordCredentialsProvider(name, password);
            Git.cloneRepository()
                    .setURI(repo)
                    .setDirectory(new File(localDirectory))
                    .setCredentialsProvider(cp)
                    .call();

            Iterator it = FileUtils.iterateFiles(new File(localDirectory), null, false);
            while(it.hasNext()){
                File f = (File)it.next();
                String content = FileUtils.readFileToString(f);
                FileUtils.writeStringToFile(f, content.replaceAll(currentVersion, releaseVersion));
            }

            Git git = Git.open(new File(localDirectory));

            git.add().addFilepattern(".").call();
            git.commit().setCommitter("justrelease", "info@justrelease.com").setMessage(releaseVersion).call();
            git.tag().setName(releaseVersion).call(); 
            it = FileUtils.iterateFiles(new File(localDirectory), null, false);

            while(it.hasNext()){
                File f = (File)it.next();
                String content = FileUtils.readFileToString(f);
                FileUtils.writeStringToFile(f, content.replaceAll(releaseVersion, nextVersion));
            }
            git.commit().setCommitter("justrelease","info@justrelease.com").setMessage(nextVersion).call();
            git.push().setCredentialsProvider(cp).call();
            git.push().setPushTags().setCredentialsProvider(cp).call();


        } catch (Exception e) {
            System.out.println(e);
            help();
        }
    }

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("JustRelease", options);
        System.exit(0);
    }
}