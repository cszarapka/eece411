package com.group11.eece411.A4;

/*
 * Static data structure for successor lists
 */
public class SuccessorList {
	private static Node[] list = new Node[3];
	
	/*
	 * Successor position is the distance between the node and the successor (i.e. first successor is 1, second is 2...)
	 */
	public void addSuccessor(Node node, int successorPosition){
		list[successorPosition] = node;
	}
	
	public Node getSuccessor(int successorPosition){
		return list[successorPosition];
	}
}
