package parser;

public class Derivative {
	
	public Node derivative;
	
	public Derivative() {
		
	}
	
	public Derivative(Node root) {
		this.derivative = getDerivative(root.copy());
	}
	
	public Node getDerivative(Node node) {
		Term data = node.getData();
		if (data.getConstant()) {
			Term t = new Term(null, true, 0);
			return new Node(t);
		}
		String operator = data.getOperator();
		if (operator == null) {
			Term t = new Term(null, true, 1);
			return new Node(t);
		}
		boolean left = BinaryTree.checkForX(node.left);
		boolean right = BinaryTree.checkForX(node.right);
		switch (operator) {
		case "+":
			node.left = getDerivative(node.left);
			node.right = getDerivative(node.right);
			break;
			
		case "*":
			 if (left && right) {
				 node = productRule(node);
				 break;
			 }
			 if (left) {
				 node.left = getDerivative(node.left);
				 break;
			 }
			 if (right) {
				 node.right = getDerivative(node.right);
				 break;
			 }
			 node.left = getDerivative(node.left);
			 node.right = getDerivative(node.right);
			break;
			
		case "/":
			if (left && right) {
				 node = quotientRule(node);
				 break;
			}
			if (left) {
				node.left = getDerivative(node.left);
				break;
			}
			if (right) {
				node = specialQuotientRule(node);
				break;
			}
			node.left = getDerivative(node.left);
			node.right = getDerivative(node.right);
			break;
			
		case "-":
			node.left = getDerivative(node.left);
			node.right = getDerivative(node.right);
			break;
			
		case "^":
			if (left && right) {
				 node = generalizedPowerRule(node);
				 break;
			}
			if (left) {
				node = powerRule(node);
				break;
			}
			if (right) {
				node = chainRuleForExponential(node);
				break;
			}
			node.left = getDerivative(node.left);
			node.right = getDerivative(node.right);
			break;
			
		default: // default
			if (!left) {
				node = new Node(new Term(null, true, 0d));
				break;
			}
			Term t = node.left.data;
			if (t.getOperator() == null) {
				
				switch (operator) {
				case "ln":
					node = ddxLn();
					break;
				
				case "sin":
					node = ddxSin();
					break;
					
				case "cos":
					node = ddxCos();
					break;
					
				case "tan": 
					node = ddxTan();
					break;
					
				case "arcsin": 
					node = ddxArcsin();
					break;
				
				case "arccos": 
					node = ddxArccos();
					break;
					
				case "arctan": 
					node = ddxArctan();
					break;
				}
			} else {
				node = generalizedChainRule(node);
			}
		}
		return node;
	}
	
	// u(v(x)) ==> u'(v(x)) * v'(x)
	public Node generalizedChainRule(Node node) {
		Node rightDeriv = getDerivative(node.left.copy());
		Term t = node.getData();
		Node outerFunc = new Node(new Term(t.getOperator()), new Node(new Term(null, false, 0d)), null);
		Node chain = getDerivative(outerFunc);
		BinaryTree.setParents(chain);
		Node parentsOfX = BinaryTree.searchForX(chain);
		if (parentsOfX == null) {
			return new Node(new Term(null, true, 0d));
		} else {
			parentsOfX = parentsOfX.parent;
		}
		Term t1 = parentsOfX.left.data;
		if (t1.getOperator() == null && t1.getConstant()) {
			parentsOfX.right = node.left; // not reachable
		} else {
			parentsOfX.left = node.left;
		}
		Node mult = new Node(new Term("*"), chain, rightDeriv);
		return mult;
	}
	
	// a / u(x) ==> a * [-u'(x) / u(x)^2]
	public Node specialQuotientRule(Node node) {
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		
		Node exp = new Node(new Term("^"), nodeRight, new Node(new Term(null, true, 2d)));
		Node deriv = getDerivative(node.right.copy());
		Node min = new Node(new Term("-"), new Node(new Term(null, true, 0d)), deriv);
		Node div = new Node(new Term("/"), min, exp);
		Node mult1 = new Node(new Term("*"), nodeLeft, div);
		return mult1;
	}
	
	// u(x) / v(x) ==> (u'(x) * v(x) - u(x) * v'(x)) / v(x)^2
	public Node quotientRule(Node node) {
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		
		Node leftDeriv = getDerivative(nodeLeft.copy());
		Node rightDeriv = getDerivative(nodeRight.copy());
		
		Node mult1 = new Node(new Term("*"), leftDeriv, nodeRight.copy());
		Node mult2 = new Node(new Term("*"), nodeLeft, rightDeriv);
		
		Node exp = new Node(new Term("^"), nodeRight.copy(), new Node(new Term(null, true, 2d)));
		Node min = new Node(new Term("-"), mult1, mult2);
		Node div = new Node(new Term("/"), min, exp);
		return div;
	}
	
	// u(x) * v(x) ==> u'(x) * v(x) + u(x) * v'(x)
	public Node productRule(Node node) {
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		
		Node leftDeriv = getDerivative(nodeLeft.copy());
		Node rightDeriv = getDerivative(nodeRight.copy());
		Node mult1 = new Node(new Term("*"), leftDeriv, nodeRight);
		Node mult2 = new Node(new Term("*"), nodeLeft, rightDeriv);
		Node add1 = new Node(new Term("+"), mult1, mult2);
		return add1;
	}
	
	// u(x)^v(x) ==> u(x)^v(x) * [ln(u(x)) * v(x)]'
	public Node generalizedPowerRule(Node node) {
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		
		Node ln = new Node(new Term("ln"));
		ln.left = nodeLeft;
		Node d = new Node(new Term("*"), ln, nodeRight);
		//ln.setParent(d);  			//error-prone
		//nodeRight.setParent(d); 
		Node ddx = getDerivative(d);		
		Node mult1 = new Node(new Term("*"), node.copy(), ddx, node.parent); //check for parent node of copy
		//ddx.setParent(mult1);
		return mult1;
	}
	
	// a^u(x) ==> a^u(x) * ln(a) * u'(x)
	public Node chainRuleForExponential(Node node) {
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		Node temp = node.copy();
		double v = nodeLeft.getData().getValue();
		Term t; 
		Node ln;
		if (v == Math.E) {
			t = new Term(null, true, 1);
			ln = new Node(t);
		} else {
			t = new Term(null, true, v);
			Node a = new Node(t);
			ln = new Node(new Term("ln"));
			ln.left = a;
			a.setParent(ln);
		}
		Node derivative = getDerivative(nodeRight);
		Node mult2 = new Node(new Term("*"), derivative, ln);
		Node mult1 = new Node(new Term("*"), temp, mult2, node.parent);
		//mult2.setParent(mult1);
		return mult1;
	}
	
	//  u(x)^n  ==> n * u'(x) * u(x)^(n-1)	
	public Node powerRule(Node node) {
		Node nodeLeft = node.left;
		Node nodeRight = node.right;
		Term t = new Term(null, true, 1d);
		Node nodeDerivative = getDerivative(nodeLeft.copy());
		Node temp = nodeRight.copy();
		double d = temp.getData().getValue()-1;
		temp.getData().setValue(d);
		if (d < 0) {
			temp.getData().setValue(d+1);
			Node min = new Node(new Term("-", true, d), new Node(new Term(null, true, 0d)), temp);
			temp = min;
		}
		Node exp = new Node(new Term("^"), nodeLeft.copy(), temp);
		Node mult2 = new Node(new Term("*"), nodeDerivative, exp);
		//nodeDerivative.setParent(mult2);
		Node mult1 = new Node(new Term("*"), nodeRight, mult2, node.parent);
		//exp.setParent(mult2);
		//mult2.setParent(mult1);
		return mult1;
	}
	
	// ln(x) ==> 1/x
	public Node ddxLn() {
		Node div = new Node(new Term("/"), new Node(new Term(null, true, 1d)), new Node(new Term(null, false, 0d)));
		return div;
	}
	
	// sin(x) ==> cos(x)
	public Node ddxSin() {
		Node cos = new Node(new Term("cos"), new Node(new Term(null, false, 0d)), null);
		return cos;
	}
	
	// cos(x) ==> -sin(x)
	public Node ddxCos() {
		Node sin = new Node(new Term("sin"), new Node(new Term(null, false, 0d)), null);
		Node min = new Node(new Term("-"), new Node(new Term(null, true, 0d)), sin);
		return min;
	}
	
	// tan(x) ==> 1/cos(x)^2
	public Node ddxTan() {
		Node cos = new Node(new Term("cos"), new Node(new Term(null, false, 0d)), null);
		Node exp = new Node(new Term("^"), cos, new Node(new Term(null, true, 2d)));
		Node div = new Node(new Term("/"), new Node(new Term(null, true, 1d)), exp);
		return div;
	}
	
	// arcsin(x) ==> 1/sqrt(1 - x^2)
	public Node ddxArcsin() {
		Node exp = new Node(new Term("^"), new Node(new Term(null, false, 0d)), new Node(new Term(null, true, 2d)));
		Node min = new Node(new Term("-"), new Node(new Term(null, true, 1d)), exp);
		Node div2 = new Node(new Term("/"), new Node(new Term(null, true, 1d)), new Node(new Term(null, true, 2d)));
		Node sqrt = new Node(new Term("^"), min, div2);
		Node div = new Node(new Term("/"), new Node(new Term(null, true, 1d)), sqrt);
		return div;
	}
	
	// arccos(x) ==> -1/sqrt(1 - x^2)
	public Node ddxArccos() {
		Node exp = new Node(new Term("^"), new Node(new Term(null, false, 0d)), new Node(new Term(null, true, 2d)));
		Node min = new Node(new Term("-"), new Node(new Term(null, true, 1d)), exp);
		Node div2 = new Node(new Term("/"), new Node(new Term(null, true, 1d)), new Node(new Term(null, true, 2d)));
		Node sqrt = new Node(new Term("^"), min, div2);
		Node div = new Node(new Term("/"), new Node(new Term(null, true, 1d)), sqrt);
		Node min2 = new Node(new Term("-"), new Node(new Term(null, true, 0d)), div);
		System.out.println(BinaryTree.binaryTreeToString(min2));
		return min2;
	}
	
	// arctan(x) ==> 1/(x^2 + 1)
	public Node ddxArctan() {
		Node exp = new Node(new Term("^"), new Node(new Term(null, false, 0d)), new Node(new Term(null, true, 2d)));
		Node add = new Node(new Term("+"), exp, new Node(new Term(null, true, 1)));
		Node div = new Node(new Term("/"), new Node(new Term(null, true, 1d)), add);
		return div;
	}
	
}
