# N2SGEN

This is a simple static site generator for my personal sites. It uses Freemarker as a template engine and assumes that you write the content in Markdown. It has a built in web server, so it can also be used to serve the static pages generated. It should work well on any operating system provided that JRE8 is installed. To build it sbt need to be installed.

## Getting Started

Download the latest release from the [release page] (https://github.com/rewati/n2sgen/releases)

### Prerequisites

  - JAVA 8
  - SBT

### Installing

Extract the downloaded package. cd to the n2sgen directory and run: 
```
  cd $N2SGEN
  sbt assembly
```
After building add n2sgen executable to the PATH.
```
  export PATH=$N2SGEN/bin:$PATH
```

## Create Project

  Create a project
  ```
    mkdir myProject
    cd myProject
  ```
  Launch n2sgen client.
  ```
    n2sgen
    Welcome n2sgen. Yet another static site generator. Built in scala.
    Project name = myProject
    myProject not initialized>>>
  ```
## n2sgen client commands 
```
  init              Initialize project. This will create basic structure.
  new               Will create new page.
  help              Will print this help.
  compile           Compile and generate static html file.
  rsync             Rsync to remote server. Or can be rsync to HTTP server serving content location.
  ftp               Ftp to remote server. Or can be ftp to HTTP server serving content location.
  conf              Will reload the configuration.
  serve             Will launch a local server with the generate content.
```

rsync and ftp dont work in current version. Will be working in second version.

## Configure Project
  
  Initialize project and exit
  ```
    myProject not initialized>>> init
    Project myProject is initialized.
    myProject>>> exit
  ```
  Open n2sgen conf file
  ```
    vi n2sgen.conf
  ```
  Configure project name and the tags that should show in navigation bar on the site and save n2.sgen conf file.
  ```
    project-name=My Project
    nav=Scala,Java,Dev
  ```
  Change template
  ```
    vi templates/template
  ```
  Change style
  ```
    vi templates/css
  ```
  Launch n2sgen client and run conf.
  ```
    n2sgen
    Welcome n2sgen. Yet another static site generator. Built in scala.
    Project name = My Project
    My Project>>>
  ```
  
## Create new page

  ```
    My Project>>> new
    Title: First page
    My Project>>>
  ```
  This will create content/First-page.md. This page will have following as first line.
  
  ```
    <<<?title=First page||date=2017/02/04||tags=||published=false?>>>
  ```
  - published need to be set true if the page is ready to be published.
  - title is the tile of the page.
  - tags is comma seperated tags the page belong to. Like scala,java
  
## Generate html pages

```
  compile
```

### Launch local server

```
  Project name = My Project
  My Project>>> serve
  Please enter the Port: 8080
  2017-02-04 16:48:10.813:INFO::main: Logging initialized @6040ms
  Server started press Enter.
  My Project>>> 2017-02-04 16:48:10.875:INFO:oejs.Server:scala-execution-context-global-10: jetty-9.1.z-SNAPSHOT
  2017-02-04 16:48:10.907:INFO:oejs.ServerConnector:scala-execution-context-global-10: Started        ServerConnector@7b58c7e9{HTTP/1.1}{0.0.0.0:8080}
  2017-02-04 16:48:10.908:INFO:oejs.Server:scala-execution-context-global-10: Started @6154ms

  My Project>>>
```

  Go to http://localhost:8080/ 
  





