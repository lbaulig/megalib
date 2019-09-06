# megalib visualizer
A visualization library for MegaL models.

## Motivation
[MegaL](https://github.com/softlang/megalib/blob/master/docs/LanguageDescription.pdf) is a text-based modeling language to describe relations between software artifacts and their corresponding concepts. As textual models are rather hard to read, a visualization library is to be considered to create a graphical representation of the model. Megalib visualizer is a component implemented as a part of the megalib providing this functionality.

The megalib visualizer is designed and implemented to be extendible using a flexible design. It provides several classes and interfaces that will help developers extending the library and providing other concrete visualizer adapters.

## Installation
Before installing the visualizer library, please ensure that your target machine provides a Java Runtime Environment with minimal version 8. Check your installed Java version and update if necessary. You can download the latest JRE 8 [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). The project was also successfully compiled and tested on openJDK8+.

This repository provides a runable binary in form of a jar file. It is also possible to compile the project source code yourself. For further instructions, refer to [Custom build](#contribution)

Anyway, the jar file, regardless of the origin, must obtain the following file hierarchy:
```
---megalib/
   ---models/
      ---common/
         ---Prelude.megal
   ---visualizer
      ---visualizer.jar
```
In concrete means, the parent directory of the current working directory has to contain the folder "/models" or in other word, the relative path "../models" in relation to the current working directory must be valid. Another requirement is that the "/models" directory contains the directory "/common", which in turn needs to contain "Prelude.megal". This file needs to be loaded for every MegaModel and therefore has to be in this exact location. At current stage, this is a strict directory structure to be obtained. Otherwhise the execution will result in erroneous behavior.

## Usage
For correct results please ensure that the directory structure shown above is obtained. The executable binary provides a cli-like command. The execution must be done within the folder containing the visualizer binary. You have to run the binary jar file using java and the following parameters:
```bash
-f(ile) # the file location of the mega model to be visualized (relative or absolute), this may also be the folder in case of graph option "feature" or "folder" and can be omitted completely for graph option "force" (which producer one single graph of all MegaL models in /models
-t(ype) # the concrete visualizer type, currently supported: graphml, dot, dot_pdf, latex
-g(raph) # this parameter decides which kind of visualization is generated, valid values are "default", "overiew", "latex", "feature", "force" or "folder"
-s(pecial) # this parameter decides whether the resulting graph contains a "i" => import graph, "b" => block graph, and "l" => whether the import graph and block graph are linked to each other, this options are concatinated together, e.g. "-s bil" would be a valid parameter aswell as "-s b", "-s i" and "-s bi"
```

Every parameter flag takes another argument which will be the value for its key. A concrete example of visualizing a model would be:
```bash
cd models
java -jar ../visualizer/visualizer.jar -f ../models/antlr/App.megal -t graphml -g overview -s bil
```
This command takes the MegaL model "../model/antlr/App.megal" into account and visualizes it using the graphml transformer. The concrete example will generate a .graphml file in "../output/antlr". Executing the jar file without any arguments will print a supporting dialog that shows the concrete usage manual for the visualizer.


## Configuration
Concrete visualizer adapters may use additional configuration options provided outside of the execution scope. For concrete information on configuring a concrete visualizer adapter, visit the [wiki](https://github.com/nikonovd/megalib/wiki)

## Contribution
Contribution to this project requires several steps to be done. 

1. Clone this git repository using ```git clone```
2. Ensure that the checker dependency is provided on any maven repository, even if it has to be the local repository.
   The current visualizer implementation relies on the following checker dependency structure:
   ```xml
   <dependency>
      <groupId>org.softlang.megalib</groupId>
      <artifactId>checker</artifactId>
      <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```
   Otherwise build of this project will fail.
3. Apply extensions and changes to the code. For advances extension techniques, visit the [wiki](https://github.com/softlang/megalib/wiki)
4. Use maven to build this project. A distributable and executable jar will be provided within the target directory.
