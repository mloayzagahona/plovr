/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An undirected graph using linked list within nodes to store edge
 * information.
 *
*
 *
 * @param <N> Value type that the graph node stores.
 * @param <E> Value type that the graph edge stores.
 */
public class LinkedUndirectedGraph<N, E>
    extends UndiGraph<N, E> implements GraphvizGraph {
  protected final Map<N, LinkedUndirectedGraphNode<N, E>> nodes =
      Maps.newHashMap();

  public SubGraph<N, E> newSubGraph() {
    return new SimpleSubGraph<N, E>(this);
  }

  @Override
  public GraphEdge<N, E> connect(N srcValue, E edgeValue, N destValue) {
    UndiGraphNode<N, E> node = getUndirectedGraphNode(srcValue);
    if (node == null) {
      throw new IllegalArgumentException(
          srcValue + " does not exist in graph");
    }
    LinkedUndirectedGraphNode<N, E> src =
        (LinkedUndirectedGraphNode<N, E>) node;
    node = getUndirectedGraphNode(destValue);
    if (node == null) {
      throw new IllegalArgumentException(
          destValue + " does not exist in graph");
    }
    LinkedUndirectedGraphNode<N, E> dest =
        (LinkedUndirectedGraphNode<N, E>) node;
    LinkedUndirectedGraphEdge<N, E> edge =
        new LinkedUndirectedGraphEdge<N, E>(src, edgeValue, dest);
    src.getNeighborEdges().add(edge);
    dest.getNeighborEdges().add(edge);
    return edge;
  }

  @Override
  public void disconnect(N srcValue, N destValue) {
    UndiGraphNode<N, E> node = getUndirectedGraphNode(srcValue);
    if (node == null) {
      throw new IllegalArgumentException(
          srcValue + " does not exist in graph");
    }
    LinkedUndirectedGraphNode<N, E> src = 
      (LinkedUndirectedGraphNode<N, E>) node;
    node = getUndirectedGraphNode(destValue);
    if (node == null) {
      throw new IllegalArgumentException(
          destValue + " does not exist in graph");
    }
    LinkedUndirectedGraphNode<N, E> dest = 
      (LinkedUndirectedGraphNode<N, E>) node;

    for (UndiGraphEdge<N, E> edge : 
      getUndirectedGraphEdges(srcValue, destValue)) {
      src.getNeighborEdges().remove(edge);
      dest.getNeighborEdges().remove(edge);
    }
  }

  @Override
  public UndiGraphNode<N, E> createUndirectedGraphNode(
      N nodeValue) {
    LinkedUndirectedGraphNode<N, E> node = nodes.get(nodeValue);
    if (node == null) {
      node = new LinkedUndirectedGraphNode<N, E>(nodeValue);
      nodes.put(nodeValue, node);
    }
    return node;
  }

  @Override
  public List<GraphNode<N, E>> getNeighborNodes(N value) {
    UndiGraphNode<N, E> uNode = getUndirectedGraphNode(value);
    List<GraphNode<N, E>> nodeList = Lists.newArrayList();
    for (Iterator<GraphNode<N, E>> i = getNeighborNodesIterator(value);
        i.hasNext();) {
      nodeList.add(i.next());
    }
    return nodeList;
  }
  
  @Override
  public Iterator<GraphNode<N, E>> getNeighborNodesIterator(N value) {
    UndiGraphNode<N, E> uNode = getUndirectedGraphNode(value);
    Preconditions.checkNotNull(uNode, value + " should be in the graph.");
    return ((LinkedUndirectedGraphNode<N, E>) uNode).neighborIterator();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<UndiGraphEdge<N, E>> getUndirectedGraphEdges(N n1, N n2) {
    UndiGraphNode<N, E> dNode1 = nodes.get(n1);
    if (dNode1 == null) {
      return null;
    }
    UndiGraphNode<N, E> dNode2 = nodes.get(n2);
    if (dNode2 == null) {
      return null;
    }
    List<UndiGraphEdge<N, E>> edges = Lists.newArrayList();
    for (UndiGraphEdge<N, E> outEdge : dNode1.getNeighborEdges()) {
      if (outEdge.getNodeA() == dNode2 || outEdge.getNodeB() == dNode2) {
        edges.add(outEdge);
      }
    }
    return edges;
  }

  @Override
  public UndiGraphNode<N, E> getUndirectedGraphNode(N nodeValue) {
    return nodes.get(nodeValue);
  }

  @Override
  public List<UndiGraphNode<N, E>> getUndirectedGraphNodes() {
    List<UndiGraphNode<N, E>> nodeList = Lists.newArrayList();
    nodeList.addAll(nodes.values());
    return nodeList;
  }

  @Override
  public GraphNode<N, E> createNode(N value) {
    return createUndirectedGraphNode(value);
  }

  @Override
  public List<GraphEdge<N, E>> getEdges(N n1, N n2) {
    List<GraphEdge<N, E>> result = Lists.newArrayList();
    result.addAll(getUndirectedGraphEdges(n1, n2));
    return result;
  }

  @Override
  public GraphNode<N, E> getNode(N value) {
    return getUndirectedGraphNode(value);
  }

  @Override
  public boolean isConnected(N n1, N n2) {
    UndiGraphNode<N, E> dNode1 = nodes.get(n1);
    if (dNode1 == null) {
      return false;
    }
    UndiGraphNode<N, E> dNode2 = nodes.get(n2);
    if (dNode2 == null) {
      return false;
    }
    for (UndiGraphEdge<N, E> outEdge : dNode1.getNeighborEdges()) {
      if ((outEdge.getNodeA() == dNode1 && outEdge.getNodeB() == dNode2) ||
          (outEdge.getNodeA() == dNode2 && outEdge.getNodeB() == dNode1)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<GraphvizEdge> getGraphvizEdges() {
    List<GraphvizEdge> edgeList = Lists.newArrayList();
    for (LinkedUndirectedGraphNode<N, E> node : nodes.values()) {
      for (UndiGraphEdge<N, E> edge : node.getNeighborEdges()) {
        if (edge.getNodeA() == node) {
          edgeList.add((GraphvizEdge) edge);
        }
      }
    }
    return edgeList;
  }

  @Override
  public String getName() {
    return "LinkedUndirectedGraph";
  }

  @Override
  public List<GraphvizNode> getGraphvizNodes() {
    List<GraphvizNode> nodeList =
        Lists.newArrayListWithCapacity(nodes.size());
    for (LinkedUndirectedGraphNode<N, E> node : nodes.values()) {
      nodeList.add(node);
    }
    return nodeList;
  }

  @Override
  public boolean isDirected() {
    return false;
  }

  @Override
  public List<GraphNode<N, E>> getNodes() {
    List<GraphNode<N, E>> list = Lists.newArrayList();
    list.addAll(nodes.values());
    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<GraphEdge<N, E>> getEdges() {
    List<GraphEdge<N, E>> result = Lists.newArrayList();
    for (LinkedUndirectedGraphNode<N, E> node : nodes.values()) {
      for (UndiGraphEdge<N, E> edge : node.getNeighborEdges()) {
        if (edge.getNodeA() == node) {
          result.add(edge);
        }
      }
    }
    return result;
  }

  @Override
  public int getNodeDegree(N value) {
    UndiGraphNode<N, E> uNode = getUndirectedGraphNode(value);
    if (uNode == null) {
      throw new IllegalArgumentException(value + " not found in graph");
    }
    return uNode.getNeighborEdges().size();
  }
  
  /**
   * An undirected graph node that stores outgoing edges and incoming edges as
   * an list within the node itself.
   */
  static class LinkedUndirectedGraphNode<N, E> implements UndiGraphNode<N, E>,
      GraphvizNode {

    private List<UndiGraphEdge<N, E>> neighborList =
      Lists.newArrayList();
    private final N value;
    private Annotation annotation;
    private int id;
    private static int totalNodes = 0;

    public LinkedUndirectedGraphNode(N nodeValue) {
      this.value = nodeValue;
      this.id = totalNodes++;
    }

    @Override
    public List<UndiGraphEdge<N, E>> getNeighborEdges() {
      return neighborList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getAnnotation() {
      return (A) annotation;
    }

    @Override
    public N getValue() {
      return value;
    }

    @Override
    public void setAnnotation(Annotation data) {
      annotation = data;
    }

    @Override
    public String getColor() {
      return "white";
    }

    @Override
    public String getId() {
      return "LDN" + id;
    }

    @Override
    public String getLabel() {
      return value != null ? value.toString() : "null";
    }
    
    public Iterator<GraphNode<N, E>> neighborIterator() {
      return new NeighborIterator();
    }

    private class NeighborIterator implements Iterator<GraphNode<N, E>> {
      
      private final Iterator<UndiGraphEdge<N, E>> edgeIterator =
          neighborList.iterator();

      @Override
      public boolean hasNext() {
        return edgeIterator.hasNext();
      }

      @Override
      public GraphNode<N, E> next() {
        UndiGraphEdge<N, E> edge = edgeIterator.next();
        if (edge.getNodeA() == LinkedUndirectedGraphNode.this) {
          return edge.getNodeB();
        } else {
          return edge.getNodeA();
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Remove not supported.");
      }
    }
  }
  
  /**
   * An undirected graph edge that stores two nodes at each edge.
   */
  static class LinkedUndirectedGraphEdge<N, E> implements UndiGraphEdge<N, E>,
      GraphvizEdge {

    private UndiGraphNode<N, E> nodeA;
    private UndiGraphNode<N, E> nodeB;
    protected final E value;
    protected Annotation annotation;

    public LinkedUndirectedGraphEdge(UndiGraphNode<N, E> nodeA, E edgeValue,
        UndiGraphNode<N, E> nodeB) {
      this.value = edgeValue;
      this.nodeA = nodeA;
      this.nodeB = nodeB;
    }

    @Override
    public E getValue() {
      return value;
    }

    @Override
    public GraphNode<N, E> getNodeA() {
      return nodeA;
    }

    @Override
    public GraphNode<N, E> getNodeB() {
      return nodeB;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getAnnotation() {
      return (A) annotation;
    }

    @Override
    public void setAnnotation(Annotation data) {
      annotation = data;
    }

    @Override
    public String getColor() {
      return "black";
    }

    @Override
    public String getLabel() {
      return value != null ? value.toString() : "null";
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getNode1Id() {
      return ((LinkedUndirectedGraphNode<N, E>) nodeA).getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getNode2Id() {
      return ((LinkedUndirectedGraphNode<N, E>) nodeB).getId();
    }

    @Override
    public String toString() {
      return nodeA.toString() + " -- " + nodeB.toString();
    }
  }
}

