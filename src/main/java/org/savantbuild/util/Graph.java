/*
 * Copyright (c) 2001-2006, Inversoft, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This interface defines the generic graph data structure.
 * <p/>
 * <h3>Graphs</h3>
 * <p/>
 * Graphs are simple structures that model nodes with any number of connections between nodes. The connections are
 * bi-directional and are called Edges. A two node graph with an edge between the nodes looks like this:
 * <p/>
 * <pre>
 * node1 <---> node2
 * </pre>
 * <p/>
 * The important point about Graphs is that implementations don't need to enforce a top level node that controls the
 * entire structure like trees do. Instead, implementations can choose to have the graph store all of the nodes and the
 * connections between them allowing for direct access to any node. These implementations should define how the direct
 * access is managed and whether or not duplicate nodes can be stored.
 * <p/>
 * <h3>Generics</h3>
 * <p/>
 * There are two generics for a Graph. The first variable T is the content of the nodes themselves. Each node can stored
 * a single value. The second generic is the value that can be associated with the Edge between nodes. This is carried
 * throughout the entire graph structure making it very strongly typed.
 *
 * @author Brian Pontarelli
 */
public interface Graph<T, U> {
  /**
   * Adds an edge between the node whose value is the origin value given and the node whose value is the destination
   * value given. This method works well for implementations that only allow values to exist once.
   * <p/>
   * If there are no nodes for the given value, this method should create nodes for each value and then create an edge
   * between them. This reduces the work required to edge values.
   *
   * @param origin      The origin value that may or may not exist in the graph.
   * @param destination The destination value that may or may not exist in the graph.
   * @param edgeValue   The value to associate with the edge.
   */
  void addEdge(T origin, T destination, U edgeValue);

  /**
   * Determines if the Graph contains the given value or not.
   *
   * @param value The value.
   * @return True if the value is in the graph, false otherwise.
   */
  boolean contains(T value);

  /**
   * Returns a list of all the inbound edges for the node whose value is given. This locates the first node with the
   * value.
   *
   * @param value The value to find the edges for.
   * @return The edges or an empty list if the node exists and has no edges or null if the node does not exist.
   */
  List<Edge<T, U>> getInboundEdges(T value);

  /**
   * Returns a list of all the outbound edges for the node whose value is given. This locates the first node with the
   * value.
   *
   * @param value The value to find the edges for.
   * @return The edges or an empty list if the node exists and has no edges or null if the node does not exist.
   */
  List<Edge<T, U>> getOutboundEdges(T value);

  /**
   * Determines the path from the given origin value to given destination value.
   *
   * @param origin      The origin value.
   * @param destination The destination value.
   * @return A list of all the paths between the two nodes or an empty list if there are none or null if either of the
   *         nodes don't exist.
   */
  List<Path<T>> getPaths(T origin, T destination);

  /**
   * Removes all empty nodes from the graph except the given list of nodes.
   *
   * @param excludes The nodes to exclude from pruning.
   */
  @SuppressWarnings("unchecked")
  void prune(T... excludes);

  /**
   * Removes the edge between the two nodes from the graph.
   *
   * @param origin      The origin value.
   * @param destination The destination value.
   * @param value       The edge value.
   */
  void removeEdge(T origin, T destination, U value);

  /**
   * Removes the given value (node) from the graph and ensures that nothing is orphaned by the removal.
   *
   * @param value The value to remove.
   * @throws CyclicException If the graph has any cycles in it.
   */
  void removeNode(T value) throws CyclicException;

  /**
   * @return The size of the graph (number of nodes).
   */
  int size();

  /**
   * Traverses the graph in a depth-first manner starting at the node whose value is given. The GraphConsumer is called
   * for each edge in the graph.
   *
   * @param rootValue The value of the node to start the traversal from.
   * @param consumer  The GraphConsumer that is called for each edge.
   */
  void traverse(T rootValue, GraphConsumer<T, U> consumer);

  /**
   * Returns a Set that contains all of the unique values contained in the graph.
   *
   * @return All the values.
   */
  Set<T> values();

  /**
   * Interface for edges in the graph. This interface is the edge information that is used by users of a Graph and often
   * is not used internally to the graph itself. It abstracts the implementation details of the graph from the user.
   *
   * @param <T> The node value type.
   * @param <U> The edge value type.
   */
  public static interface Edge<T, U> {
    /**
     * @return The destination node value.
     */
    public T getDestination();

    /**
     * @return The origin node value.
     */
    public T getOrigin();

    /**
     * @return The edge value.
     */
    public U getValue();

    /**
     * Basic implementation of the Edge interface. Provides public final fields and public accessors.
     *
     * @param <T> The node value type.
     * @param <U> The edge value type.
     */
    public static class BaseEdge<T, U> implements Edge<T, U> {
      public final T destination;

      public final T origin;

      public final U value;

      public BaseEdge(T origin, T destination, U value) {
        this.origin = origin;
        this.destination = destination;
        this.value = value;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }

        final BaseEdge baseEdge = (BaseEdge) o;
        return destination.equals(baseEdge.destination) && origin.equals(baseEdge.origin) && value.equals(baseEdge.value);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public T getDestination() {
        return destination;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public T getOrigin() {
        return origin;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public U getValue() {
        return value;
      }

      @Override
      public int hashCode() {
        int result = destination.hashCode();
        result = 31 * result + origin.hashCode();
        result = 31 * result + value.hashCode();
        return result;
      }

      public String toString() {
        return origin + "-(" + value + ")->" + destination;
      }
    }
  }

  /**
   * Consumer interface for graph traversal.
   *
   * @param <T> The node value type.
   * @param <U> The edge value type
   */
  public static interface GraphConsumer<T, U> {
    /**
     * Called by the graph during traversal to handle each node in the graph. The parameters passed in constitute an
     * edge between two nodes. This method will not visit detached clusters of nodes.
     *
     * @param origin      The origin node value.
     * @param destination The destination node value.
     * @param edgeValue   The edge value.
     * @param depth       The current depth in the graph.
     * @return True if the traversal should continue down from the destination node. False if the traversal should exit
     *         and resume from the origin node.
     */
    public boolean consume(T origin, T destination, U edgeValue, int depth);
  }

  /**
   * Defines a path between two nodes in the graph.
   *
   * @param <T> The node type.
   */
  public static interface Path<T> {
    /**
     * @return The path.
     */
    public List<T> get();

    /**
     * A simple implementation for the Path interface. This takes a constructor parameter that is the Path list and
     * shallow copies it to a new unmodifiable LinkedList.
     *
     * @param <T> The node value type.
     */
    public static class BasePath<T> implements Path<T> {
      private final List<T> path;

      public BasePath(List<T> path) {
        this.path = Collections.unmodifiableList(new LinkedList<>(path));
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }

        final BasePath basePath = (BasePath) o;
        return path.equals(basePath.path);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public List<T> get() {
        return path;
      }

      @Override
      public int hashCode() {
        return path.hashCode();
      }
    }
  }
}
