/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer.cli;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.softlang.megalib.visualizer.Visualizer;
import org.softlang.megalib.visualizer.VisualizerOptions;
import org.softlang.megalib.visualizer.exceptions.CommandLineException;
import org.softlang.megalib.visualizer.models.Graph;
import org.softlang.megalib.visualizer.models.ModelToGraph;
import org.softlang.megalib.visualizer.models.transformation.TransformerRegistry;
import org.softlang.megalib.visualizer.transformation.dot.DOTTransformer;

/**
 *
 * @author Dmitri Nikonov <dnikonov at uni-koblenz.de>
 */
public class CommandLineTest {

    @Test
    public void testValidData() {
        CommandLine cli = new CommandLine(Arrays.asList("graphviz", "testtype"));

        String data[] = {
            "-f",
            "abc123",
            "-t",
            "graphviz"
        };
        cli.parse(data);
        Assert.assertEquals(
            new CommandLineArguments("graphviz", "abc123"), cli.getAllArguments());//cli.getRequiredArguments());
    }

    @Test
    public void testValidData2() {
        CommandLine cli = new CommandLine(Arrays.asList("graphviz", "testtype"));
        String another[] = {
            "-f",
            "abc123",
            "-t",
            "testtype"
        };
        cli.parse(another);
        Assert.assertEquals(
            new CommandLineArguments( "testtype", "abc123"), cli.getAllArguments());//cli.getRequiredArguments());
    }

    @Test
    public void testWebBrowser() {
    	TransformerRegistry.registerTransformer("dot", "dot", (VisualizerOptions options)
   		     -> new DOTTransformer(options));
        CommandLine cli = new CommandLine(Arrays.asList("graphviz", "dot"));

        String data[] = {"-f", "../models/webbrowser/Webbrowser.megal", "-t", "dot"};
        cli.parse(data);
        VisualizerOptions options = VisualizerOptions.of(cli.getAllArguments());//cli.getRequiredArguments());

        ModelToGraph mtg = new ModelToGraph(options);
	    assertTrue(mtg.loadModel());
	    Graph graph = mtg.createGraph();

        Visualizer visualizer = new Visualizer(options);
        visualizer.plotGraph(graph);

        System.out.println("Visualization complete.");
    }

    @Test(expected = CommandLineException.class)
    public void testWithNoSupportedTypes() {
        CommandLine cli = new CommandLine(Collections.emptyList());

        String data[] = {
            "-f",
            "abc123",
            "-t",
            "test"
        };

        cli.parse(data);
    }

    @Test(expected = CommandLineException.class)
    public void testWithInvalidType() {
        CommandLine cli = new CommandLine(Arrays.asList("abc", "123"));

        String data[] = {
            "-f",
            "abc123",
            "-t",
            "test"
        };

        cli.parse(data);
    }

    @Test(expected = CommandLineException.class)
    public void testWithMissingFileArgument() {
        CommandLine cli = new CommandLine(Arrays.asList("abc", "123"));

        String data[] = {
            "-t",
            "test"
        };

        cli.parse(data);
    }

    @Test(expected = CommandLineException.class)
    public void testWithMissingTypeArgument() {
        CommandLine cli = new CommandLine(Arrays.asList("abc", "123"));

        String data[] = {
            "-f",
            "abc123",
        };

        cli.parse(data);
    }

}
