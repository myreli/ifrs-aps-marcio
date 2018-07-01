package cobaia.persistence;

public class PersistenceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5557098253307069655L;

	public PersistenceException(Throwable cause) {
		super("DAO Exception: an exception occurred in persistence.", cause);
	}

	public PersistenceException(String message) {
		super(message);
	}
	
}
