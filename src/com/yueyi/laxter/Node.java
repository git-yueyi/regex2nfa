package com.yueyi.laxter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


class Edge{
	private String text;
	private Node target;
	public String getText() {
		return text;
	}
	
	
	public Edge(String text, Node target) {
		super();
		this.text = text;
		this.target = target;
	}


	public void setText(String text) {
		this.text = text;
	}
	public Node getTarget() {
		return target;
	}
	public void setTarget(Node target) {
		this.target = target;
	}
}
public class Node {

	public final static String EPSINO="ep";
	
	private Node head = this;
	private static int cnt = 0;
	private int idx = -1;
	private List<Edge> edges = new LinkedList<Edge>();
	
	
	private Node tail = this; 
	
	public void copy(Node other) {
		for (Edge edge : other.edges) {
			Node newnode = new Node();
			Node othertarget = edge.getTarget();
			newnode.copy(othertarget);
			Edge newedge = new Edge(edge.getText(),newnode );
			edges.add(newedge);
			if(othertarget.equals(other.tail)) {
				this.tail = newnode;
			}
		}
	}

	public Node getHead() {
		return head;
	}


	
	public Node() {
		super();
		idx = cnt++;
	}



	@Override
	public int hashCode() {
		
		return this.idx;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		
		return this.idx == other.idx;
	}



	public void setHead(Node head) {
		this.head = head;
	}


	public List<Edge> getEdges() {
		return edges;
	}


	public Node getTail() {
		return tail;
	}


	
	
	public void addTail(String txt,Node tail) {
		this.tail.edges.add(new Edge(txt, tail));
		this.tail = tail.tail;
	}
	public void headLink(String txt,Node tail) {
		this.edges.add(new Edge(txt, tail));
	}
	public void setTail(Node tail) {
		this.tail = tail;
	}



	public List<String> getString(List<Node> vlist) {
		LinkedList<String> list = new LinkedList<String>();
		
		if(edges.isEmpty() || vlist.contains(this)) {
			list.add(head.hashCode()+"");
			return list;
		}
		vlist.add(this);
		for (Edge edge : edges) {
			List<String> strs = edge.getTarget().getString(vlist);
			for (String string : strs) {
				StringBuilder sBuilder = new StringBuilder();
				sBuilder.append(head.hashCode()+"-"+edge.getText()+"->"+string.trim()+"\n");
				list.add(sBuilder.toString());
			}
		}
		return list;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		List<Node> visited = new LinkedList<Node>();
		List<String> strs = getString(visited) ;
		for (String string : strs) {
			builder.append(string);
		}
		return builder.toString();
	}


	public void concat(Node nodeh) {
		for (Edge edge : nodeh.getEdges()) {
			this.tail.edges.add(edge);
		}
		this.tail = nodeh.getTail();
	}



	
	
	
	
}
