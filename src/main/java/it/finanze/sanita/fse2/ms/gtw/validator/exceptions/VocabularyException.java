package it.finanze.sanita.fse2.ms.gtw.validator.exceptions;

public class VocabularyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4420700371354323215L;

	/**
	 * Message constructor.
	 * 
	 * @param msg Message to be shown.
	 */
	public VocabularyException(final String msg) {
		super(msg);
	}
	
	/**
	 * Complete constructor.
	 * 
	 * @param msg	Message to be shown.
	 * @param e		Exception to be shown.
	 */
	public VocabularyException(final String msg, final Exception e) {
		super(msg, e);
	}
	
	/**
	 * Exception constructor.
	 * 
	 * @param e	Exception to be shown.
	 */
	public VocabularyException(final Exception e) {
		super(e);
	}
	
}
