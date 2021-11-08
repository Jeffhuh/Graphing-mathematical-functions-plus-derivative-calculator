package parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Parser {
	private static final String[] OPERATORS = {"+","-","*","/","^","root_","log_","ln","arcsin","arccos","arctan","sin","cos","tan","("};
	private static Deque<Boolean> path; 
	String exp;
	
	public Parser() {
		
	}
	
	public Parser(String s) throws Exception {
		path = new ArrayDeque<>();
		exp = s;
		s = s.replace(" ", "");
		Deque<Character> stack = new ArrayDeque<>();
		boolean b = false;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '(') {
				stack.offerFirst('(');
			} else if (s.charAt(i) == ')') {
				try {
					stack.removeFirst();
				} catch (NoSuchElementException e) {
					b = true;
					break;
				}
				
			}
		}
		if (stack.size() != 0 || b) {
			System.out.println("Close all brackets");
		}
		s = putBracketsAround(s);
		s = insertMultiplicationSign(s);
		s = removeMult(s);
		s = putbracketsAroundMin(s);
		//System.out.println(s);
		createBinaryTree(s, true);		// 4 * (x+1) - 2 * (x+2)
	}
	
	private void createBinaryTree(String subExp, boolean direction) throws Exception {
		HashMap<Integer, String> op = operators(subExp);
		//System.out.println(subExp+ " : " +direction);
		path.offerFirst(direction);
		if (op == null) {
			path.removeFirst();
			//System.out.println(subExp);
			createBinaryTree(subExp.substring(1, subExp.length()-1), direction);
			return;
		}
		int operatorIndex = 0;
		for (Integer e : op.keySet()) {
			operatorIndex = e;
		}
		if (operatorIndex == -1) {
			Term t = new Term(null);
			if (subExp.charAt(0) != 'x' || subExp.length() != 1) {
				t.setConstant(true);
				double value = 0.0d;
				if (subExp.charAt(0) == 'e') {
					t.setValue(2.718281828459045d);
				} else if (subExp.charAt(0) == 'p') {
					t.setValue(3.141592653589793d);
				} else {
					t.setValue(Double.parseDouble(subExp));
				}
			}
			BinaryTree.add(getDuplicatePath(), t);
			path.removeFirst();
			return;
		}
		String operator = op.get(operatorIndex);
		//System.out.println(operator+"  :  "+subExp);
		String left = subExp.substring(0, operatorIndex);
		String right = subExp.substring(operatorIndex+operator.length(), subExp.length());
		//System.out.println(right);
		boolean dualOperator = true;
		switch (operator) {
		case "-": 
			Term t = new Term("-");
			if ("".equals(left)) {
				left = "0";
			}
			BinaryTree.add(getDuplicatePath(), t);
			break;
			
		case "+":
			BinaryTree.add(getDuplicatePath(), new Term("+"));
			break;
			
		case "*":
			BinaryTree.add(getDuplicatePath(), new Term("*"));
			break;
			
		case "/":
			BinaryTree.add(getDuplicatePath(), new Term("/"));
			break;
			
		case "^":
			BinaryTree.add(getDuplicatePath(), new Term("^"));
			break;
			
		case "ln":
			BinaryTree.add(getDuplicatePath(), new Term("ln"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
			
		case "log_":
			BinaryTree.add(getDuplicatePath(), new Term("/"));
			if (right.charAt(0) == '(') {
				Deque<Character> br = new ArrayDeque<>();
				br.offerFirst('(');
				int k = 1;
				while (br.size() != 0) {
					if (right.charAt(k) == '(') {
						br.offerFirst('(');
					} else if (right.charAt(k) == ')') {
						br.removeFirst();
					}
					k++;
				}
				left = right.substring(1, k-1);
				right = right.substring(k+1, right.length()-1);
				String temp = left;
			} else {
				int k = 0;
				while (isNumeric(right.charAt(k))) {
					k++;
				}
				left = right.substring(0,k);
				right = right.substring(k+1, right.length()-1);
			}
			String temp1 = left;
			left = "ln("+right+")";
			right = "ln("+temp1+")";
			break;
			
		case "sin":
			BinaryTree.add(getDuplicatePath(), new Term("sin"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
			
		case "cos":
			BinaryTree.add(getDuplicatePath(), new Term("cos"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
			
		case "tan":
			BinaryTree.add(getDuplicatePath(), new Term("tan"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
		
		case "arccos":
			BinaryTree.add(getDuplicatePath(), new Term("arccos"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
			
		case "arcsin":
			BinaryTree.add(getDuplicatePath(), new Term("arcsin"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
			
		case "arctan":
			BinaryTree.add(getDuplicatePath(), new Term("arctan"));
			left = right.substring(1, right.length()-1);
			dualOperator = false;
			break;
		
		case "root_":
			BinaryTree.add(getDuplicatePath(), new Term("^"));
			if (right.charAt(0) == '(') {
				Deque<Character> br = new ArrayDeque<>();
				br.offerFirst('(');
				int k = 1;
				while (br.size() != 0) {
					if (right.charAt(k) == '(') {
						br.offerFirst('(');
					} else if (right.charAt(k) == ')') {
						br.removeFirst();
					}
					k++;
				}
				left = right.substring(1, k-1);
				right = right.substring(k+1, right.length()-1);
			} else {
				int k = 0;
				while (isNumeric(right.charAt(k))) {
					k++;
				}
				left = right.substring(0,k);
				right = right.substring(k+1, right.length()-1);
			}
			String temp = right;
			right = "1/("+left+")";
			left = temp;
			break;
			
		}
		createBinaryTree(left, true);
		if (dualOperator) {
			createBinaryTree(right, false);
		}
		path.removeFirst();
	}
	
	public HashMap<Integer, String> operators(String subExp) throws Exception {
		StringBuilder str = new StringBuilder(subExp); 
		boolean b = true;
		HashMap<Integer, Integer> map = new HashMap<>();
		HashMap<Integer, String> op = new HashMap<>();
		while (str.indexOf("(") != -1) {
			Deque<Character> br = new ArrayDeque<>();
			int j = str.indexOf("(");
			for (int i = j; i < str.length(); i++) {
				if (str.charAt(i) == '(') {
					br.offerFirst('(');
				} else if (str.charAt(i) == ')') {
					if (br.size() == 1) {
						br.removeFirst();
						if (map.isEmpty()) {
							map.put(j, i+1-j);
						} else {
							int sum = 0; 
							for (Integer value : map.values()) {
							    sum += value;
							}
							map.put(j+sum, i+1-j);
						}
						str.delete(j, i+1);
						break;
					}
					br.removeFirst();
				}
			}
		}
		//System.out.println(str);
		/*for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
		    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}*/
		if (str.length() == 0) {
			return null;
		}
		for (int i = 0; i < OPERATORS.length; i++) {
			for (int j = 0; j < str.length(); j++) {
				if (i == 1 || i == 3) {
					int o = str.length()-1-j;
					if (OPERATORS[i].charAt(0) == str.charAt(o)) {
						/*if (i == 1 && o != 0 && (str.charAt(o-1) == '*' || str.charAt(o-1) == '/')) {						// implement for -x ==> (-x)
							continue;															
						}*/
						List<Integer> sortedList = new ArrayList<>(map.keySet());
						Collections.sort(sortedList);
						for (Integer e : sortedList) {
							if (o < e) {
								break;
							} else {
								o += map.get(e);
							}
						}
						op.put(o, OPERATORS[i]);
						return op;
					}
				} else {
					boolean bb = true;
					for (int k = 0; k < OPERATORS[i].length(); k++) {
						if (j+k >= str.length() || str.charAt(j+k) != OPERATORS[i].charAt(k)) {
							bb = false;
						}
					}
					if (bb) {
						List<Integer> sortedList = new ArrayList<>(map.keySet());
						Collections.sort(sortedList);
						for (Integer e : sortedList) {
							if (j < e) {
								break;
							} else {
								j += map.get(e);
							}
						}
						op.put(j, OPERATORS[i]);
						return op;
					}
				}
			}
		}
		op.put(-1, "");
		return op;
	}
	
	private String putbracketsAroundMin(String exp) {
		StringBuilder str = new StringBuilder(exp);
		Deque<Character> deque = new ArrayDeque<>();
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '-' && (i == 0 || (i != 0 && (str.charAt(i-1) == '*' || str.charAt(i-1) == '/')))) {
				str.insert(i, '(');
				if (isNumeric(str.charAt(i+2))) {
					for (int j = i + 2; j < str.length(); j++) {
						if (!isNumeric(str.charAt(j))) {
							str.insert(j, ')');
							break;
						}
						if (j == str.length() - 1) {
							str.insert(j+1,')');
							break;
						}
					}
				} else {
					boolean first = true;
					for (int j = i + 2; j < str.length(); j++) {
						if (str.charAt(j) == '(') {
							deque.offerFirst('(');
							first = false;
						} else if (str.charAt(j) == ')') {
							deque.removeFirst();
						}
						if (deque.size() == 0 && !first) {
							str.insert(j, ')');
							break;
						}
					}
				}
			}
		}
		return str.toString();
	}
	
	private String removeMult(String exp) throws Exception{
		StringBuilder str = new StringBuilder(exp);
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '_') {
				int j = i+1;
				if (str.charAt(j) == '(') {
					Deque<Character> br = new ArrayDeque<>();
					br.offerFirst('(');
					j++;
					while (br.size() != 0) {
						if (str.charAt(j) == '(') {
							br.offerFirst('(');
						} else if (str.charAt(j) == ')') {
							br.removeFirst();
						}
						j++;//j is the index for *
					}
				} else {
					while (isNumeric(str.charAt(j))) {
						j++;
					}
				}
				str.deleteCharAt(j);
			}
		}
		return str.toString();
	}
	
	private String insertMultiplicationSign(String exp)  {
		StringBuilder str = new StringBuilder(exp);
		try {
			for (int i = 0; i < str.length()-1; i++) {
				if (str.charAt(i) == ')' && insertMultB(str.charAt(i+1))) {
					str.insert(++i,'*');
					continue;
				} else if (str.charAt(i) == 'x' && insertMultB(str.charAt(i+1))) {
					str.insert(++i,'*');
				} else if ((Character.isDigit(str.charAt(i)) || str.charAt(i) == '.')&& insertMultF(str.charAt(i+1))) {
					str.insert(++i,'*');
				} else if (str.charAt(i) == 'e' && insertMultB(str.charAt(i+1))) {
					str.insert(++i,'*');
				} else if (str.charAt(i) == 'p' && str.charAt(i+1) == 'i' && insertMultB(str.charAt(i+2))) {
					i += 2;
					str.insert(i,'*');
				}
			}
		} catch (Exception e) {
			
		}
		return str.toString();
	}
	
	private String putBracketsAround(String exp) throws Exception {
		StringBuilder str = new StringBuilder(exp);
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '^' && (isNumeric(str.charAt(i+1)) || str.charAt(i+1) == '-')) {
				str.insert(++i, '(');
				if (str.charAt(i+1) == '-') {
					i++;
				}
				int j = i+1; 
				int anzahl = 0;
				while (j+anzahl < str.length() && isNumeric(str.charAt(j+anzahl))) {
					anzahl++;
				}
				i = i+1+anzahl;
				str.insert(i, ')');
			}
		}
		return str.toString();
	}
	
	public static Deque<Boolean> getDuplicatePath() {
		Deque<Boolean> clone = new ArrayDeque<>();
		Iterator<Boolean> iter = path.descendingIterator();
		while (iter.hasNext()) {
			if (iter.next() == true) {
				clone.offerFirst(true);
			} else {
				clone.offerFirst(false);
			}
		}
		return clone;
	}
	
	private boolean isNumeric(char c) {
		return c == 'x' || Character.isDigit(c) || c == 'e' || c == '.' || c == 'p' || c == 'i';
	}
	
	private boolean insertMultB(char c) {
		return !(c == '+' || c == '-' || c == '*' || c == '/' || c == ')' || c == '^');
	}
	
	private boolean insertMultF(char c) {
		return !(c == '+' || c == '-' || c == '*' || c == '/' || c == ')' || c == '^' || Character.isDigit(c) || c == '.');
	}
}
