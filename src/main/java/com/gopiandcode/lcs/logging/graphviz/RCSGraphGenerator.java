package com.gopiandcode.lcs.logging.graphviz;

import com.google.common.collect.ImmutableList;
import com.gopiandcode.lcs.rcs.RCSBinaryClassifier;
import com.gopiandcode.lcs.rcs.RCSClassifier;
import org.anarres.graphviz.builder.GraphVizGraph;
import org.anarres.graphviz.builder.GraphVizScope;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class RCSGraphGenerator {
    private final RCSBinaryClassifier classifier;
    private final GraphVizGraph graph;

    public RCSGraphGenerator(RCSBinaryClassifier classifier) {
        this.classifier = classifier;
        this.graph = generateGraph();
    }


    public void writeToDot(String outpath) {
        try {
            graph.writeTo(new File(outpath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GraphVizGraph generateGraph() {
        GraphVizGraph graph = new GraphVizGraph();
        GraphVizScope scope = new GraphVizScope.Impl();

//        HashMap<RCSClassifier, Node> nodeMap = new HashMap<>();
        List<RCSClassifier> population = classifier.getPopulation();
        int i = 0;
        for (RCSClassifier rcsClassifier : population) {
            if (rcsClassifier.getOutput().isLeft()) {
                graph.node(scope, rcsClassifier).label(rcsClassifier.toString());
            } else {
                graph.node(scope, rcsClassifier).label(rcsClassifier.toString());
            }
        }

        ImmutableList<RCSClassifier> rcsClassifiers = RCSBinaryClassifier.RCS_CLASSIFIER_ORDERING.immutableSortedCopy(population);
        for (RCSClassifier rcsClassifier : population) {

            // if the classifier is an intermediate
            if (rcsClassifier.getOutput().isRight()) {
                for (RCSClassifier otherClassifier : rcsClassifiers) {
                    if (otherClassifier.matches(rcsClassifier.getOutput().right())) {
                        if(otherClassifier.getOutput().isLeft()) {
                            graph.edge(scope, rcsClassifier, otherClassifier);
                        } else {
                            graph.edge(scope, rcsClassifier, otherClassifier);
                        }
                        break;
                    }
                }
            }

       }
       System.out.println("Completed graph: ");
        return graph;
    }
}
