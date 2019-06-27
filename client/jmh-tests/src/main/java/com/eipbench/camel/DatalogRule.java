package com.eipbench.camel;

public class DatalogRule {

	private String predicateName;
	private String arguments;
	private int arity;
	private String ruleBody;

	public DatalogRule(String predicateName, String arguments, String ruleBody) {
		super();
		this.predicateName = predicateName;
		this.arguments = arguments;
		String[] argumentArray = arguments.split(",");
		this.arity = argumentArray.length;
		this.ruleBody = ruleBody;
	}

	public DatalogRule() {
	}

	public String getPredicateName() {
		return predicateName;
	}

	public String getArguments() {
		return arguments;
	}

	public int getArity() {
		return arity;
	}

	public String getRuleBody() {
		return ruleBody;
	}

	@Override
	public String toString() {
		return predicateName + "(" + arguments + "):-" + ruleBody + ".";
	}

	public static DatalogRule fromString(String ruleString) {
		// FIXME: duplicate Code, CamelDatalogPreprocessorGraphBuilder
		int endOfRuleName = ruleString.indexOf("(");
		String predicateName = ruleString.substring(0, endOfRuleName);
		String ruleBody = ruleString.replaceFirst(".*\\(.*\\):-", "");
		String arguments = ruleString.replaceFirst(":-.*", "");
		arguments = arguments.replaceFirst(".*\\(", "");
		arguments = arguments.replaceFirst("\\).*", "");
		if (predicateName.isEmpty() || ruleBody.isEmpty()
				|| arguments.isEmpty()) {
			throw new IllegalArgumentException(
					"Invalid Rule either predicateName, ruleBody or arguments empty");
		}
		//remove trailing .
		ruleBody = ruleBody.substring(0, ruleBody.length()-1);
		return new DatalogRule(predicateName, arguments, ruleBody);
	}
}
