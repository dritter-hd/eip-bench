package com.eipbench.camel;

import com.github.dritter.hd.dlog.evaluator.IFacts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class DatalogProgram implements Serializable{

	private static final long serialVersionUID = 4310187643164860976L;
	private Collection<IFacts> facts = new ArrayList<IFacts>();
	private Collection<IFacts> metaFacts = new ArrayList<IFacts>();

	private DatalogProgram() {
	}
	
	
	private DatalogProgram(Collection<IFacts> facts2, Collection<IFacts> metaFacts2) {
		super();
		this.facts = facts2;
		this.metaFacts = metaFacts2;
	}

	public static DatalogProgram create(Collection<IFacts> facts, Collection<IFacts> metaFacts) {
		return new DatalogProgram(facts, metaFacts);
	}
	
	public static DatalogProgram create() {
		return new DatalogProgram();
	}

	public void setFacts(final Collection<IFacts> facts) {
		this.facts = facts;
	}

	public void setMetaFacts(final Collection<IFacts> metaFacts) {
		this.metaFacts = metaFacts;
	}

	public Collection<IFacts> getFacts() {
		return facts;
	}

	public Collection<IFacts> getMetaFacts() {
		return metaFacts;
	}
	
	@Override
	public String toString() {
		return "meta:\r\n"+metaFacts.toString()+"\r\n"+"facts:\r\n"+facts.toString();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof DatalogProgram){
			DatalogProgram datalog = (DatalogProgram) arg0;
			return (this.facts.equals(datalog.getFacts())&&(this.metaFacts.equals(datalog.getMetaFacts())));
		}
		return super.equals(arg0);
	}	
}