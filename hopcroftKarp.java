/*******************************************************************************
* Hopcroft-Karp algorithm for finding maximum matchings in bipartite graphs.
* Time complexity: O(n^(5/2)), where n is the number of vertices in the graph.
*
* Author: Jesse Goodman
******************************************************************************/
import java.util.*;

public class hopcroftKarp {

    private HashSet<Edge> maxMatching;
    private boolean[] matchedVertices;
    private boolean[] freeBoys; // free vertices in first partition
    private boolean[] partitions;
    private Graph g;
    private static HashSet<Edge> augAcc; // augmenting path accumulator

    // representation of gHat graph. arraylist of buckets (levels) of vertices
    // each hashmap represents a level. each vertex has a label
    private static ArrayList<HashMap<Integer, HashSet<Edge>>> gHat;

    // run hopcroft karp algorithm for maximum matchings in a bipartite graph
    public hopcroftKarp(Graph g) {
        this.g = g;
        // is this vertex a girl?
        // thus, false == boys
        this.partitions = g.getBipartitions();
        if (this.partitions == null) {
            throw new IllegalArgumentException("Input must be the adjacency matrix of a bipartite graph.");
        }
        this.maxMatching = new HashSet<Edge>();
        this.matchedVertices = new boolean[this.g.getNumVertices()];
        this.gHat = new ArrayList<HashMap<Integer, HashSet<Edge>>>();

        // put max matching into global variable
        int result = 1;
        // no more than sqrt(|V(G)|) iterations
        while (result > 0) {
            System.out.println("IN HEREEEEE THO");
            // get another augmenting graph, and then symdiff all new matchings
            // from that grpah into our current matching
            if (setNewGHat() == null) break;
            result = augmentMatching();
            System.out.println("result is : " + result);
        }
    }


    // returns the edge set of the max cardinality matching of this graph
    public HashSet<Edge> getMaxMatching() {
        return this.maxMatching;
    }

    // returns the size of the max cardinality matching of this graph
    public int getMaxMatchingSize() {
        return this.maxMatching.size();
    }

    // build graph G^hat of G with least
    // G^hat represented as an array list of levels (pools of vertices)
    // similar to a BFS
    // levels are built by filtering neighbrs of vertices in original graph
    // levels will only have edges going forward
    private ArrayList<HashMap<Integer, HashSet<Edge>>> setNewGHat() {
        System.out.println("!!!!!!!!in set new ghat!!!!!!!!");
        System.out.println(maxMatching + "    (matching now)");
        ArrayList<HashMap<Integer, HashSet<Edge>>> levels =
        new ArrayList<HashMap<Integer, HashSet<Edge>>>();

        boolean[] visited = new boolean[this.g.getNumVertices()];
        boolean foundFreeGirl = false;

        // populate first level -- free boys
        HashMap<Integer, HashSet<Edge>> level_0 =
        new HashMap<Integer, HashSet<Edge>>();

        for (int i = 0; i < partitions.length; i++) {
            if (!partitions[i]) continue; // vertex is a girl
            if (matchedVertices[i]) continue; //vertex already matched

            // found free boy
            HashSet<Edge> freeBoy = new HashSet<Edge>();
            visited[i] = true;

            // decide which edges to include
            for (Edge e : this.g.getVertices().get(i)) {
                // only add edges that are not in matching
                // (looking for alternate path)
                if (!maxMatching.contains(e)) freeBoy.add(e);
            }
            level_0.put(i, freeBoy);
        }
        // if no free boys left
        if (level_0.size() == 0) return null;
        levels.add(level_0);
        // create levels
        for (int i = 1; !foundFreeGirl; i++) {
            System.out.println("new level: " + i);
            HashMap<Integer, HashSet<Edge>> level = new HashMap<Integer, HashSet<Edge>>();
            // get all vertices to put into new level
            // aka, vertices adjacent to vertices from previous level
            for (HashSet<Edge> nbrs : levels.get(i-1).values()) {
                // iterate over neighbors of some vertex in the previous levels
                for (Edge e : nbrs) {
                    int vi = e.v2(); // label of current vertex being examined

                    // found free girl
                    if (!matchedVertices[vi] && (i % 2 == 1)) {
                        foundFreeGirl = true;
                    }

                    // once we've found a free girl, no need for any more forward edges
                    if (foundFreeGirl) continue;

                    HashSet<Edge> newVertex = new HashSet<Edge>();
                    visited[vi] = true;

                    // filter neighbors of vertex in original graph
                    // which neighbors to add to new vertex?
                    for (Edge j : this.g.getVertices().get(vi)) {
                        System.out.println(j.v1() + " == to == " + j.v2());
                        int vj = j.v2();
                        if (visited[vj] || foundFreeGirl) continue;
                        System.out.println("uh -- checking for  " + j);
                        // do we want a matching edge for our alternating path?
                        // yes -- odd level
                        Edge mirror_j = new Edge(j.v2(), j.v1(), 1);
                        if (i % 2 == 1) {
                            if (maxMatching.contains(j) || maxMatching.contains(mirror_j)) {
                                newVertex.add(j);
                            }
                        }
                        // no
                        else {
                            if (!maxMatching.contains(j) && !maxMatching.contains(mirror_j)) {
                                newVertex.add(j);
                            }
                        }
                    }
                    level.put(vi, newVertex);
                }
            }

            if (level.size() == 0 && !foundFreeGirl) {
                return null;
            }

            levels.add(level);
        }
        gHat = levels;
        System.out.println(gHat + "         (just generated ghat)");
        return levels;
    }

    // get a min augmenting path from g hat
    private static HashSet<Edge> minAugPathFromGHat() {
        System.out.println("in min aug path from ghat");
        // System.out.println(gHat);
        augAcc = new HashSet<Edge>(); // reset accumulator global

        HashMap<Integer, HashSet<Edge>> freeBoys = gHat.get(0);

        for (int freeBoy : freeBoys.keySet()) {
            // also updates augAcc
            if (hasPathToGirl(freeBoy, 0)) {
                System.out.println(gHat);
                removeAugPathFromGHat(augAcc);
                System.out.println(gHat);
                return augAcc;
            }
        }

        // g hat is exhausted of augmenting paths
        return null;
    }

    // is there a path from vertex v to a free girl? use DFS
    private static boolean hasPathToGirl(int v, int lvl) {
        System.out.println("--hasPathToGirl--(" + v + "," + lvl + ")");
        if (lvl == gHat.size() - 1) {
            return true;
        }

        // each neighbor of given vertex
        for (Edge e : gHat.get(lvl).get(v)) {
            if (hasPathToGirl(e.v2(), lvl+1)) {
                augAcc.add(e);
                return true;
            }
        }

        return false;
    }

    // remove augPath from ghat (delete edges)
    private static void removeAugPathFromGHat(HashSet<Edge> augPath) {
        boolean inMatching;
        HashSet<Integer> augPathVs = new HashSet<Integer>();

        // initialize vertices along augmented path
        for (Edge e : augPath) {
            augPathVs.add(e.v1());
            augPathVs.add(e.v2());
        }

        System.out.println("===================================================");
        System.out.println(augPathVs);
        System.out.println("===================================================");

        for (HashMap<Integer, HashSet<Edge>> level : gHat) {

            Iterator<Map.Entry<Integer, HashSet<Edge>>> iter = level.entrySet().iterator();
            while (iter.hasNext()) {
                inMatching = false;

                Map.Entry<Integer, HashSet<Edge>> vertex = iter.next();
                int v = vertex.getKey();

                // delete all vertices along the aug path
                HashSet<Edge> nbrs = vertex.getValue();
                if (augPathVs.contains(v)) {
                    iter.remove();
                    continue;
                }

                // delete all edges with one vertex along the aug path
                Iterator<Edge> iterE = nbrs.iterator();
                while (iterE.hasNext()) {
                    Edge e = iterE.next();
                    if (augPathVs.contains(e.v2())) {
                        iterE.remove();
                    }
                }
            }
        }
    }

    // M' = M (+) A1 (+) A2 (+) ... (+) An, where Ai is a min augmenting path
    // in a maximal set (with n elements) of min augmenting paths. Note that
    // (+) represents symmetric difference, M represents the old matching,
    // and M' represents the augmented matching.
    // One iteration of augmenting matching thru maximal set of min
    // augmenting paths
    // returns 0 if cannot augment matching
    private int augmentMatching() {
        System.out.println("in augment matching");
        int timesAugmented = 0;

        HashSet<Edge> augPath = minAugPathFromGHat();

        // continue until there are no aug paths left in maximal set of
        // minimum augmenting paths
        while (augPath != null) {
            // System.out.println("--------AUG MATCHING--------");
            timesAugmented++;
            this.maxMatching = symDiff(this.maxMatching, augPath);
            updateMatchedVertices();
            augPath = minAugPathFromGHat();
        }

        return timesAugmented;
    }

    private void updateMatchedVertices() {
        int v1;
        int v2;
        this.matchedVertices = new boolean[this.g.getNumVertices()];
        for (Edge e : this.maxMatching) {
            v1 = e.v1();
            v2 = e.v2();
            this.matchedVertices[v1] = true;
            this.matchedVertices[v2] = true;
        }
    }

    // get symmetric difference of two sets of edges
    public static HashSet<Edge> symDiff(HashSet<Edge> edges1,
    HashSet<Edge> edges2) {
        HashSet<Edge> symDiff = new HashSet<Edge>();
        Edge mirror;
        for (Edge e : edges1) {
            mirror = new Edge(e.v2(), e.v1(), 1);
            if (edges2.contains(e) || edges2.contains(mirror)) continue;
            symDiff.add(e);
        }
        for (Edge e : edges2) {
            mirror = new Edge(e.v2(), e.v1(), 1);
            if (edges1.contains(e) || edges1.contains(mirror)) continue;
            symDiff.add(e);
        }
        return symDiff;
    }


    // String representation of result
    public String toString() {
        StringBuilder edges = new StringBuilder();
        for (Edge e : this.getMaxMatching()) {
            edges.append(e).append("\n");
        }
        return "--------------------------------------------------\n" +
        "Max matching size:\n" + this.getMaxMatchingSize() + "\n\n" +
        "Illustration:\n" +
        edges +
        "--------------------------------------------------";
    }

    // unit testing
    public static void main(String[] args) {
        int[][] adjMatrix = Graph.loadMatrixFromStdIn();
        Graph g = new Graph(adjMatrix);
        hopcroftKarp hk = new hopcroftKarp(g);

        System.out.println(hk);
    }

}
