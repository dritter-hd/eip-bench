package com.eipbench.camel;

public class UnsupportedDataTypeException extends Exception {

	private static final long serialVersionUID = 8970527135661687946L;

	public UnsupportedDataTypeException(String exceptionMessage, Throwable cause) {
		super(exceptionMessage, cause);
	}

	public UnsupportedDataTypeException(String exceptionMessage) {
		super(exceptionMessage);
	}
}
