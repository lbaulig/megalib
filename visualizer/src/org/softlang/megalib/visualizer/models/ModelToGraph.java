/*
 *  All rights reserved.
 */
package org.softlang.megalib.visualizer.models;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.java.megalib.checker.services.ModelLoader;
import org.java.megalib.models.Block;
import org.java.megalib.models.Function;
import org.java.megalib.models.MegaModel;
import org.java.megalib.models.Relation;
import org.softlang.megalib.visualizer.VisualizerOptions;

public class ModelToGraph {

	private MegaModel model;
	private VisualizerOptions options;
	private ModelLoader loader;


	private String fileEnding;
	private String fileName;
	private String parentFolder;
	private String moduleName;

	public ModelToGraph(VisualizerOptions options) {
		this.options = options;
	}
	
	public ModelToGraph() {
		
	}

	public String getFileEnding() {
		return fileEnding;
	}

	public String getFileName() {
		return fileName;
	}

	public String getParentFolder() {
		return parentFolder;
	}
	public String getModuleName() {
		return moduleName;
	}

	public boolean loadModel() {
		loader = new ModelLoader();
		try {
			model = loader.getModel();
			Path filePath = options.getFilePath();

			String[] result = filePath.toString().split("[/\\\\.]");

			if(result.length >=3)
			{
				this.fileEnding = result[result.length-1];
				this.fileName = result[result.length-2];
				this.parentFolder = result[result.length-3];
				this.moduleName = parentFolder+"."+fileName;
			}
			return loader.loadFile(options.getFilePath().toAbsolutePath().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}


	public Graph createGraph() {
        Graph graph = new Graph(options.getModelName() + "Complete",
                "The complete Megamodel for " + options.getModelName());
        model.getInstanceOfMap().entrySet().stream().filter(entry -> !entry.getValue().equals("Link"))
                .map(entry -> createNode(entry.getKey(), entry.getValue(), model)).forEach(graph::add);
        model.getFunctionDeclarations().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
        model.getFunctionApplications().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
        model.getRelationships().entrySet().stream().filter(e -> !e.getKey().equals("=") && !e.getKey().equals("~="))
                .forEach(e -> createEdgesByRelations(graph, e.getKey(), e.getValue()));
        model.getFunctionDeclarations().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
        model.getFunctionApplications().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));

        return graph;
    }
	
	public Graph createLatexGraph() {
		Graph graph = new Graph(options.getModelName(),"ToDo");
        model.getInstanceOfMap().entrySet().stream().filter(entry -> !entry.getValue().equals("Link"))
                .map(entry -> createNode(entry.getKey(), entry.getValue(), model)).forEach(graph::add);
        model.getFunctionDeclarations().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
        model.getFunctionApplications().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
        model.getRelationships().entrySet().stream().filter(e -> !e.getKey().equals("=") && !e.getKey().equals("~="))
                .forEach(e -> createEdgesByRelations(graph, e.getKey(), e.getValue()));
        model.getFunctionDeclarations().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
        model.getFunctionApplications().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
        return graph;
    }

    public Graph createImportGraph() {
		//debug
		//System.out.println("Name:" + graphName);

        Graph graph = new Graph(moduleName+ "Importgraph",
                "Import graph for " + moduleName);


        if(!model.getImportGraph().isEmpty())
		{
			//create Each node
			model.getImportGraph().forEach(relation ->
			{
				//debug
				//System.out.println(relation.getSubject() + " imports "+relation.getObject());
				Node nodeLeft = new Node("Module", relation.getSubject(), "");
				nodeLeft.getInstanceHierarchy().add(nodeLeft.getType());
				Node nodeRight = new Node("Module", relation.getObject(), "");
				nodeRight.getInstanceHierarchy().add(nodeRight.getType());
				graph.add(nodeLeft);
				graph.add(nodeRight);
			});

			model.getImportGraph().forEach(relation -> createEdge(graph, relation.getSubject(), relation.getObject(), "imports"));
		}
        else
		{
			//debug
			//model has no imports, add as single node
			Node node = new Node("Module", moduleName, "");
			graph.add(node);
		}

        return graph;
    }




	public List<Graph> createBlockGraphs() {
		List<Graph> graphs = new LinkedList<>();
		for (Block b : model.getBlocks()) {
			if (b.getModule().startsWith("common.")) {
				continue;
			}
			Graph graph = new Graph(b.getModule() + b.getId(), b.getText());
			//debug
			//System.out.println("Name: "+graph.getName() + " Text: "+graph.getText());
			// instance nodes
			b.getInstanceOfMap().entrySet().stream().filter(entry -> !entry.getValue().equals("Link"))
					.map(entry -> createNode(entry.getKey(), entry.getValue(), model)).forEach(graph::add);
			b.getFunctionDeclarations().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
			b.getFunctionApplications().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
			b.getRelationships().entrySet().stream().filter(e -> !e.getKey().equals("=") && !e.getKey().equals("~="))
					.forEach(e -> createEdgesByRelations(graph, e.getKey(), e.getValue()));
			b.getFunctionDeclarations().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
			b.getFunctionApplications().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
			graphs.add(graph);
		}

		return graphs;
	}

	public List<Graph> createBlockGraphsOfModule(String moduleName) {
		List<Graph> graphs = new LinkedList<>();
		for (Block b : model.getBlocks()) {
			if (!b.getModule().startsWith(moduleName)) {
				continue;
			}
			//debug
			//System.out.println("Creating Graph of: "+b.getModule());
			Graph graph = new Graph(b.getModule() +"."+ b.getId(), b.getText());
			//debug
			//System.out.println("Name: "+graph.getName() + " Text: "+graph.getText());
			// instance nodes
			b.getInstanceOfMap().entrySet().stream().filter(entry -> !entry.getValue().equals("Link"))
					.map(entry -> createNode(entry.getKey(), entry.getValue(), model)).forEach(graph::add);
			b.getFunctionDeclarations().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
			b.getFunctionApplications().forEach((name, funcs) -> graph.add(createNode(name, "Function", model)));
			b.getRelationships().entrySet().stream().filter(e -> !e.getKey().equals("=") && !e.getKey().equals("~="))
					.forEach(e -> createEdgesByRelations(graph, e.getKey(), e.getValue()));
			b.getFunctionDeclarations().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
			b.getFunctionApplications().forEach((name, functions) -> createEdgesByFunction(graph, name, functions));
			graphs.add(graph);
		}

		return graphs;
	}

	protected Node createNode(String name, String type, MegaModel model) {
		//public Node(String type, String name, String link)
		String link = getFirstInstanceLink(model, name);
		Node result = new Node(type, name, link);
		//debug
		//System.out.println("New Node: Type: "+type+" Name: "+ name+" Link: "+link);
		applyInstanceHierarchy(result);
		return result;
	}

	private void applyInstanceHierarchy(Node node) {
		String type = node.getType();
		while (type != null && !type.isEmpty()) {
			node.getInstanceHierarchy().add(type);
			type = model.getSubtypesMap().get(type);
		}
	}

	private String getFirstInstanceLink(MegaModel model, String name) {
		Set<Relation> r = new HashSet();
		if(model.getRelationships().get("=") != null) {
			r.addAll(model.getRelationships().get("="));
		}
		if(model.getRelationships().get("~=") !=null) {
			r.addAll(model.getRelationships().get("~="));
		}
		for(Relation x:r) {
			if(x.getSubject().equals(name)) {
				String link = x.getObject();
				if(link.contains("::")){
		            String ns = link.split("::")[0];
		            link = link.replace(ns + "::", model.getNamespace(ns) + "/");
		        }
				return link;
			}
		}
		return "";
	}

	protected void createEdgesByFunction(Graph graph, String functionName, Set<Function> funcs) {
		Iterator<Function> it = funcs.iterator();
		for (int i = 0; it.hasNext(); i++) {
			createEdgesByFunction(graph, functionName, it.next(), i);
		}
	}

	protected void createEdgesByFunction(Graph graph, String functionName, Function f, int i) {
		if (f.isDecl) {
			f.getInputs().forEach(input -> createEdge(graph, input, functionName, "domainOf_" + i));
			f.getOutputs().forEach(output -> createEdge(graph, functionName, output, "hasRange_" + i));
		} else {
			f.getInputs().forEach(input -> createEdge(graph, input, functionName, "inputOf_" + i));
			f.getOutputs().forEach(output -> createEdge(graph, functionName, output, "hasOutput_" + i));
		}
	}

	protected void createEdgesByRelations(Graph graph, String relationName, Set<Relation> relations) {
		relations.stream()
				.forEach(relation -> createEdge(graph, relation.getSubject(), relation.getObject(), relationName));
	}

	private void createEdge(Graph graph, String from, String to, String relation) {
		Node fromNode = graph.get(from);
		Node toNode = graph.get(to);
		fromNode.connect(relation, toNode);
	}

	public List<String> getTypeErrors() {
		return loader.getTypeErrors();
	}
}
