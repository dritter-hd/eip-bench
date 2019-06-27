package com.eipbench.camel;


public class NotYetImplementedException extends RuntimeException {
	private static final long serialVersionUID = 479534811630548632L;
	
	public NotYetImplementedException(String string) {
		super(string);
	}

	public NotYetImplementedException() {
		super();
	}
}
