package jvn;

import java.io.Serializable;

public class MonObjet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2165446687915476843L;
	private String s;
	
	public MonObjet(String s){
		this.s = s;
	}
	
	public void setString(String s){
		this.s = s;
	}
	
	public String getString(){
		return s;
	}
}
