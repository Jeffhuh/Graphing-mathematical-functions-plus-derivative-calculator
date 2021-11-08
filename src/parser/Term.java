package parser;

public class Term {
	private double value;
	private String operator;
	private boolean constant;
	
	public Term() {
		
	}
	
	public Term(String operator) {
		setOperator(operator);
	}
	
	public Term(String operator, boolean constant, double value) {
		setOperator(operator);
		setConstant(constant);
		setValue(value);
	}
	
	public boolean getConstant() {
		return constant;
	}
	
	public void setConstant(boolean constant) {
		this.constant = constant;
	}
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	
	public boolean equal(Term term) {
		if (term == null) return false;
		if (this == term) return true;
		String s1 = getOperator();
		String s2 = term.getOperator();
		if (term.getConstant() != getConstant()) {
			return false;
		} else if (getValue() != term.getValue()) {
			return false;
		} else if (s1 == null || s2 == null) {
			if (s1 == null && s2 == null) {
				return true;
			} else {
				return false;
			}
		} else if (!s1.equals(s2)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Term [value=" + value + ", operator=" + operator + ", constant=" + constant + "]";
	}
}
