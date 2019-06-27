package com.eipbench.camel;

import java.util.ArrayList;
import java.util.List;

public abstract class JsonDatalog {

	protected String uid = null;
	protected String parentId = null;
	protected boolean generateRandomIds;
	protected boolean appendMissingVariables = true;
	protected String nameOfSuperObject;
	protected List<JsonDatalog> nestedObjects = new ArrayList<JsonDatalog>();
	protected IdGenerator idGenerator;

	public String getUID() {
		return uid;
	};

	public abstract void parseJson(DatalogProgramCreator creator);

	public List<JsonDatalog> getNestedObjects() {
		return nestedObjects;
	}
}
