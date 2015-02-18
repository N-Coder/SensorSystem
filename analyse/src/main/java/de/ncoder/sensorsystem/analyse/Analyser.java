package de.ncoder.sensorsystem.analyse;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

import de.ncoder.sensorsystem.*;
import de.ncoder.typedmap.Key;

public class Analyser {
	public static void outputGraph(Container container, String filename) throws IOException {
		outputGraph(container, new FileOutputStream(filename));
	}

	public static void outputGraph(Container container, OutputStream graphMLOutputStream) throws IOException {
		getGraphMLWriter(analyse(container)).outputGraph(graphMLOutputStream);
	}

	public static void outputGraph(Graph graph, String filename) throws IOException {
		outputGraph(graph, new FileOutputStream(filename));
	}

	public static void outputGraph(Graph graph, OutputStream graphMLOutputStream) throws IOException {
		getGraphMLWriter(graph).outputGraph(graphMLOutputStream);
	}

	private static GraphMLWriter getGraphMLWriter(Graph graph) {
		GraphMLWriter writer = new GraphMLWriter(graph);
		writer.setNormalize(true);
		writer.setEdgeLabelKey("label");
		return writer;
	}

	public static Graph analyse(Container container) {
		Graph graph = new TinkerGraph();

		Vertex root = graph.addVertex(Container.class);
		root.setProperty("label", Utils.simpleClassNames(container.getClass().getName()));
		root.setProperty("description", container.toString());
		root.setProperty("class", container.getClass().getName());
		int number = 0;
		for (Key<? extends Component> key : ((SimpleContainer) container).getKeyOrder()) {
			Component component = container.get(key);
			if (component != null) {
				Vertex vertex = graph.addVertex(key);
				vertex.setProperty("label", Utils.simpleClassNames(key.toString()));
				vertex.setProperty("description", component.toString());
				vertex.setProperty("class", component.getClass().getName());
				vertex.setProperty("key", key);
				vertex.setProperty("number", number);
				if (component instanceof DependantComponent) {
					Set<Key<? extends Component>> dependencies = ((DependantComponent) component).dependencies();
					for (Key<? extends Component> dependency : dependencies) {
						graph.addEdge(null, vertex, graph.getVertex(dependency), "dependsOn");
					}
					vertex.setProperty("dependencies", dependencies);
				} else {
					vertex.setProperty("dependencies", Collections.emptySet());
				}

				graph.addEdge(null, root, vertex, "contains");

				number++;
			}
		}

		return graph;
	}
}
