/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer;

import java.util.List;

import org.softlang.megalib.visualizer.cli.CommandLine;
import org.softlang.megalib.visualizer.exceptions.MegaModelVisualizerException;
import org.softlang.megalib.visualizer.models.Graph;
import org.softlang.megalib.visualizer.models.ModelToGraph;
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
            //Graph graph = new ModelToGraph(options).createGraph();
            //visualizer.plotGraph(graph);
            ModelToGraph mtg = new ModelToGraph(options);
            boolean success = mtg.loadModel();
            if(!success) {
            	mtg.getTypeErrors().forEach(e -> System.out.println(e));
            }else {
            	List<Graph> graphs = mtg.createBlockGraphs();
            	graphs.forEach(visualizer::plotGraph);

            	System.out.println("Visualization complete.");
            }
        } catch (MegaModelVisualizerException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

}
