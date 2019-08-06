/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.softlang.megalib.visualizer.cli.CommandLine;
import org.softlang.megalib.visualizer.exceptions.MegaModelVisualizerException;
import org.softlang.megalib.visualizer.models.Graph;
import org.softlang.megalib.visualizer.models.ModelToGraph;
import org.softlang.megalib.visualizer.models.Node;
import org.softlang.megalib.visualizer.models.transformation.TransformerRegistry;
import org.softlang.megalib.visualizer.transformation.dot.DOTPDFTransformer;
import org.softlang.megalib.visualizer.transformation.dot.DOTTransformer;
import org.softlang.megalib.visualizer.transformation.graphml.GRAPHMLTransformer;
import org.softlang.megalib.visualizer.transformation.latex.LATEXTransformer;

public class Main {

    public static void main(String[] args) {
        try {
        	TransformerRegistry.registerTransformer("dot", "dot", (VisualizerOptions options)
        		     -> new DOTTransformer(options));
        	TransformerRegistry.registerTransformer("graphml", "graphml", (VisualizerOptions options)
          		     -> new GRAPHMLTransformer(options));
            TransformerRegistry.registerTransformer("dot_pdf", "dot", (VisualizerOptions options) 
            		-> new DOTPDFTransformer(options));
            TransformerRegistry.registerTransformer("latex", "tex", (VisualizerOptions options)
                    -> new LATEXTransformer(options));

            CommandLine cli = new CommandLine(TransformerRegistry.getRegisteredTransformerNames())
                .parse(args);

            if(!cli.getSpecialArgument().contains("i") && cli.getSpecialArgument().contains("l")) {
                System.out.println("Special option \"l\"=link is ignored if special option \"i\"=importGraph is missing.");
            }
            VisualizerOptions options = VisualizerOptions.of(cli.getAllArguments());
            Visualizer visualizer = new Visualizer(options);

            if(cli.getGraphArgument().equals("force")) {
                List<Graph> graphs = new LinkedList<>();
                File cwd = new File(".");
                try {
                    cwd = new File(cwd.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!cwd.getName().equals("models"))
                    throw new MegaModelVisualizerException("Current working directory has to be \"models\"");
                List<File> files = Arrays.asList(Objects.requireNonNull(cwd.listFiles()));
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (file.getPath().contains("common")) {
                            continue;
                        }
                        //debug
                        //System.out.println(file.toString());
                        List<File> subfiles = Arrays.asList(Objects.requireNonNull(file.listFiles()));
                        for (File subfile : subfiles) {
                            if (subfile.isFile()) {
                                String fileName = "";
                                try {
                                    fileName = subfile.getCanonicalPath();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //debug
                                //System.out.println(fileName);
                                String[] result = fileName.split("[/\\\\.]");

                                String fileEnding = result[result.length - 1];
                                String shortFileName = result[result.length - 2];
                                String parentFolder = result[result.length - 3];

                                if (fileEnding.toLowerCase().equals("megal")) {
                                    //debug
                                    System.out.println("Loading model: " + parentFolder + "." + shortFileName + "." + fileEnding);
                                    VisualizerOptions tempOptions = VisualizerOptions.of(subfile.toPath(), "graphml", "graphml");
                                    ModelToGraph tempGraph = new ModelToGraph(tempOptions);
                                    boolean loadSuccess = tempGraph.loadModel();

                                    if (!loadSuccess) {
                                        tempGraph.getTypeErrors().forEach(e -> System.out.println(e));
                                    } else {
                                        //debug
                                        //System.out.println("Load successful.");
                                        Graph importGraph = null;
                                        if(cli.getSpecialArgument().contains("i"))
                                        {
                                            importGraph = tempGraph.createImportGraph();
                                        }
                                        else
                                            importGraph = new Graph("tempgraph.tempgrah",
                                                    "temporary graph");
                                        //debug
                                        //System.out.println("Searching for: "+parentFolder+"."+shortFileName);
                                        //importGraph.forEachNode(node -> System.out.println(node.getName()));
                                        //String searchString = parentFolder + "." + shortFileName;
                                        //searchString = searchString.toLowerCase();

                                        if(cli.getSpecialArgument().contains("b"))
                                        {

                                        List<Graph> blockGraphs = tempGraph.createBlockGraphsOfModule(parentFolder);
                                        for (Graph graph : blockGraphs) {
                                            System.out.println("Processing Block: " + graph.getName());

                                            List<String> tempStrings = new LinkedList<String>(Arrays.asList(graph.getName().split("\\.")));
                                            tempStrings.remove(tempStrings.size() - 1);
                                            String searchString = String.join(".", tempStrings).toLowerCase();
                                            //debug
                                            //System.out.println(searchString);
                                            Graph finalImportGraph = importGraph;
                                            graph.forEachNode(node -> {
                                                //debug
                                                //System.out.println("Add node: "+ node.getName());
                                                //have to check if node already exists, otherwise edges are overwritten
                                                if (finalImportGraph.get(node.getName()) == null)
                                                    finalImportGraph.add(node);
                                                if(cli.getSpecialArgument().contains("i") && cli.getSpecialArgument().contains("l")) {
                                                    Node fromNode = finalImportGraph.get(searchString);
                                                    if (fromNode == null)
                                                        System.out.println("Could not find module \"" + searchString + "\" . Maybe mismatch between file name and module name?");
                                                    else {
                                                        Node toNode = finalImportGraph.get(node.getName());
                                                        toNode.connect("isIn", fromNode);
                                                    }
                                                }
                                            });
                                            Graph finalImportGraph1 = importGraph;
                                            graph.forEachEdge(edge -> {
                                                //debug
                                                //System.out.println("Add edge: "+edge.getOrigin().getName()+" "+edge.getLabel()+" "+edge.getDestination().getName());
                                                Node fromNode = finalImportGraph1.get(edge.getOrigin().getName());
                                                Node toNode = finalImportGraph1.get(edge.getDestination().getName());
                                                fromNode.connect(edge.getLabel(), toNode);
                                            });
                                        }
                                    }

                                        graphs.add(importGraph);
                                    }
                                }
                            }
                        }
                    }
                    Graph finalGraph = new Graph("forcegraph.forcegraph",
                            "Force directed overview graph");

                    for (Graph graph : graphs) {
                        graph.forEachNode(node -> {
                            //debug
                            //System.out.println("Add node: "+ node.getName());
                            //have to check if node already exists, otherwise edges are overwritten
                            if (finalGraph.get(node.getName()) == null)
                                finalGraph.add(node);
                        });
                    }
                    for (Graph graph : graphs) {
                        graph.forEachEdge(edge -> {
                            Node fromNode = finalGraph.get(edge.getOrigin().getName());
                            Node toNode = finalGraph.get(edge.getDestination().getName());
                            fromNode.connect(edge.getLabel(), toNode);
                        });
                    }
                    visualizer.plotGraph(finalGraph);
                }

            }
            else if(cli.getGraphArgument().equals("overview"))
            {
                ModelToGraph mtg = new ModelToGraph(options);
                boolean success = mtg.loadModel();
                if(!success) {
                    mtg.getTypeErrors().forEach(e -> System.out.println(e));
                }else {


                    Graph importGraph = null;
                    if(cli.getSpecialArgument().contains("i"))
                    {
                        importGraph = mtg.createImportGraph();
                    }
                    else
                        importGraph = new Graph("tempgraph.tempgrah",
                                "temporary graph");

                    if(cli.getSpecialArgument().contains("b")) {
                        List<Graph> blockGraphs = mtg.createBlockGraphsOfModule(mtg.getParentFolder());
                        for (Graph graph : blockGraphs) {
                            System.out.println("Processing Block: " + graph.getName());

                            List<String> tempStrings = new LinkedList<String>(Arrays.asList(graph.getName().split("\\.")));
                            tempStrings.remove(tempStrings.size() - 1);
                            String searchString = String.join(".", tempStrings).toLowerCase();
                            //debug
                            //System.out.println(searchString);
                            Graph finalImportGraph = importGraph;
                            graph.forEachNode(node -> {
                                //debug
                                //System.out.println("Add node: "+ node.getName());
                                //have to check if node already exists, otherwise edges are overwritten
                                if (finalImportGraph.get(node.getName()) == null)
                                    finalImportGraph.add(node);
                                if(cli.getSpecialArgument().contains("i") && cli.getSpecialArgument().contains("l"))
                                {
                                    Node fromNode = finalImportGraph.get(searchString);
                                    if (fromNode == null)
                                        System.out.println("Could not find module \"" + searchString + "\" . Maybe mismatch between file name and module name?");
                                    else {
                                        Node toNode = finalImportGraph.get(node.getName());
                                        toNode.connect("isIn", fromNode);
                                    }
                                }
                            });
                            Graph finalImportGraph1 = importGraph;
                            graph.forEachEdge(edge -> {
                                //debug
                                //System.out.println("Add edge: "+edge.getOrigin().getName()+" "+edge.getLabel()+" "+edge.getDestination().getName());
                                Node fromNode = finalImportGraph1.get(edge.getOrigin().getName());
                                Node toNode = finalImportGraph1.get(edge.getDestination().getName());
                                fromNode.connect(edge.getLabel(), toNode);
                            });

                        }
                    }
                    visualizer.plotGraph(importGraph);
                }
            }else if(cli.getGraphArgument().equals("feature"))
            {
                List<Graph> demoGraphs = new ArrayList<>();
                List<Graph> moduleGraphs = new ArrayList<>();
                File cwd = new File(".");
                try
                {
                    cwd = new File(cwd.getCanonicalPath());
                }catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("CWD: "+cwd.getPath());
                Path cwdPath = cwd.toPath();
                File demoFolder = null;
                File moduleFolder = null;
                try {
                    demoFolder = new File(cwdPath.toString()+"/"+cli.getFileArgument()).getCanonicalFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (demoFolder != null) {
                    System.out.println("Demo folder: "+demoFolder.getPath());
                    moduleFolder = new File(demoFolder.toPath().getParent().getParent().toString());
                    System.out.println("Module folder: "+moduleFolder.getPath());
                }
                else
                    throw new MegaModelVisualizerException("The folder provided does not match!");

                //module Folder
                List<File> files = Arrays.asList(Objects.requireNonNull(moduleFolder.listFiles()));
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = "";
                        try {
                            fileName = file.getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //debug
                        //System.out.println(fileName);
                        String[] result = fileName.split("[/\\\\.]");

                        String fileEnding = result[result.length - 1];
                        String shortFileName = result[result.length - 2];
                        String parentFolder = result[result.length - 3];

                        if (fileEnding.toLowerCase().equals("megal")) {
                            //debug
                            System.out.println("Loading model: " + parentFolder + "." + shortFileName + "." + fileEnding);
                            VisualizerOptions tempOptions = VisualizerOptions.of(file.toPath(), "graphml", "graphml");
                            ModelToGraph tempGraph = new ModelToGraph(tempOptions);
                            boolean loadSuccess = tempGraph.loadModel();

                            if(!loadSuccess) {
                                tempGraph.getTypeErrors().forEach(e -> System.out.println(e));
                            }else {

                                Graph importGraph = null;
                                if (cli.getSpecialArgument().contains("i")) {
                                    importGraph = tempGraph.createImportGraph();
                                } else
                                    importGraph = new Graph("tempgraph.tempgrah",
                                            "temporary graph");

                                if (cli.getSpecialArgument().contains("b")) {
                                    List<Graph> blockGraphs = tempGraph.createBlockGraphsOfModule(tempGraph.getParentFolder());
                                    for (Graph graph : blockGraphs) {
                                        System.out.println("Processing Block: " + graph.getName());

                                        List<String> tempStrings = new LinkedList<String>(Arrays.asList(graph.getName().split("\\.")));
                                        tempStrings.remove(tempStrings.size() - 1);
                                        String searchString = String.join(".", tempStrings).toLowerCase();
                                        //debug
                                        //System.out.println(searchString);
                                        Graph finalImportGraph = importGraph;
                                        graph.forEachNode(node -> {
                                            //debug
                                            //System.out.println("Add node: "+ node.getName());
                                            //have to check if node already exists, otherwise edges are overwritten
                                            if (finalImportGraph.get(node.getName()) == null) {
                                                node.setType(node.getType() + "_demo");
                                                finalImportGraph.add(node);
                                            }
                                            if(cli.getSpecialArgument().contains("i") && cli.getSpecialArgument().contains("l")) {
                                                Node fromNode = finalImportGraph.get(searchString);
                                                if (fromNode == null)
                                                    System.out.println("Could not find module \"" + searchString + "\" . Maybe mismatch between file name and module name?");
                                                else {
                                                    Node toNode = finalImportGraph.get(node.getName());
                                                    toNode.connect("isIn", fromNode);
                                                }
                                            }
                                        });
                                        Graph finalImportGraph1 = importGraph;
                                        graph.forEachEdge(edge -> {
                                            //debug
                                            //System.out.println("Add edge: "+edge.getOrigin().getName()+" "+edge.getLabel()+" "+edge.getDestination().getName());
                                            Node fromNode = finalImportGraph1.get(edge.getOrigin().getName());
                                            Node toNode = finalImportGraph1.get(edge.getDestination().getName());
                                            fromNode.connect(edge.getLabel(), toNode);
                                        });

                                    }
                                }
                                moduleGraphs.add(importGraph);
                            }
                        }
                    }
                }

                Graph moduleGraph = new Graph("modulegraph.modulegraph",
                        "moduleGraph");

                for (Graph graph : moduleGraphs) {
                    graph.forEachNode(node -> {
                        //debug
                        //System.out.println("Add node: "+ node.getName());
                        //have to check if node already exists, otherwise edges are overwritten
                        if (moduleGraph.get(node.getName()) == null)
                            moduleGraph.add(node);
                    });
                }
                for (Graph graph : moduleGraphs) {
                    graph.forEachEdge(edge -> {
                        Node fromNode = moduleGraph.get(edge.getOrigin().getName());
                        Node toNode = moduleGraph.get(edge.getDestination().getName());
                        fromNode.connect(edge.getLabel(), toNode);
                    });
                }

                //demo Folder
                files = Arrays.asList(Objects.requireNonNull(demoFolder.listFiles()));
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = "";
                        try {
                            fileName = file.getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //debug
                        //System.out.println(fileName);
                        String[] result = fileName.split("[/\\\\.]");

                        String fileEnding = result[result.length - 1];
                        String shortFileName = result[result.length - 2];
                        String parentFolder = result[result.length - 3];

                        if (fileEnding.toLowerCase().equals("megal")) {
                            //debug
                            System.out.println("Loading model: " + parentFolder + "." + shortFileName + "." + fileEnding);
                            VisualizerOptions tempOptions = VisualizerOptions.of(file.toPath(), "graphml", "graphml");
                            ModelToGraph tempGraph = new ModelToGraph(tempOptions);
                            boolean loadSuccess = tempGraph.loadModel();

                            if(!loadSuccess) {
                                tempGraph.getTypeErrors().forEach(e -> System.out.println(e));
                            }else {

                                Graph importGraph = null;
                                if(cli.getSpecialArgument().contains("i"))
                                {
                                    importGraph = tempGraph.createImportGraph();
                                }
                                else
                                    importGraph = new Graph("tempgraph.tempgrah",
                                            "temporary graph");

                                if(cli.getSpecialArgument().contains("b")) {
                                    List<Graph> blockGraphs = tempGraph.createBlockGraphsOfModule(tempGraph.getParentFolder());
                                    for (Graph graph : blockGraphs) {
                                        System.out.println("Processing Block: " + graph.getName());

                                        List<String> tempStrings = new LinkedList<String>(Arrays.asList(graph.getName().split("\\.")));
                                        tempStrings.remove(tempStrings.size() - 1);
                                        String searchString = String.join(".", tempStrings).toLowerCase();
                                        //debug
                                        //System.out.println(searchString);
                                        Graph finalImportGraph = importGraph;
                                        graph.forEachNode(node -> {
                                            //debug
                                            //System.out.println("Add node: "+ node.getName());
                                            //have to check if node already exists, otherwise edges are overwritten
                                            if (finalImportGraph.get(node.getName()) == null) {
                                                finalImportGraph.add(node);
                                            }
                                            if(cli.getSpecialArgument().contains("i") && cli.getSpecialArgument().contains("l")) {
                                                Node fromNode = finalImportGraph.get(searchString);
                                                if (fromNode == null)
                                                    System.out.println("Could not find module \"" + searchString + "\" . Maybe mismatch between file name and module name?");
                                                else {
                                                    Node toNode = finalImportGraph.get(node.getName());
                                                    toNode.connect("isIn", fromNode);
                                                }
                                            }
                                        });
                                        Graph finalImportGraph1 = importGraph;
                                        graph.forEachEdge(edge -> {
                                            //debug
                                            //System.out.println("Add edge: "+edge.getOrigin().getName()+" "+edge.getLabel()+" "+edge.getDestination().getName());
                                            Node fromNode = finalImportGraph1.get(edge.getOrigin().getName());
                                            Node toNode = finalImportGraph1.get(edge.getDestination().getName());
                                            fromNode.connect(edge.getLabel(), toNode);
                                        });

                                    }
                                }
                                demoGraphs.add(importGraph);
                            }
                        }
                    }
                }

                Graph demoGraph = new Graph("demograph.demograph",
                        "Force directed overview graph");

                for (Graph graph : demoGraphs) {
                    graph.forEachNode(node -> {
                        //debug
                        //System.out.println("Add node: "+ node.getName());
                        //have to check if node already exists, otherwise edges are overwritten
                        if (demoGraph.get(node.getName()) == null)
                            demoGraph.add(node);
                    });
                }
                for (Graph graph : demoGraphs) {
                    graph.forEachEdge(edge -> {
                        Node fromNode = demoGraph.get(edge.getOrigin().getName());
                        Node toNode = demoGraph.get(edge.getDestination().getName());
                        fromNode.connect(edge.getLabel(), toNode);
                    });
                }




                //combine Graphs


                Graph finalGraph = new Graph("feature.featureGraph",
                        "Feature Graph");


                for (Graph graph : moduleGraphs) {
                    graph.forEachNode(node -> {
                        //debug
                        //System.out.println("Add node: "+ node.getName());
                        //have to check if node already exists, otherwise edges are overwritten
                        if (finalGraph.get(node.getName()) == null)
                            finalGraph.add(node);
                    });
                }
                for (Graph graph : moduleGraphs) {
                    graph.forEachEdge(edge -> {
                        Node fromNode = finalGraph.get(edge.getOrigin().getName());
                        Node toNode = finalGraph.get(edge.getDestination().getName());
                        fromNode.connect(edge.getLabel(), toNode);
                    });
                }


                for (Graph graph : demoGraphs) {
                    graph.forEachNode(node -> {
                        //debug
                        //System.out.println("Add node: "+ node.getName());
                        //have to check if node already exists, otherwise edges are overwritten
                        if (finalGraph.get(node.getName()) == null)
                            finalGraph.add(node);
                        else
                        {
                            Node changeNode = finalGraph.get(node.getName());
                            changeNode.setType(node.getType());
                        }
                    });
                }
                for (Graph graph : demoGraphs) {
                    graph.forEachEdge(edge -> {
                        Node fromNode = finalGraph.get(edge.getOrigin().getName());
                        Node toNode = finalGraph.get(edge.getDestination().getName());
                        fromNode.connect(edge.getLabel(), toNode);
                    });
                }
                visualizer.plotGraph(finalGraph);
            }
            else if(cli.getGraphArgument().equals("folder"))
            {
                List<Graph> moduleGraphs = new ArrayList<>();
                File cwd = new File(".");
                try
                {
                    cwd = new File(cwd.getCanonicalPath());
                }catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("CWD: "+cwd.getPath());
                Path cwdPath = cwd.toPath();
                File moduleFolder = null;
                try {
                    moduleFolder = new File(cwdPath.toString()+"/"+cli.getFileArgument()).getCanonicalFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (moduleFolder != null) {
                    System.out.println("module folder: "+moduleFolder.getPath());
                }
                else
                    throw new MegaModelVisualizerException("The folder provided does not match!");

                //module Folder
                List<File> files = Arrays.asList(Objects.requireNonNull(moduleFolder.listFiles()));
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = "";
                        try {
                            fileName = file.getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //debug
                        //System.out.println(fileName);
                        String[] result = fileName.split("[/\\\\.]");

                        String fileEnding = result[result.length - 1];
                        String shortFileName = result[result.length - 2];
                        String parentFolder = result[result.length - 3];

                        if (fileEnding.toLowerCase().equals("megal")) {
                            //debug
                            System.out.println("Loading model: " + parentFolder + "." + shortFileName + "." + fileEnding);
                            VisualizerOptions tempOptions = VisualizerOptions.of(file.toPath(), "graphml", "graphml");
                            ModelToGraph tempGraph = new ModelToGraph(tempOptions);
                            boolean loadSuccess = tempGraph.loadModel();

                            if(!loadSuccess) {
                                tempGraph.getTypeErrors().forEach(e -> System.out.println(e));
                            }else {

                                Graph importGraph = null;
                                if(cli.getSpecialArgument().contains("i"))
                                {
                                    importGraph = tempGraph.createImportGraph();
                                }
                                else
                                    importGraph = new Graph("tempgraph.tempgrah",
                                            "temporary graph");

                                if(cli.getSpecialArgument().contains("b")) {
                                    List<Graph> blockGraphs = tempGraph.createBlockGraphsOfModule(tempGraph.getParentFolder());
                                    for (Graph graph : blockGraphs) {
                                        System.out.println("Processing Block: " + graph.getName());

                                        List<String> tempStrings = new LinkedList<String>(Arrays.asList(graph.getName().split("\\.")));
                                        tempStrings.remove(tempStrings.size() - 1);
                                        String searchString = String.join(".", tempStrings).toLowerCase();
                                        //debug
                                        //System.out.println(searchString);
                                        Graph finalImportGraph = importGraph;
                                        graph.forEachNode(node -> {
                                            //debug
                                            //System.out.println("Add node: "+ node.getName());
                                            //have to check if node already exists, otherwise edges are overwritten
                                            if (finalImportGraph.get(node.getName()) == null) {
                                                finalImportGraph.add(node);
                                            }
                                            if(cli.getSpecialArgument().contains("i") && cli.getSpecialArgument().contains("l")) {
                                                Node fromNode = finalImportGraph.get(searchString);
                                                if (fromNode == null)
                                                    System.out.println("Could not find module \"" + searchString + "\" . Maybe mismatch between file name and module name?");
                                                else {
                                                    Node toNode = finalImportGraph.get(node.getName());
                                                    toNode.connect("isIn", fromNode);
                                                }
                                            }
                                        });
                                        Graph finalImportGraph1 = importGraph;
                                        graph.forEachEdge(edge -> {
                                            //debug
                                            //System.out.println("Add edge: "+edge.getOrigin().getName()+" "+edge.getLabel()+" "+edge.getDestination().getName());
                                            Node fromNode = finalImportGraph1.get(edge.getOrigin().getName());
                                            Node toNode = finalImportGraph1.get(edge.getDestination().getName());
                                            fromNode.connect(edge.getLabel(), toNode);
                                        });

                                    }
                                }
                                moduleGraphs.add(importGraph);
                            }
                        }
                    }
                }

                Graph finalGraph = new Graph("modulegraph.modulegraph",
                        "Complete module graph");

                for (Graph graph : moduleGraphs) {
                    graph.forEachNode(node -> {
                        //debug
                        //System.out.println("Add node: "+ node.getName());
                        //have to check if node already exists, otherwise edges are overwritten
                        if (finalGraph.get(node.getName()) == null)
                            finalGraph.add(node);
                    });
                }
                for (Graph graph : moduleGraphs) {
                    graph.forEachEdge(edge -> {
                        Node fromNode = finalGraph.get(edge.getOrigin().getName());
                        Node toNode = finalGraph.get(edge.getDestination().getName());
                        fromNode.connect(edge.getLabel(), toNode);
                    });
                }
                visualizer.plotGraph(finalGraph);
            }
            else if(cli.getTypeArgument().equals("latex"))
            {
                ModelToGraph mtg = new ModelToGraph(options);
                boolean success = mtg.loadModel();
                if(!success) {
                    mtg.getTypeErrors().forEach(e -> System.out.println(e));
                }else {

                    Graph latex = mtg.createLatexGraph();
                    visualizer.plotGraph(latex);
                }
            }
            else // "default"
            {
                ModelToGraph mtg = new ModelToGraph(options);
                boolean success = mtg.loadModel();
                if(!success)
                {
                    mtg.getTypeErrors().forEach(e -> System.out.println(e));
                }
                else
                {
                    List<Graph> graphs = mtg.createBlockGraphs();
                    graphs.forEach(visualizer::plotGraph);
                }
            }
            System.out.println("Visualization complete.");
    } catch (MegaModelVisualizerException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

}
