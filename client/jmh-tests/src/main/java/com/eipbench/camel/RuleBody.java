package com.eipbench.camel;

public class RuleBody {
	String[] parameters;

	public RuleBody(int arity) {
		parameters = new String[arity];
	}

	public void insertAttribute(int position, String value) {
		parameters[position - 1] = value;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		for (String parameter : parameters) {
			result.append(parameter + ",");
		}

		// Remove trailing comma
		String resultStr = "";
		if (result.length() > 1) {
			resultStr = result.substring(0, result.length() -1);
		}
		return resultStr;
	}

	public String[] getParameters() {
        return parameters;
    }
}
