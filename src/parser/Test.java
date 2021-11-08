package parser;

import java.util.HashMap;
import java.util.Scanner;

public class Test {

	public static void main(String[] args) throws LeftRightException {
		Scanner scan = new Scanner(System.in);
		Parser p;
		try {
			p = new Parser(scan.nextLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
		BinaryTree.value = 2d;
		BinaryTree.optimize(BinaryTree.root.left);
		Simplification s = new Simplification();
		Node na = BinaryTree.root.left.copy();
		//System.out.println(BinaryTree.binaryTreeToString(na));
		//System.out.println(BinaryTree.binaryTreeToString(s.simplifyAddZeros(na)));
		Derivative d = new Derivative(na);
		//System.out.println(BinaryTree.binaryTreeToString(d.derivative));
		na = d.derivative;
		BinaryTree.setParents(na);
		//System.out.println(BinaryTree.binaryTreeToString(n));
		na = s.simplify(na);
		BinaryTree.optimize(na);
		System.out.println(BinaryTree.binaryTreeToString(na));
	}

}