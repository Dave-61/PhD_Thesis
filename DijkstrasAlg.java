package PhD3;// Dijkstra's single source shortest path algorithm:
// Given a graph, a source vertex and a target vertex in the graph,
// find shortest path from the source to the target in the given graph.
// The program is for adjacency matrix representation of the graph.

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DijkstrasAlg {

    private static final int NO_PARENT = -1;

    // Function that implements Dijkstra's single source shortest path algorithm
    // for a graph represented using adjacency matrix representation
    static List<Double> dijkstra(double[][] adjacencyMatrix, int startVertex, int endVertex) {
        int nVertices = adjacencyMatrix[0].length;

        // shortestDistances[i] will hold the shortest distance from src to i
        double[] shortestDistances = new double[nVertices];

        // added[i] will true if vertex i is included in shortest path tree,
        // or shortest distance from src to i is finalized
        boolean[] added = new boolean[nVertices];

        // Initialize all distances as INFINITE and added[] as false
        for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
            shortestDistances[vertexIndex] = Integer.MAX_VALUE;
            added[vertexIndex] = false;
        }

        // Distance of source vertex from itself is always 0
        shortestDistances[startVertex] = 0;

        // Parent array to store shortest path tree
        int[] parents = new int[nVertices];

        // The starting vertex does not have a parent
        parents[startVertex] = NO_PARENT;

        // Find shortest path for all vertices
        // SINGLE TARGET (the for loop breaks when the picked minimum distance vertex is equal to target)
        for (int i = 1; i < nVertices; i++) {
            // Pick the minimum distance vertex from the set of vertices not yet processed.
            // nearestVertex is always equal to startNode in first iteration.
            int nearestVertex = -1;
            double shortestDistance = Integer.MAX_VALUE;
            for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
                if (!added[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance) {
                    nearestVertex = vertexIndex;
                    shortestDistance = shortestDistances[vertexIndex];

                    if (nearestVertex == endVertex) // SINGLE TARGET
                        break;

                }
            }

            if (nearestVertex == endVertex) // SINGLE TARGET
                break;

            // Mark the picked vertex as processed
            added[nearestVertex] = true;

            // Update dist value of the adjacent vertices of the picked vertex.
            for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
                double edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];

                if (edgeDistance >= 0 && ((shortestDistance + edgeDistance) < shortestDistances[vertexIndex])) {
                    parents[vertexIndex] = nearestVertex;
                    shortestDistances[vertexIndex] = shortestDistance + edgeDistance;
                }
            }
        }

        printSolution(startVertex, shortestDistances, parents, endVertex);

        List<Double> routeR = new ArrayList<>();
        int currentVertex = endVertex;
        for (int ignored : parents) {
            routeR.add((double) currentVertex);
            currentVertex = parents[currentVertex];
            if (currentVertex == NO_PARENT) {
                break;
            }
        }
        routeR.add(shortestDistances[endVertex]);
        List<Double> route = routeR.subList(0, routeR.size());
        Collections.reverse(route);
        //System.out.println(route);
        return route;
    }

    // A utility function to print the constructed distances array and shortest path // SINGLE TARGET
    private static void printSolution(int startVertex, double[] distances, int[] parents, int endVertex) {
        //System.out.print("Vertex\t\t\t Distance\t\t\t\t Path");
        //System.out.print("\n" + startVertex + " -> ");
        //System.out.print(endVertex + " \t\t ");
        //System.out.print(distances[endVertex] + "\t\t");
        printPath(endVertex, parents);
        //System.out.println();
    }

    // Function to print shortest path from source to currentVertex using parents array
    private static void printPath(int currentVertex, int[] parents) {

        // Base case : Source node has been processed
        if (currentVertex == NO_PARENT) {
            return;
        }
        printPath(parents[currentVertex], parents);
        //System.out.print(currentVertex + " ");
    }

}