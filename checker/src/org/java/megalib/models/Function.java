package org.java.megalib.models;

import java.util.Collections;
import java.util.List;

public class Function {
    private List<String> inputs;
    private List<String> outputs;

    private Block block;
    public boolean isDecl;

    public Function(List<String> inputs, List<String> outputs, boolean isDecl){
        this.inputs = inputs;
        this.outputs = outputs;
        this.isDecl = isDecl;
    }

    public List<String> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<String> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block b) {
        block = b;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            // System.out.println("Null");
            return false;

        if(getClass() != obj.getClass())
            // System.out.println("Class unequal");
            return false;
        if(obj instanceof Function){
            Function o = (Function) obj;
            if(o.getInputs().size() != getInputs().size() || o.getOutputs().size() != getOutputs().size())
                return false;
            for(int i = 0; i < getInputs().size(); i++){
                if(!getInputs().get(i).equals(o.getInputs().get(i)))
                    return false;
            }
            for(int i = 0; i < getOutputs().size(); i++){
                if(!getOutputs().get(i).equals(o.getOutputs().get(i)))
                    return false;
            }

            return true;
        }else
            return obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return inputs.hashCode() + outputs.hashCode();
    }
}
