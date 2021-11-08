package parser;

public class Operation {
	public static double add(double a, double b) {
		return a + b;
	}
	
	public static double minus(double a, double b) {
		return a - b;
	}
	
	public static double mult(double a, double b) {
		return a * b;
	}
	
	public static double div(double a, double b) {
		return a / b;
	}
	
	public static double pow(double a, double b) {
		return Math.pow(a, b);
	}
	
	public static double sin(double a, double b) {
		return Math.sin(a);
	}
	
	public static double cos(double a, double b) {
		return Math.cos(a);
	}
	
	public static double tan(double a, double b) {
		return Math.tan(a);
	}
	
	public static double arcsin(double a, double b) {
		return Math.asin(a);
	}
	
	public static double arccos(double a, double b) {
		return Math.acos(a);
	}
	
	public static double arctan(double a, double b) {
		return Math.atan(a);
	}
	
	public static double root(double a, double b) {
		return Math.pow(b, 1d/a);
	}
	
	public static double ln(double a, double b) {
		return Math.log(a);
	}
	
	public static double log(double a, double b) {
		return Math.log(b)/Math.log(a);
	}
}
