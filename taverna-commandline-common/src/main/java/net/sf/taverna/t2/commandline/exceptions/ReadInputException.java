package net.sf.taverna.t2.commandline.exceptions;

public class ReadInputException extends Exception {
		
	private static final long serialVersionUID = -3494432791254643055L;

	public ReadInputException(String msg) {
		super(msg); 
	}
	
	public ReadInputException(String msg, Throwable e) {
		super(msg,e); 
	}
	
}
