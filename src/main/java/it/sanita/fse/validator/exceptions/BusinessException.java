/**
 * 
 */
package it.sanita.fse.validator.exceptions;

/**
 * @author AndreaPerquoti
 * 
 * Eccezione di business.
 *
 */
public class BusinessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4420700371354323215L;

	/**
	 * Costruttore.
	 * 
	 * @param msg	messaggio
	 */
	public BusinessException(final String msg) {
		super(msg);
	}
	
	/**
	 * Costruttore.
	 * 
	 * @param msg	messaggio
	 * @param e		eccezione
	 */
	public BusinessException(final String msg, final Exception e) {
		super(msg, e);
	}
	
	/**
	 * Costruttore.
	 * 
	 * @param e	eccezione
	 */
	public BusinessException(final Exception e) {
		super(e);
	}
	
}
