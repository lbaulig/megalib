/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.softlang.megalib.visualizer.cli.CommandLineArguments;
import org.softlang.megalib.visualizer.models.transformation.TransformerRegistry;

/**
 *
 * @author Dmitri Nikonov <dnikonov at uni-koblenz.de>
 */
public class VisualizerOptions {

    public static VisualizerOptions of(CommandLineArguments args) {
        Path filePath = null;
        if(args.getFilePath()!=null)
        {
            Path path = Paths.get(args.getFilePath());
            filePath = path.toAbsolutePath();
        }

        String fileEnding = TransformerRegistry.getFileEnding(args.getType().toLowerCase());
        return new VisualizerOptions(filePath, args.getType().toLowerCase(), fileEnding);
    }

    public static VisualizerOptions of(Path filePath, String type, String fileEnding) {

        return new VisualizerOptions(filePath, type, fileEnding);
    }

    private Path filePath;

    private String type;
    
    private String fileEnding;
    
    
    private VisualizerOptions(Path filePath, String type, String fileEnding){
        this.filePath = filePath;
        this.type = type;
        this.fileEnding = fileEnding;
    }

    public String getModelName() {
        String modelName = "";
        if(filePath!=null)
            modelName = filePath.getFileName().toString().replaceAll("\\.megal", "");
        else
            modelName = "forceGraph";
        return modelName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getTransformationType() {
        return type;
    }

    public String getFileEnding() {
    	return fileEnding;
    }
    
    
    
}
