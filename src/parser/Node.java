package parser;

public class Node {
	public Term data;
	public Node parent; 
	public Node left;
	public Node right;
	
	public Node(Term data) {
		setData(data);
	}
	
	public Node(Term data, Node left, Node right) {
		setData(data);
		setLeft(left);
		setRight(right);
	}
	
	public Node(Term data, Node left, Node right, Node parent) {
		setData(data);
		setLeft(left);
		setRight(right);
		setParent(parent);
	}
	
	public Node copy() {
		Node left = null;
		Node right = null;
		if (this.left != null) {
			left = this.left.copy();
		}
		if (this.right != null) {
			right = this.right.copy();
		}
		return new Node(new Term(this.data.getOperator(),this.data.getConstant(),this.data.getValue()),left,right, this.getParent());
	}

	public Term getData() {
		return data;
	}

	public void setData(Term data) {
		this.data = data;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}
}
