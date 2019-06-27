package com.eipbench.camel;

import java.io.Serializable;
import java.util.Map;

public class NamedDatalogFact implements Serializable{
	private static final long serialVersionUID = -8468213228404660482L;
	private String name;
	private Map<String, Object> parameters;
	
	
	public NamedDatalogFact(String name, Map<String, Object> parameters) {
		this.name = name;
		this.parameters = parameters;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
