
#JustRelease

[![Join the chat at https://gitter.im/justrelease/justrelease](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/justrelease/justrelease?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/justrelease/justrelease.svg?branch=master)](https://travis-ci.org/justrelease/justrelease)
[![codecov.io](http://codecov.io/github/justrelease/justrelease/coverage.svg?branch=master)](http://codecov.io/github/justrelease/justrelease?branch=master)

- [Installation](#installation)
  - [OSX Installation](#osx-installation)
  - [Default Installation](#default-installation-works-all-operating-systems)
- [JustRelease Maven Plugin](#justrelease-maven-plugin) :new:
- [Configuring Credentials](#configuring-credentials) 
- [Quick Example](#quick-example)
- [Usage](#usage)
- [Advanced Configuration](#advanced-configuration)
  - [Version Update](#version-update)
  - [Create Artifacts](#create-artifacts)
  - [Publish](#publish)
  - [Project Variables](#project-variables)
  - [Release Notes Generator](#release-notes-generator)
- [How to Use JustRelease Library](#how-to-use-justrelease-library)
  - [Most Simple Usage](#most-simple-usage)
  - [Giving Release Type](#giving-release-type)
  - [DryRun Config](#dryrun-config)


JustRelease is command line release tool for software libraries hosted on github.com. Justrelease requires no configuration and applies default behavior in each release operation. If you want to customize some of the features offered by justrelease, please include `justrelease.yml` file in your repository.

##Installation

##### OSX Installation
- `brew install justrelease/justrelease/justrelease`

##### Default Installation (Works All Operating Systems)
- Download and unzip [latest zip file](https://github.com/justrelease/justrelease/releases)

## JustRelease Maven Plugin

Add your pom.xml following plugin repository.
```
        <pluginRepository>
            <id>repo</id>
            <url>https://github.com/justrelease/maven-repository/raw/master</url>
        </pluginRepository>
```
Define JustRelease Plugin as follows:

```
            <plugin>
                <groupId>com.justrelease</groupId>
                <artifactId>justrelease-maven-plugin</artifactId>
                <version>1.1.3</version>
                <configuration>
                    <github>username/reponame</github>
                </configuration>
            </plugin>
```

Run plugin with this command:

`mvn justrelease:goal`

goal = patch | minor | major

If you want to enable `dryRun` you need to pass `-DdryRun=true` argument.

## Configuring Credentials

- Generate [Personal Access Token](https://github.com/settings/tokens)
- Create ~/.github file and set *login* and *oauth* parameters as below
```
login=github_username
oauth=GITHUB_OAUTH_TOKEN
```

##Quick Example

- fork [sample repository](https://github.com/justrelease/justrelease-sample-npm)
- update releasenotes.md file and commit the change.
- run `justrelease yourusername/justrelease-sample-npm patch`
- check releases page of your forked repository

##Usage

```
$ justrelease

Thanks for using justrelease 1.0.2!

usage: justrelease <username/repository> <major|minor|patch|X.Y.Z>
 -dryRun                  release without push
 -h,--help                nelp
 -snapshotVersion <arg>   version number that will be updated after the
                          release. maven spesific feature
 -v,--version             Print the version of the application
```



## Advanced Configuration

To configure your release steps you need to create `justrelease.yml` file in your repo.
If you don't create a `justrelease.yml` file, default configurations will be used.

This is an example `justrelease.yml` file:

```
version.update:
    - xml
create.artifacts:
        - mvn clean install
publish:
        - github:
            - description:releasenotes.md
            - attachment:target/justrelease-${version}.jar
```


###Version Update

You need to give file extensions as a config. If you don't provide this step, we automatically detect your project type
and use default configurations. Currently we are supporting maven and npm type projects.

An example is following ( you need to put this code snippet to your `justrelease.yml` file.

```
version.update:
    - json,js
```

###Create Artifacts

In this step, you need to provide commands that create artifacts in your project. If you don't provide this configuration,
we will use default config according to your project. ( i.e.: For maven `mvn clean install` )

Example create artifacts config:

```
create.artifacts:
        - npm install
        - npm test
```

###Publish

In this step you need to provide publish configuration. Currently we are supporting `github` publish.

You can give your releasenotes file, and your attachment file.
An example is following:

```
publish:
        - github:
            - description=releasenotes.md
            - attachment=lib/index.js
```

###Project Variables

In the commands, you can use some variables. They are `${latest.tag}` and `${version}`

So, JustRelease automatically change those variables with corresponding values.

This is an example:

` - attachment:target/justrelease-${version}.zip`

###Release Notes Generator

JustRelease can update your Github Release Page according to your change log file..
You can specify change log file as:
```
publish:
        - github:
            - description=releasenotes.md
```
If you don't specify this file, JustRelease automatically collects all commit messages from the latest tag and generates change log file.

Also you can use third party tools for generating change log file.
See this example:
```
create.artifacts:
        - github_changelog_generator  --since-tag v1.1.3
publish:
        - github:
            - description:CHANGELOG.md
```
We are creating `CHANGELOG.md` file at `create.artifacts` step then use that file in `github` step.

Note: Github Changelog Generator is a good open source project you can find here: 

https://github.com/skywinder/github-changelog-generator

##How to Use JustRelease Library

###Most Simple Usage:

sh justrelease.sh username/reponame

In this usage we detect your project type, update the version ( we assume it is patch release).


###Giving Release Type

sh justrelease.sh username/reponame (major | minor | patch )


###DryRun Config

If you want to just observe what will happen when releasing you can use `DryRun` config.
In this case, there will be no push or committing. You will see just logs.

sh justrelease.sh username/reponame minor -dryRun
