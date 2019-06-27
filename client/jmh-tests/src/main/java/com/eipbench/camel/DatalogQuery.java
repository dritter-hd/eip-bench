package com.eipbench.camel;


public class DatalogQuery {

	private int arity;
	private String predicateName;
	private String[] params;

	public DatalogQuery(String predicateName, int arity) {
		this.predicateName = predicateName;
		this.arity = arity;
		this.params = new String[arity];
	}

	public DatalogQuery(String predicateName, int arity, String[] params) throws ParameterCountDoesNotMatchPredicateArityException {
		this.arity = arity;
		this.predicateName = predicateName;
		this.params = params;

		if (params.length != arity) {
			throw new ParameterCountDoesNotMatchPredicateArityException(
					"the amount of parameters specified for the query does not match the amount of parameters specified via the arity declaration");
		}
	}

	public int getArity() {
		return arity;
	}

	public String getPredicateName() {
		return predicateName;
	}

	public String[] getParams() {
		return params;
	}
}
