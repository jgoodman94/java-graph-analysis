# java-graph-analysis
Library for determining various characteristics about graphs / networks.
Given the adjacency matrix of a simple graph as input, this library has the following functions:

**Undirected, unweighted graphs**
* Determine number of vertices
* Determine number of edges
* Determine the total degree of the graph
* Determine the max degree of the graph
* Determine if the graph is connected
* Determine the number of connected components
* Determine if there is a path between vertices u and v
* Determine if the graph has a cycle
  * Determine if the graph is a tree or forest
* Determine if the graph is bipartite

**Bipartite graphs**
* Determine size of max cardinality matching (Hopcroft-Karp implementation)
* Determine edge set of max cardinality matching (Hopcroft-Karp implementation)

**General, Non-Bipartite graphs**
* Determine size of max cardinality matching (Edmonds' Blossom implementation)
* Determine edge set of max cardinality matching (Edmonds' Blossom implementation)
* Determine if there is a matching that covers all max degree vertices

The library contains the following classes:
* Graph (representation of a graph)
* Edge (representation of an edge in the graph)
* DFS (depth first search)
* HopcroftKarp (custom implementation of the Hopcroft-Karp algorithm)
* Blossom (custom implementation of Edmonds' blossom algorithm)
* RandomGraph (random graph generator)
* DisjointSet (disjoint set data structure)

Currently, the graph has two instance variables that represent it: (1) an
adjacency matrix, and (2) an ArrayList of ArrayLists of Edge objects. At this
point, all functions utilize the second implementation, as it makes the code
much easier to understand, and because the latter implementation facilitates
 traversal along edges.

Note that several test input adjacency matrices are located in the test_input_graphs
directory. There, you will find several popular graphs.

There is a MatchStudents main that demonstrates a real application of this library: that is, it finds a maximum number of pairs of students that do not know one another, in order to organize a meal event to help students make friends.
