package parser;

import java.util.ArrayList;
import java.util.HashMap;

public class Simplification {
	
	private ArrayList<Boolean> inverse;
	private ArrayList<Node> terms; 
	private Node simplified;
	
	public Simplification() {
		inverse = new ArrayList<>();
		terms = new ArrayList<>();
	}
	
	public Simplification(Node node) {
		inverse = new ArrayList<>();
		terms = new ArrayList<>();
		simplified = simplify(node);
		
	}
	//ln(2)*ln(5) -> problem
	public Node simplify(Node node) {
		Node temp = null;
		int i = 0;
		BinaryTree.setParents(node);
		//System.out.println(BinaryTree.binaryTreeToString(node));
		do {
			//System.out.println("START");
			temp = node.copy(); // doesnt copy parents properly
			BinaryTree.setParents(temp);
			node = simplifyPow(node);
			//System.out.println(BinaryTree.binaryTreeToString(node));
			node = simplifyBijectiveFunctions(node);
			//System.out.println(BinaryTree.binaryTreeToString(node));
			node = simplifyAddMin(node);		//error prone 
			//System.out.println(BinaryTree.binaryTreeToString(node));
			node = simplifyMultDiv(node);
			//BinaryTree.setParents(node);
			//System.out.println(BinaryTree.binaryTreeToString(node));
			node  = simplifyAddZeros(node);
			//System.out.println(BinaryTree.binaryTreeToString(node));
			node = simplifyMultOne(node);
			//System.out.println("END");
			BinaryTree.setParents(node);
			i++;
		} while (!BinaryTree.equal(node, temp) && i < 10); //!BinaryTree.equal(node, temp)
		do {
			temp = node.copy();
			node = simplifyAddZeros(node);
			node = simplifyMultOne(node);
			BinaryTree.setParents(node);
		} while (!BinaryTree.equal(node, temp));
		return node;
	}
	
	public Node simplifyAddZeros(Node node) {
		if (node == null) {
			return null;
		}
		Term t = node.getData();
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		if ("+".equals(t.getOperator())) {
			Term leftTerm = nodeLeft.getData();
			if (leftTerm.getConstant() && leftTerm.getValue() == 0d) {
				return nodeRight;
			}
			Term rightTerm = nodeRight.getData();
			if (rightTerm.getConstant() && rightTerm.getValue() == 0d) {
				return nodeLeft;
			}
		}
		if ("-".equals(t.getOperator())) {
			Term rightTerm = nodeRight.getData();
			if (rightTerm.getConstant() && rightTerm.getValue() == 0d) {
				return nodeLeft;
			}
		}
		Node n1 = simplifyAddZeros(node.left == null ? null : node.left);
		Node n2 = simplifyAddZeros(node.right == null ? null : node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		return node;
	}
	
	public Node simplifyMultOne(Node node) { /////////todo
		if (node == null) {
			return null;
		}
		Term t = node.getData();
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		if ("*".equals(t.getOperator())) {
			Term leftTerm = nodeLeft.getData();
			if (leftTerm.getConstant() && leftTerm.getValue() == 1d) {
				return nodeRight;
			}
			Term rightTerm = nodeRight.getData();
			if (rightTerm.getConstant() && rightTerm.getValue() == 1d) {
				return nodeLeft;
			}
		}
		if ("/".equals(t.getOperator())) {
			Term rightTerm = nodeRight.getData();
			if (rightTerm.getConstant() && rightTerm.getValue() == 1d) {
				return nodeLeft;
			}
			if (BinaryTree.equal(nodeLeft, nodeRight)) {
				return new Node(new Term(null, true, 1d));
			}
		}
		Node n1 = simplifyMultOne(node.left == null ? null : node.left);
		Node n2 = simplifyMultOne(node.right == null ? null : node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		return node;
	}
	
	public Node simplifyPow(Node node) {
		if (node == null) {
			return null;
		}
		//System.out.println(true);
		
		Term t = node.getData();
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		if ("^".equals(t.getOperator())) {
			Term rightTerm = nodeRight.getData();
			if (rightTerm.getConstant()) {
				if (rightTerm.getValue() == 0) {
					return new Node(new Term(null, true, 1d));
				} else if (rightTerm.getValue() == 1) {
					return nodeLeft;
				}
			}
			if ("^".equals(nodeLeft.getData().getOperator())) {
				Node nodeLeftR = nodeLeft.right;
				Node nodeMult = new Node(new Term("*", false, 0d), nodeLeftR, nodeRight);
				nodeLeft.setRight(nodeMult);
				return simplifyPow(nodeLeft);
			}
		}
		Node n1 = simplifyPow(node.left == null ? null : node.left);
		Node n2 = simplifyPow(node.right == null ? null : node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		return node;
	}
	
	public Node simplifyAddMin(Node node) {
		if (node == null) {
			return null;
		}
		Term data = node.getData();
		String operator = data.getOperator();
		if ("+".equals(operator) || "-".equals(operator)) {
			//System.out.println(BinaryTree.binaryTreeToString(node));
			Node temp = createAddSimply(node);
			if (!BinaryTree.equal(temp, node)) {
				node = temp;
			}
		}
		Node n1 = simplifyAddMin(node.left);
		Node n2 = simplifyAddMin(node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		return node;
	}
	
	public Node createAddSimply(Node node) {
		inverse.clear();
		terms.clear();
		createArrayListAdd1(node, 0);
		/*for (int i = 0; i < inverse.size(); i++) {
			System.out.println(terms.get(i).getData()+" "+inverse.get(i));
		}
		System.out.println("END OF ARRAYLIST\n");*/
		double sum = 0d;
		int index = 0;
		boolean first = true;
		HashMap<Node, ArrayList<Node>> sameNode = new HashMap<>();	
		ArrayList<Node> a = new ArrayList<>();
		ArrayList<Boolean> b = new ArrayList<>();
		
		//////////////////////////////////////
		/*ArrayList<Node> multAndDiv = new ArrayList<>();
		for (int i = 0; i < terms.size(); i++) {
			Node n = terms.get(i);
			Term t = n.getData();
			if (t.getConstant()) {
				if (t.getOperator() == null) {
					sum += t.getValue() * (inverse.get(i) == true ? -1d : 1);
				} else {
					double d = t.getValue();
					if (d % 1 == 0) {
						sum += t.getValue() * (inverse.get(i) == true ? -1d : 1);
					} else {
						if ()
					}
				}
			}
			if ("*".equals(t.getOperator()) || "/".equals(t.getOperator())) {
				multAndDiv.add(n);
			}
		}*/
		//////////////////////////////////////
		for (int i = 0; i < terms.size(); i++) {
			Node n = terms.get(i);
			Term t = n.getData();
			if (t.getConstant()) {
				if (t.getOperator() == null) {
					/*if (first) {
						System.out.println("AM I ENTERING");
						index = i;
						first = false;
					}*/
					sum += t.getValue() * (inverse.get(i) == true ? -1d : 1);
				} else {
					double d = t.getValue();
					if (d % 1 == 0) {
						sum += t.getValue() * (inverse.get(i) == true ? -1d : 1);
					} else {
						a.add(n);
						b.add(inverse.get(i));
					}
				}
			} else {
				a.add(n);
				b.add(inverse.get(i));
			}
		}
		//if (sum != 0) {

		b.add(false);
		a.add(new Node(new Term(null, true, sum)));
		//b.add(index, false);
		//}
		/*System.out.println("START");
		for (int i = 0; i < a.size(); i++) {
			System.out.println(a.get(i).getData());
		}
		System.out.println();
		for (int i = 0; i < terms.size(); i++) {
			System.out.println(terms.get(i).getData());
		}
		System.out.println("END");
		if (a.size() == terms.size()) {
			for (int i = 0; i < a.size(); i++) {
				if (!a.get(i).getData().equal(terms.get(i).getData())) {
					System.out.println("sdf");
					return node;
				}
			}
		}*/
		if (a.size() == 1) {
			return a.get(0);
		} else {
			ArrayList<Node> nodes = new ArrayList<>();
			for (int i = 0; i < a.size() - 1; i++) {
				boolean tempB = b.get(i);
				Node n = new Node(new Term(tempB ? "-" : "+", false, 0d));
				nodes.add(n);
			}
			for (int i = 0; i < nodes.size() - 1; i++) {
				Node n = nodes.get(i);
				n.setLeft(nodes.get(i+1));
				n.setRight(a.get(i));
			}
			Node n = nodes.get(nodes.size() - 1);
			n.setLeft(a.get(a.size() - 1));
			n.setRight(a.get(a.size() - 2));
			return nodes.get(0);
		}
	}
	
	public void createArrayListAdd1(Node node, int minus) {
		if (node == null) {
			return;
		}
		Term t = node.getData();
		String operator = t.getOperator();
		boolean b = (minus & 1) == 0 ? false : true;
		if (!"-".equals(operator) && !"+".equals(operator)) {
			//System.out.println(operator);
			terms.add(node);
			inverse.add(b);
			return;
		}
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		if ("+".equals(operator)) {
			createArrayListAdd1(nodeLeft, minus);
			createArrayListAdd1(nodeRight, minus);
		} else {
			createArrayListAdd1(nodeLeft, minus);
			createArrayListAdd1(nodeRight, minus+1);
		}
	}
	
	public Node simplifyMultDiv(Node node) {
		if (node == null) {
			return null;
		}
		Term data = node.getData();
		String operator = data.getOperator();
		if ("*".equals(operator) || "/".equals(operator)) {
			//System.out.println("HEY");
			node = createMultSimply(node);
		}
		Node n1 = simplifyMultDiv(node.left);
		Node n2 = simplifyMultDiv(node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		return node;
	}
	
	public Node createMultSimply(Node node) {
		inverse.clear();
		terms.clear();
		createArrayListMult1(node, 0);
		/*for (int i = 0; i < inverse.size(); i++) {
			System.out.println(terms.get(i).getData().getValue()+" "+inverse.get(i)+" "+terms.get(i).getData().getOperator());
		}*/
		double mult = 1d;
		ArrayList<Node> a = new ArrayList<>();
		ArrayList<Boolean> b = new ArrayList<>();
		for (int i = 0; i < terms.size(); i++) {
			Node n = terms.get(i);
			Term t = n.getData();
			String operator = t.getOperator();
			if (t.getConstant()) {
				if (operator == null) {
					mult *= (inverse.get(i) == true ? 1d/t.getValue() : t.getValue());
				} else {
					double d = t.getValue();
					System.out.println(d);
					if (d % 1 == 0 || "*".equals(operator) || "/".equals(operator)) {
						mult *= (inverse.get(i) == true ? 1d/t.getValue() : t.getValue());
					} else {
						a.add(n);
						b.add(inverse.get(i));
					}
				}
			} else {
				a.add(n);
				b.add(inverse.get(i));
			}
		}
		if (a.equals(terms)) {
			return node;
		}
		//System.out.println(mult);
		if (mult == 0) {
			return new Node(new Term(null, true, 0d));
		} else {
			a.add(new Node(new Term(null, true, mult)));
			b.add(false);
		}
		/*for (int i = 0; i < a.size(); i++) {
			System.out.println(a.get(i));
		}*/
		if (a.size() == 1) {
			return a.get(0);
		} else {
			ArrayList<Node> nodes = new ArrayList<>();
			for (int i = 0; i < a.size() - 1; i++) {
				boolean tempB = b.get(i);
				Node n = new Node(new Term(tempB ? "/" : "*", false, 0d));
				nodes.add(n);
			}
			for (int i = 0; i < nodes.size() - 1; i++) {
				Node n = nodes.get(i);
				n.setLeft(nodes.get(i+1));
				n.setRight(a.get(i));
			}
			Node n = nodes.get(nodes.size() - 1);
			n.setLeft(a.get(a.size() - 1));
			n.setRight(a.get(a.size() - 2));
			return nodes.get(0);
		}
	}
	
	public void createArrayListMult1(Node node, int div) {
		if (node == null) {
			return;
		}
		Term t = node.getData();
		String operator = t.getOperator();
		boolean b = (div & 1) == 0 ? false : true;
		if (!"*".equals(operator) && !"/".equals(operator)) {
			//System.out.println(operator);
			terms.add(node);
			inverse.add(b);
			return;
		}
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		if ("*".equals(operator)) {
			createArrayListMult1(nodeLeft, div);
			createArrayListMult1(nodeRight, div);
		} else {
			createArrayListMult1(nodeLeft, div);
			createArrayListMult1(nodeRight, div+1);
		}
	}
	
	// 0 - x ==> -1 * x
	public Node replaceMinWithMult(Node node) {
		if (node == null) {
			return null;
		}
		Node n1 = replaceMinWithMult(node.left);
		Node n2 = replaceMinWithMult(node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		Term data = node.getData();
		if ("-".equals(data.getOperator())) {
			Node nodeLeft = node.left;
			Node nodeRight = node.right;
			Term t = nodeLeft.getData();
			if (t.getConstant() && t.getValue() == 0d) {
				node = new Node(new Term("*"), new Node(new Term(null, true, -1)), nodeRight);
			}
		}
		return node;
	}
	
	public Node simplifyBijectiveFunctions(Node node) {
		if (node == null) {
			return null;
		}
		Node n1 = simplifyBijectiveFunctions(node.left == null ? null : node.left);
		Node n2 = simplifyBijectiveFunctions(node.right == null ? null : node.right);
		if (n1 != null) {
			node.left = n1;
		}
		if (n2 != null) {
			node.right = n2;
		}
		Term data = node.getData();
		String operator = data.getOperator();
		if (data.getConstant() || operator == null) {
			return node;
		}
		Node nodeLeft;
		Node nodeRight;
		String nodeLeftOperator;
		switch (operator) {
		case "ln":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if ("^".equals(nodeLeftOperator)) {
				Node nodeLeft2 = nodeLeft.left;
				Term data2 = nodeLeft2.getData();
				if (data2.getValue() == Math.E) {
					node = nodeLeft.right;
				}
			}
			break;
			
		case "/":
			nodeLeft = node.left;
			nodeRight = node.right;
			if ("ln".equals(nodeLeft.getData().getOperator()) && "ln".equals(nodeRight.getData().getOperator())) {
				Node nodeLeftLn = nodeLeft.left;
				if ("^".equals(nodeLeftLn.getData().getOperator())) {
					Node nodeRightLn = nodeRight.left;
					if (BinaryTree.equal(nodeLeftLn.left, nodeRightLn)) {
						node = nodeLeftLn.right;
					}
				}
			}
			break;
			
		case "sin":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if (nodeLeftOperator != null && nodeLeftOperator.equals("arcsin")) {
				node = nodeLeft.left;
			}
			break;
			
		case "cos":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if (nodeLeftOperator != null && nodeLeftOperator.equals("arccos")) {
				node = nodeLeft.left;
			}
			break;
			
		case "tan":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if (nodeLeftOperator != null && nodeLeftOperator.equals("arctan")) {
				node = nodeLeft.left;
			}
			break;
			
		case "arcsin":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if (nodeLeftOperator != null && nodeLeftOperator.equals("sin")) {
				node = nodeLeft.left;
			}
			break;
			
		case "arccos":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if (nodeLeftOperator != null && nodeLeftOperator.equals("cos")) {
				node = nodeLeft.left;
			}
			break;
			
		case "arctan":
			nodeLeft = node.left;
			nodeLeftOperator = nodeLeft.data.getOperator();
			if (nodeLeftOperator != null && nodeLeftOperator.equals("tan")) {
				node = nodeLeft.left;
			}
			break;
			// arctan(tan(log_(x+1)((x+1)^(cos(x)))))
		case "^":
			nodeLeft = node.left;
			nodeRight = node.right;
			Term nodeLeftData = nodeLeft.getData();
			Term nodeRightData = nodeRight.getData();
			if (nodeLeftData.getValue() == Math.E) {
				if ("ln".equals(nodeRightData.getOperator())) {
					node = nodeRight.left;
				}
			} else if ("/".equals(nodeRightData.getOperator())) {
				Node nodeRightL = nodeRight.left;
				Node nodeRightR = nodeRight.right;
				if ("ln".equals(nodeRightL.data.getOperator()) && "ln".equals(nodeRightR.data.getOperator())) {
					if (BinaryTree.equal(nodeLeft, nodeRightR.left)) {
						node = nodeRightL.left;
					}
				}
			}
			break;		
		// + and - case should be implemented in a separate method
		}
		return node;
	}
	
	public Node replaceNodeWithChild(Node node, boolean dir) {
		Node xPos;
		if (dir) {
			xPos = node.left;
		} else {
			xPos = node.right;
		}
		return xPos;
	}
}
