package parser;

import java.util.Deque;
import java.util.HashMap;

public class BinaryTree {
	public static Node root = new Node(null); 
	public static double value;
	private static HashMap<String, Integer> priority;
	
	static { 
		//{"+","-","*","/","^","root_","log_","ln","arcsin","arccos","arctan","sin","cos","tan","("}
		priority = new HashMap<>();
		priority.put("(", 0);
		priority.put("tan", 1);
		priority.put("sin", 1);
		priority.put("cos", 1);
		priority.put("arcsin", 1);
		priority.put("arccos", 1);
		priority.put("arctan", 1);
		priority.put("ln", 1);
		priority.put("log_", 1);
		priority.put("root_", 1);
		priority.put("^", 2);
		priority.put("*", 3);
		priority.put("/", 3);
		priority.put("+", 4);
		priority.put("-", 4);
	}
	
	public static boolean equal(Node node1, Node node2) {
		if (node1 == null && node2 == null) {
			return true;
		} else if ((node1 == null && node2 != null) || (node1 != null && node2 == null)) {
			return false;
		}
		Term node1Data = node1.getData();
		Term node2Data = node2.getData();
		boolean bb = node1Data.equal(node2Data);
		return bb && equal(node1.left, node2.left) && equal(node1.right, node2.right);
	}
	
	public static int countNodes(Node node) {
		if (node == null) {
			return 0;
		}
		return 1 + countNodes(node.left) + countNodes(node.right);
	}
	
	public static String binaryTreeToString(Node node) {
		if (node == null) {
			return "";
		}
		boolean brackets = false;
		String left = binaryTreeToString(node.left);
		
		Term t = node.getData();
		String operator = t.getOperator();
		if (t.getConstant() && operator == null) {
			if (operator == null) {
				Node parentNode = node.parent;
				if (parentNode != null && "-".equals(parentNode.getData().getOperator()) && parentNode.left == node && t.getValue() == 0d) {
					return "";
				}
				double d = t.getValue();
				if (d % 1 == 0) {
					return String.valueOf((int)d);
				} else {
					if (d == Math.PI) {
						return "pi";
					} else if (d == Math.E) {
						return "e";
					}
					return String.valueOf(d);
				}
			}
		} else if (operator == null) {
			return "x";
		} else if (t.getConstant() && (Math.round(t.getValue() * 10000d)/10000d) % 1== 0) {
			return String.valueOf((int) (Math.round(t.getValue() * 1000d)/1000d));
		} else {
			Node nodeLeft;
			Node nodeRight;
			String termRightOperator;
			String termLeftOperator;
			switch (priority.get(operator)) {
			case 1: 
				left = operator + "(" + left + ")";
				break;
			
			case 2: 
				nodeLeft = node.left;
				termLeftOperator = nodeLeft.getData().getOperator();
				if (termLeftOperator != null && priority.get(termLeftOperator) > 1) {
					left = "(" + left + ")";
				}
				nodeRight = node.right;
				termRightOperator = node.right.getData().getOperator();
				if (termRightOperator != null && priority.get(termRightOperator) > 1) {
					brackets = true;
				}
				left = left + "^";
				break;
				
			case 3:
				nodeLeft = node.left;
				nodeRight = node.right;
				termRightOperator = nodeRight.getData().getOperator();
				termLeftOperator = nodeLeft.getData().getOperator();
				if ("*".equals(operator)) {
					if (termLeftOperator != null && priority.get(termLeftOperator) > 3) {
						left = "(" + left + ")";
					}
					if (termRightOperator != null && priority.get(termRightOperator) > 3) {
						brackets = true;
					}
					left = left + "*";
				} else {
					if (termLeftOperator != null && priority.get(termLeftOperator) > 3) {
						left = "(" + left + ")";
					}
					if (termRightOperator != null && priority.get(termRightOperator) > 2) {
						brackets = true;
					}
					left = left + "/";
				}
				break;
				
			case 4: 
				left = left + operator;
				if ("-".equals(operator)) {
					nodeRight = node.right;
					termRightOperator = nodeRight.getData().getOperator();
					if ("+".equals(termRightOperator) || "-".equals(termRightOperator)) {
						brackets = true;
					}
				}
			}
		}
		
		String right = binaryTreeToString(node.right);
		if (brackets) {
			right = "(" + right + ")";
		}
		String exp = left + right;
		return exp;
	}
	
	public static String binaryTreeToString2(Node node) {
		if (node == null) {
			return "";
		}
		String left = binaryTreeToString2(node.left);
		//
		Term t = node.getData();
		String operator = t.getOperator();
		if (t.getConstant() && operator == null) {
			if (operator == null) {
				double d = t.getValue();
				if (d % 1 == 0) {
					return String.valueOf((int)d);
				} else {
					if (d == Math.PI) {
						return "pi";
					} else if (d == Math.E) {
						return "e";
					}
					return String.valueOf(d);
				}
			}
		} else if (operator == null) {
			return "x";
		} else {
			switch (priority.get(operator)) {
			case 1: 
				left = operator + "(" + left + ")";
				break;
				
			default:
				if (!"".equals(left) && left.charAt(0) == '0' && "-".equals(operator)) {
					left = operator;
				} else {
					left = left + operator;
				}
				break;
			}
		}
		// fix x+1^2 ==> (x+1)^2, also (x+1)^2/(x+2)^2, 4 * (x+1) - 2 * (x+2) <=> 4 * x+1 - 2 * x+2
		String right = binaryTreeToString2(node.right);
		String exp = left + right;
		//System.out.println(exp);
		Node parentNode = node.parent;
		if (parentNode != null && parentNode.getData() != null) {
			String parentOperator = parentNode.getData().getOperator();
			if (priority.get(parentOperator) < priority.get(operator)) {
				if ((exp.charAt(0) != '-' || "^".equals(parentOperator)) && !("cos".equals(parentOperator) || "sin".equals(parentOperator) || "tan".equals(parentOperator) || "arcsin".equals(parentOperator) || "arccos".equals(parentOperator) || "arctan".equals(parentOperator) || "ln".equals(parentOperator)))  { 										//error-prone
					exp = "(" + exp + ")";
				}
			} else if (parentNode.right == node && (("-".equals(parentOperator) && "-".equals(operator)) || ("/".equals(parentOperator) && "/".equals(operator)))) {
				exp = "(" + exp + ")";
			} else if ("^".equals(operator) && "^".equals(parentOperator)) {
				exp = "(" + exp + ")";
			}
		}
		return exp;
	}
	
	public static boolean checkForX(Node node) {
		if (node == null) {
			return false;
		}
		Term t = node.data;
		if (!t.getConstant() && t.getOperator() == null) {
			return true;
		}
		boolean b1 = checkForX(node.left);
		boolean b2 = checkForX(node.right);
		return b1 || b2;
	}
	
	public static Node searchForX(Node node) {
		if (node == null) {
			return null;
		}
		Term t = node.data;
		if (!t.getConstant() && t.getOperator() == null) {
			return node;
		}
		Node n1 = searchForX(node.left);
		Node n2 = searchForX(node.right);
		if (n1 != null) {
			return n1;
		} else if (n2 != null) {
			return n2;
		} else {
			return null;
		}
		
	}
	
	public static void add(Deque<Boolean> path, Term value) throws LeftRightException{
		Node child = new Node(value); // * 
		Node parent = nodeFinder(path, root); // root node
		child.parent = parent;
		if (parent.left == null) {
			parent.left = child;
        } else if (parent.right == null){
        	parent.right = child;
        } else {
        	throw new LeftRightException("Left and right side have an object");
        }
	}
	
	public static void setParents(Node node) {
		if (node == null) {
			return;
		}
		if (node.left != null) {
			node.left.parent = node;
		}
		if (node.right != null) {
			node.right.parent = node;
		}
		
		setParents(node.left);
		setParents(node.right);
	}
	
	public static double evaluate(Node node) {
		if (node == null) {
			return 0.0d;
		}
		Term t = node.data;
		InterfaceOperation calc;
		if (t.getConstant()) {
			return t.getValue();
		} else if (t.getOperator() == null) {
			t.setValue(value);
			return value;
		} else {
			switch (t.getOperator()) {
				case "-": 
					calc = Operation::minus;
					break;
				
				case "+": 
					calc = Operation::add;
					break;
					
				case "*": 
					calc = Operation::mult;
					break;
					
				case "/": 
					calc = Operation::div;
					break;
					
				case "^": 
					calc = Operation::pow;
					break;
					
				case "root_": 
					calc = Operation::root;
					break;
				
				case "sin": 
					calc = Operation::sin;
					break;
					
				case "cos": 
					calc = Operation::cos;
					break;
					
				case "tan": 
					calc = Operation::tan;
					break;
					
				case "ln": 
					calc = Operation::ln;
					break;
					
				case "log_": 
					calc = Operation::log;
					break;
					
				case "arcsin": 
					calc = Operation::arcsin;
					break;
					
				case "arccos": 
					calc = Operation::arccos;
					break;
					
				case "arctan": 
					calc = Operation::arctan;
					break;
				
				default: 
					calc = null;
			}
		}
		return calc.meth(evaluate(node.left), evaluate(node.right));
	}
	
	public static double optimize(Node node) {
		if (node == null) {
			return 0.0d;
		}
		Term t = node.data;
		InterfaceOperation calc;
		if (t.getConstant()) {
			return t.getValue();
		} else if (t.getOperator() == null) {
			t.setValue(value);
			return value;
		} else {
			switch (t.getOperator()) {
				case "-": 
					calc = Operation::minus;
					break;
				
				case "+": 
					calc = Operation::add;
					break;
					
				case "*": 
					calc = Operation::mult;
					break;
					
				case "/": 
					calc = Operation::div;
					break;
					
				case "^": 
					calc = Operation::pow;
					break;
					
				case "root_": 
					calc = Operation::root;
					break;
				
				case "sin": 
					calc = Operation::sin;
					break;
					
				case "cos": 
					calc = Operation::cos;
					break;
					
				case "tan": 
					calc = Operation::tan;
					break;
					
				case "ln": 
					calc = Operation::ln;
					break;
					
				case "log_": 
					calc = Operation::log;
					break;
					
				case "arcsin": 
					calc = Operation::arcsin;
					break;
					
				case "arccos": 
					calc = Operation::arccos;
					break;
					
				case "arctan": 
					calc = Operation::arctan;
					break;
				
				default: 
					calc = null;
			}
		}
		double v1 = optimize(node.left);
		double v2 = optimize(node.right);
		Node leftNode = node.left;
		Node rightNode = node.right;
		if (leftNode != null && leftNode.data.getConstant() && (rightNode == null || rightNode.data.getConstant())) {
			node.data.setConstant(true);
			node.data.setValue(calc.meth(v1, v2));
		}
		return calc.meth(v1, v2);
	}
	
	public static void setValue(double va) {
		value = va;
	}
	
	public static Node nodeFinder(Deque<Boolean> path, Node node) {
		if (path.size() == 1) return node;
		if (path.removeLast() == true) {
			return nodeFinder(path, node.left);
		} else {
			return nodeFinder(path, node.right);
		}
	}
}
