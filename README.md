
#JustRelease

[![Join the chat at https://gitter.im/justrelease/justrelease](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/justrelease/justrelease?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/justrelease/justrelease.svg?branch=master)](https://travis-ci.org/justrelease/justrelease)
[![codecov.io](http://codecov.io/github/justrelease/justrelease/coverage.svg?branch=master)](http://codecov.io/github/justrelease/justrelease?branch=master)

JustRelease is command line tool to release software libraries hosted on github.com. Justrelease requires no configuration and applies default behavior in each release operation. If you want to customize some of the features offered by justrelease, please include `justrelease.yml` file in your repository.

##Requirements


- Github Account and Github Repository
- Git in your local computer
- `justrelease.yml` for custom configuration

##Installation & Quick Example

##### OSX Installation
- `brew install justrelease/justrelease/justrelease`

##### Default Installation (Works All Operating Systems)
- Download and unzip [latest zip file](https://github.com/justrelease/justrelease/releases)

##### Configuring Credentials
- Generate [Personal Access Token](https://github.com/settings/tokens)
- Create ~/.github file and set *login* and *oauth* parameters as below
```
login=github_username
oauth=GITHUB_OAUTH_TOKEN
```
- fork [sample repository](https://github.com/justrelease/justrelease-sample-npm)
- update releasenotes.md file and commit the change.
- run `sh justrelease.sh yourusername/justrelease-sample-npm patch`
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

This is example `justrelease.yml` file:

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
