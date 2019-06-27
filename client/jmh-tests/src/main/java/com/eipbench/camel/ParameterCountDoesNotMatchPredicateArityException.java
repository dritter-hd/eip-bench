package com.eipbench.camel;

public class ParameterCountDoesNotMatchPredicateArityException extends
		Exception {

	private static final long serialVersionUID = -1485879107898556393L;

	public ParameterCountDoesNotMatchPredicateArityException(String string) {
		super(string);
	}

	public ParameterCountDoesNotMatchPredicateArityException() {
		super();
	}
}
