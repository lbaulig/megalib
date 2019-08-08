/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer.cli;

import java.util.Objects;

/**
 *
 * @author Dmitri Nikonov <dnikonov at uni-koblenz.de>
 */
public class CommandLineArguments {

    private String filePath;

    private String graphType;
    private String type;
    private String specialOption;

    public CommandLineArguments(String type, String filePath, String graphType, String specialOption) {
        this.type = type;
        this.filePath = filePath;
        this.graphType = graphType;
        this.specialOption = specialOption;
    }
    public CommandLineArguments(String type, String filePath, String graphType) {
        this(type,filePath,graphType,null);
    }
    public CommandLineArguments(String type, String filePath) {
        this(type,filePath,null,null);
    }
    public CommandLineArguments(String type) {
        this(type,null,null,null);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getType() {
        return type;
    }

    public String getGraphType() {
        return graphType;
    }

    public String getSpecialOption() { return specialOption;}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.filePath);
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.graphType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CommandLineArguments other = (CommandLineArguments) obj;
        if (!Objects.equals(this.filePath, other.filePath)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.graphType, other.graphType)) {
            return false;
        }
        if (!Objects.equals(this.specialOption, other.specialOption)) {
            return false;
        }
        return true;
    }

}
