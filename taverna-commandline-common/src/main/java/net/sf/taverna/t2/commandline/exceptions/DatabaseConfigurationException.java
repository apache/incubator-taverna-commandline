package net.sf.taverna.t2.commandline.exceptions;

public class DatabaseConfigurationException extends Exception {
	
	private static final long serialVersionUID = -4128248547532355697L;

	public DatabaseConfigurationException() {

	}

	public DatabaseConfigurationException(String message) {
		super(message);
	}

	public DatabaseConfigurationException(Throwable cause) {
		super(cause);
	}

	public DatabaseConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
