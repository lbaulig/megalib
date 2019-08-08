/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer.transformation.latex;

import org.softlang.megalib.visualizer.models.Node;

public class LATEXNode extends Node {

    private String color;

    private String shape;
    
    private String instance;

    public LATEXNode(Node original, String color, String shape, String instance) {
        super(original.getType(), original.getName(), original.getLink(), original.getInstanceHierarchy(), original.getEdges());
        this.color = color;
        this.shape = shape;
        this.instance = instance;
    }

    public String getColor() {
        return color;
    }

    public String getShape() {
        return shape;
    }
    
    public String getInstance() {
        return instance;
    }
}
