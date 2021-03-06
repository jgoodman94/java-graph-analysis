/*******************************************************************************
* Representation of a simple graph using two different representations:
* (1) An adjacency matrix
* (2) An ArrayList of ArrayLists of Edges
*
* Author: Jesse Goodman
******************************************************************************/

import java.io.IOException;
import java.util.*;

public class Graph {
    private int[][] adjMatrix;  // adj matrix; rep1 of graph
    private ArrayList<HashSet<Edge>> vertices; // list of buckets (vertices)
    private HashSet<Edge> edges;
    private int numVertices; // number of vertices in this graph
    private boolean[] touched; // keeps track of which vertices have been hit by current alg

    // construct graph, given 2D int adjacency matrix
    public Graph(int[][] adjMatrix) {
        this.adjMatrix = adjMatrix;
        this.vertices = new ArrayList<HashSet<Edge>>();
        this.edges = new HashSet<Edge>();
        this.numVertices = 0;

        Edge currEdge;
        HashSet<Edge> allEdges;

        // iterate over all vertices
        for (int i = 0; i < adjMatrix.length; i++) {
            this.numVertices++;

            allEdges = new HashSet<Edge>();

            // iterate over possible edges from current vertex
            for (int j = 0; j < adjMatrix[i].length; j++) {
                if (adjMatrix[i][j] == 0) {
                    continue;
                }
                else {
                    currEdge = new Edge(i, j, adjMatrix[i][j]);
                    allEdges.add(currEdge);
                    if (i >= j) // prevent edges being added twice
                    this.edges.add(currEdge);
                }
            }
            vertices.add(allEdges);
        }
    }

    // Checks if there exists a matching that covers all max degree vertices.
    // Double number of vertices in graph; make all new vertices pw complete
    // make all new vertices complete to all non-max degree vertices
    // Exists a PM in this new graph IFF exists a delta hitting matching in original
    public boolean HasDeltaHittingMatching() {
        int maxDegree = this.getMaxDegree();

        // new graph has double length
        int[][] extendedArray = new int[2*this.numVertices][2*this.numVertices];

        // copy over original graph
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                extendedArray[i][j] = this.adjMatrix[i][j];
            }
        }

        // make new vertices all pairwise complete
        for (int i = numVertices; i < numVertices*2; i++) {
            for (int j = numVertices; j < numVertices*2; j++) {
                if (i != j) extendedArray[i][j] = 1;
            }
        }

        // make all non-max degree vertices in original graph complete
        // to new vertices
        for (int i = 0; i < numVertices; i++) {
            int degree = 0;
            for (int j = 0; j < numVertices; j++) {
                if (extendedArray[i][j] != 0) degree++;
            }
            // current vertex does not have max degree
            // make complete to all new vertices
            if (degree < maxDegree) {
                for (int j = numVertices; j < 2*numVertices; j++) {
                    extendedArray[i][j] = 1;
                    extendedArray[j][i] = 1;
                }
            }
        }

        Graph extendedGraph = new Graph(extendedArray);
        Blossom blossom = new Blossom(extendedGraph);
        return blossom.getMaxMatchingSize() == numVertices;
    }

    private HashSet<Edge> GetNeighbors(int nodeIndex) {
        return vertices.get(nodeIndex);
    }

    // construct and return adjacency matrix of graph complement
    public int[][] GetGraphComplement() {
        int[][] complement = new int[this.numVertices][this.numVertices];
        for (int i = 0; i < this.numVertices; i++) {
            for (int j = 0; j < this.numVertices; j++) {
                if (i == j) {
                    complement[i][j] = 0;
                } else {
                    complement[i][j] = this.adjMatrix[i][j] == 0 ? 1 : 0;
                }
            }
        }
        return complement;
    }

    // construct and return adjacency matrix of line graph
    public int[][] GetLineGraph() {
        int numEdges = this.edges.size();
        int[][] lineGraph = new int[numEdges][numEdges];
        HashMap<Edge, Integer> EdgeMapIndex = new HashMap<Edge, Integer>();
        int u, v;
        HashSet<Edge> uNbrs, vNbrs;
        // create map from edge to integer
        int i = 0;
        for (Edge e : this.edges) {
            EdgeMapIndex.put(e, i);
            i++;
        }
        for (Edge e : this.edges) {
            int edgeIndex = EdgeMapIndex.get(e);
            u = e.v1();
            v = e.v2();
            uNbrs = GetNeighbors(u);
            vNbrs = GetNeighbors(v);
            for (Edge f : uNbrs) {
                int incidentEdgeIndex = EdgeMapIndex.containsKey(f) ?
                EdgeMapIndex.get(f) : EdgeMapIndex.get(f.rev());
                if (edgeIndex == incidentEdgeIndex) continue;
                lineGraph[edgeIndex][incidentEdgeIndex] = 1;
            }
            for (Edge f : vNbrs) {
                int incidentEdgeIndex = EdgeMapIndex.containsKey(f) ?
                EdgeMapIndex.get(f) : EdgeMapIndex.get(f.rev());
                if (edgeIndex == incidentEdgeIndex) continue;
                lineGraph[edgeIndex][incidentEdgeIndex] = 1;
            }
        }
        return lineGraph;
    }

    // convert adj matrix representation of a graph into the adjacency lists
    // representation of a graph
    public static LinkedList<HashSet<Edge>> adjMatrixToAdjLists(int[][] adjMatrix) {
        LinkedList<HashSet<Edge>> graph = new LinkedList<HashSet<Edge>>();
        HashSet<Edge> currVertex;

        // iterate over rows
        for (int i = 0; i < adjMatrix.length; i++) {
            currVertex = new HashSet<Edge>();
            // iterate over elements in row i
            for (int j = 0; j < adjMatrix[i].length; j++) {
                if (adjMatrix[i][j] != 0) {
                    currVertex.add(new Edge(i,j));
                }
            }
            graph.add(currVertex);
        }

        return graph;
    }

    // convert adjanceny lists representation of a graph into adj matrix
    // representation of a graph
    public static int[][] adjListsToAdjMatrix(List<HashSet<Edge>> graph) {
        int[][] adjMatrix = new int[graph.size()][graph.size()];
        HashSet<Edge> currVertex;
        // will allow constant time get operation
        ArrayList<HashSet<Edge>> aGraph = new ArrayList<HashSet<Edge>>(graph);

        for (int i = 0; i < graph.size(); i++) {
            currVertex = graph.get(i);
            for (Edge e : currVertex) {
                adjMatrix[i][e.v2()] = 1;
            }
        }

        return adjMatrix;
    }

    /* Get minimum spanning tree of graph, using Kruskal's algorithm
    still a work in progress */
    // public ArrayList<Edge> getMST() {
    //     this.touched = new boolean[numVertices];
    //
    //     HashSet<Edge> mst = new HashSet<Edge>();
    //     HashSet<Edge> someList = this.getEdges();
    //     Collections.sort(someList);
    //
    //
    //     int numEdges = someList.size();
    //     Edge currEdge;
    //     int v1;
    //     int v2;
    //
    //     for (int i = 0; i < numEdges; i++) {
    //         currEdge = someList.get(i);
    //         v1 = currEdge.v1();
    //         v2 = currEdge.v2();
    //         if (touched[v1] == true && touched[v2] == true)
    //         continue;
    //
    //         mst.add(currEdge);
    //         touched[v1] = true;
    //         touched[v2] = true;
    //     }
    //
    //     System.out.println(mst);
    //     return mst;
    // }

    // returns number of vertices in graph.
    public int getNumVertices() {
        return this.numVertices;
    }

    // returns number of edges in graph.
    public int getNumEdges() {
        return this.getSumDegrees() / 2;
    }

    // returns total sum of degrees in graph.
    public int getSumDegrees() {
        int totalDegree = 0;

        for (HashSet<Edge> edgeList : this.vertices) {
            totalDegree += edgeList.size();
        }

        return totalDegree;
    }

    // returns max degree of the graph.
    public int getMaxDegree() {
        int maxDegree = 0;

        for (HashSet<Edge> edgeList : vertices) {
            if (edgeList.size() > maxDegree)
            maxDegree = edgeList.size();
        }

        return maxDegree;
    }

    // is the graph connected?
    public boolean isConnected() {
        int numVertices = this.getNumVertices();
        if (numVertices <= 1) return true;

        DFS dfs = new DFS(this, 0);
        return (dfs.getNumVisited() == numVertices) ? true : false;
    }

    // how many connected components does this graph have?
    public int numComps() {
        int numComps = 0;
        int unvisited = 0;
        DFS dfs;

        boolean[] visited = new boolean[this.getNumVertices()];

        while ((unvisited = remainingVertex(visited)) != -1) {
            numComps++;
            dfs = new DFS(this, unvisited);
            visited = this.mergeVisited(visited, dfs.getVisited());
        }

        return numComps;
    }

    // checks whether there is a path between vertices u and v.
    // vertices indexed by an int, 0 thru numVertices - 1.
    // first row of input adj matrix is vertex 0,
    // second row of input adj matrix is vertex 1, etc.
    public boolean existsPath(int u, int v) {
        int numVertices = this.getNumVertices();

        if (u >= numVertices || v >= numVertices) {
            System.out.println("One of the input vertices does not exist.");
            return false;
        }

        DFS dfs = new DFS(this, u);
        return dfs.getVisited()[v];
    }

    // does this graph have a cycle?
    // Time complexity: |V|
    public boolean hasCycle() {
        int numVertices = this.getNumVertices();
        this.touched = new boolean[numVertices];

        // iterate over all vertices (technically components b.c of if statement)
        for (int i = 0; i < numVertices; i++) {
            if (this.touched[i] == true)
            continue;

            if (compHasCycles(i, -1))
            return true;
        }

        return false; // if we reached here, never found cycle
    }

    // is the graph a tree?
    public boolean isTree() {
        return !this.hasCycle() && this.isConnected();
    }

    // is the graph a forest?
    public boolean isForest() {
        return !this.hasCycle();
    }

    // gets bipartitions of the graph if bipartite. O/w, returns null
    // bipartitions organized such that vertex is labeled 0 if in one partition,
    // 1 if in the other
    // runs in O(|V(G)|) time
    public boolean[] getBipartitions() {
        int numVertices = this.getNumVertices();
        boolean[] visited = new boolean[numVertices];
        boolean[] bipartitions = new boolean[this.getNumVertices()];

        Queue<Integer> remVertices = new LinkedList<Integer>();

        int currVertex;
        int nbr;
        boolean currColor;

        // iterate over all connected components
        for (int i = 0; i < numVertices; i++) {
            if (visited[i]) continue;

            bipartitions[i] = true;
            remVertices.add(i);
            visited[i] = true;

            // iterate over all vertices connected to vertex i, using BFS
            while (!remVertices.isEmpty()) {
                currVertex = remVertices.remove();
                currColor = bipartitions[currVertex];

                // iterate over neighbors of current vertex
                for (Edge e : this.getVertices().get(currVertex)) {
                    nbr = e.v2();

                    if (visited[nbr]) {
                        if (bipartitions[nbr] != !currColor)
                        return null;
                        else
                        continue;
                    }

                    remVertices.add(nbr);
                    visited[nbr] = true;

                    bipartitions[nbr] = !currColor;
                }
            }
        }

        return bipartitions;
    }

    // get symmetric difference of two sets of edges
    public static HashSet<Edge> symDiff(HashSet<Edge> edges1,
    HashSet<Edge> edges2) {
        HashSet<Edge> symDiff = new HashSet<Edge>();
        // Edge mirror;
        for (Edge e : edges1) {
            // mirror = new Edge(e.v2(), e.v1(), 1);
            if (edges2.contains(e) || edges2.contains(e.rev())) continue;
            symDiff.add(e);
        }
        for (Edge e : edges2) {
            // mirror = new Edge(e.v2(), e.v1(), 1);
            if (edges1.contains(e) || edges1.contains(e.rev())) continue;
            symDiff.add(e);
        }
        return symDiff;
    }

    // check if two matchings are the same, mainly a test method
    public static boolean equivMatchings(HashSet<Edge> m1, HashSet<Edge> m2) {
        for (Edge e : m1) {
            if (!m2.contains(e) && !m2.contains(e.rev())) {
                return false;
            }
        }
        for (Edge e : m2) {
            if (!m1.contains(e) && !m1.contains(e.rev())) {
                return false;
            }
        }
        return true;
    }

    // what is the chromatic number of this graph?
    // that is, what is the minimum amount of colors we can use to
    // color this graph?
    public int chromaticNum() {
        return -1;
    }

    /* ACCESSOR METHODS */
    // return 2D int array representation of graph
    public int[][] getAdjMatrix() {
        return this.adjMatrix;
    }

    // return Array List of vertices ("vertices of edges")
    public ArrayList<HashSet<Edge>> getVertices() {
        return this.vertices;
    }

    // return Array list of edges
    public HashSet<Edge> getEdges() {
        return this.edges;
    }

    /* PRIVATE HELPER METHODS */

    // does the connected component connected to vertex u have a cycle?
    // keep track of previous node too. (recursive)
    private boolean compHasCycles(int u, int prev) {

        if (this.touched[u] == true) {
            return true; // hits here if this vertex has been touched (i.e. there's a cycle)
        }

        this.touched[u] = true;

        for (Edge e : this.getVertices().get(u)) {
            int neighbor = e.v2();
            if (neighbor == prev)
            continue;
            if (compHasCycles(neighbor, u))
            return true;
        }

        return false; // hits here if no neighbors (no cycle yet)
    }

    // merges two visited arrays into a single visited array
    private boolean[] mergeVisited(boolean[] visited1, boolean[] visited2) {
        if (visited1.length != visited2.length) {
            System.out.println("Visited array size mismatch.");
            return null;
        }

        int numVertices = visited1.length;
        boolean[] merged = new boolean[numVertices];

        for (int i = 0; i < numVertices; i++)
        if (visited1[i] == true || visited2[i] == true)
        merged[i] = true;

        return merged;
    }

    // checks if visited array is completely true
    // returns -1 if no remaining vertex to be visited
    // otherwise, returns index of some remaining vertex
    private static int remainingVertex(boolean[] visited) {
        for (int i = 0; i < visited.length; i++)
        if (visited[i] == false)
        return i;

        return -1;
    }

    // loads and returns 2d adjacency matrix from standard in
    public static int[][] loadMatrixFromStdIn() {
        int[][] adjMatrix;
        String stdinLine;
        String[] stdinArray;
        int dim;
        Scanner stdin = new Scanner(System.in);

        // need this to get dimension of adj matrix that we're going to fill
        stdinLine = stdin.nextLine();
        stdinArray = stdinLine.split("\\s+");
        dim = stdinArray.length;

        if (dim <= 0)
        return null;

        adjMatrix = new int[dim][dim];

        // load adjMatrix
        for (int i = 0; i < dim; i++) {

            for (int j = 0; j < dim; j++)
            adjMatrix[i][j] = Integer.parseInt(stdinArray[j]);

            if (stdin.hasNextLine()){
                stdinLine = stdin.nextLine();
                stdinArray = stdinLine.split("\\s+");
            }
        }

        return adjMatrix;
    }

    // string representation of graph
    public String toString() {
        StringBuilder adjMatrixStr = new StringBuilder();

        // adjacency matrix representation
        adjMatrixStr.append("**********************************************\n\n");
        for (int[] row : this.adjMatrix) {
            for (int entry : row) {
                adjMatrixStr.append(entry + " ");
            }
            adjMatrixStr.append("\n");
        }
        adjMatrixStr.append("\n**********************************************\n\n");

        // bipartitions
        boolean[] parts = this.getBipartitions();
        if (parts != null) {
            StringBuilder part1 = new StringBuilder("(");
            StringBuilder part2 = new StringBuilder("(");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i]) part1.append("  " + i + "  ");
                else part2.append("  " + i + "  ");
            }
            part1.append(")");
            part2.append(")");
            adjMatrixStr.append("Bipartitions:\n");
            adjMatrixStr.append(part1).append(" , ").append(part2).append("\n\n");
            adjMatrixStr.append("**********************************************\n\n");
        }

        StringBuilder edges = new StringBuilder();
        for (Edge e : this.edges) {
            edges.append(e).append("\n");
        }
        edges.append("\n**********************************************");
        return adjMatrixStr.append(edges).toString();
    }

    // unit testing
    public static void main(String[] args) throws java.io.IOException {
        int[][] adjMatrix = loadMatrixFromStdIn();

        // unit tests
        // RandomGraph rg = new RandomGraph();

        Graph g = new Graph(adjMatrix);
        System.out.println("Does this graph have a matching that covers " +
            "all max degree vertices?");
        System.out.println(g.HasDeltaHittingMatching());
        // Graph gc = new Graph(g.GetGraphComplement());
        // Graph lg = new Graph(g.GetLineGraph());
        //
        // System.out.println("!!!!!!!!!! ORIGINAL GRAPH !!!!!!!!!!");
        // System.out.println(g);
        // System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        //System.out.println("!!!!!!!!!! COMPLEMENT GRAPH !!!!!!!!");
        //System.out.println(gc);
        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // System.out.println("!!!!!!!!!!!! LINE GRAPH !!!!!!!!!!!!");
        // System.out.println(lg);
        // System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // System.out.println(g);
        // Graph g = rg.getBipartite(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
        // System.out.println("This graph has " + g.getNumVertices() + " vertices.");
        // System.out.println("This graph has total degree " + g.getSumDegrees());
        // System.out.println("This graph has max degree " + g.getMaxDegree());
        // System.out.println("This graph has " + g.getNumEdges() + " edges.");
        // System.out.println("Is this graph connected? " + g.isConnected());
        // System.out.println("How many components does this graph have? " + g.numComps());
        // System.out.println("Is there a path between vertex 0 and 4? " + g.existsPath(0,4));
        // System.out.println("Is there a cycle? " + g.hasCycle());
        // for (Edge e : g.getEdges())
        // System.out.println(e.toString());

        // System.out.println(Collections.sort(g.getEdges(), new ArrayList<Edge>()));

        // boolean[] bipartitions = g.getBipartitions();
        // if (bipartitions == null) {
        //     System.out.println("Graph is not bipartite.");
        // }
        // else {
        //     System.out.println("bipartitions are:");
        //     for (boolean b : bipartitions) {
        //         System.out.println(b);
        //     }
        // }

        // ArrayList<HashSet<ArrayList<Edge>>> levels = new ArrayList<HashSet<ArrayList<Edge>>>();
        // levels.get(0);
    }
}
