package com.andrewpmsmith.movabletype.model;

/**
 * Raised when a submitted word is invalid. The message describes why the
 * word was invalid.
 * @author Don Kirkby
 *
 */
public class InvalidWordException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Initialize the object.
	 * @param message The message describing why the word was invalid.
	 */
	public InvalidWordException(String message) {
		super(message);
	}

}
