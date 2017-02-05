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

## Usage





