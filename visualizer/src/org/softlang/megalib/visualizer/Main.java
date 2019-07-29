/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            VisualizerOptions options = VisualizerOptions.of(cli.getRequiredArguments());    
            Visualizer visualizer = new Visualizer(options);

            ModelToGraph mtg = new ModelToGraph(options);
            boolean success = mtg.loadModel();

            if(!success) {
            	mtg.getTypeErrors().forEach(e -> System.out.println(e));
            }else {
                if(cli.getGraphArgument().equals("force"))
                {
                    List<Graph> graphs = new ArrayList<>();
                    File cwd = new File(".");
                    try
                    {
                        cwd = new File(cwd.getCanonicalPath());
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(!cwd.getName().equals("models")) throw new MegaModelVisualizerException("Current working directory has to be \"models\"");
                    List<File> files = Arrays.asList(Objects.requireNonNull(cwd.listFiles()));
                    files.forEach(file -> {
                        if(file.isDirectory())
                        {
                            System.out.println(file.toString());
                            List<File> subfiles = Arrays.asList(Objects.requireNonNull(file.listFiles()));
                            subfiles.forEach(subfile -> {
                                if(subfile.isFile())
                                {
                                    String fileName="";
                                    try {
                                        fileName = subfile.getCanonicalPath();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println(fileName);
                                    String[] result = fileName.split("[/\\\\.]");

                                    String fileEnding = result[result.length-1];
                                    String shortFileName = result[result.length-2];
                                    String parentFolder = result[result.length-3];

                                    if(fileEnding.toLowerCase().equals("megal"))
                                    {
                                        VisualizerOptions tempOptions = VisualizerOptions.of(subfile.toPath(), "graphml", "graphml");
                                        ModelToGraph tempGraph = new ModelToGraph(tempOptions);
                                        boolean loadSuccess = tempGraph.loadModel();

                                        if(!loadSuccess) {
                                            tempGraph.getTypeErrors().forEach(e -> System.out.println(e));
                                        }
                                        else
                                        {
                                            graphs.add(tempGraph.createImportGraph());
                                        }
                                    }
                                }
                            });
                        }
                    });
                    Graph finalGraph = new Graph("ForceDirectedOverviewgraph",
                            "Force directed overview graph");

                    graphs.forEach(graph -> {
                        graph.forEachNode(node -> {
                            finalGraph.add(node);
                        });
                        graph.forEachEdge(edge -> {
                            Node fromNode = finalGraph.get(edge.getOrigin().getName());
                            Node toNode = graph.get(edge.getDestination().getName());
                            fromNode.connect(edge.getLabel(), toNode);
                        });
                    });

                    visualizer.plotGraph(finalGraph);

                }
                else if(cli.getGraphArgument().equals("overview"))
                {
                    Graph importGraph = mtg.createImportGraph();
                    visualizer.plotGraph(importGraph);
                }
                else
                {
                    List<Graph> graphs = mtg.createBlockGraphs();
                    graphs.forEach(visualizer::plotGraph);
                }

            	System.out.println("Visualization complete.");
            }
        } catch (MegaModelVisualizerException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

}
