package net.sf.taverna.t2.commandline.exceptions;

public class OpenDataflowException extends Exception {

	
	private static final long serialVersionUID = 4778578311101082197L;

	public OpenDataflowException() {
		
	}

	public OpenDataflowException(String message) {
		super(message);	
	}

	public OpenDataflowException(Throwable cause) {
		super(cause);
	}

	public OpenDataflowException(String message, Throwable cause) {
		super(message, cause);
	}

}
