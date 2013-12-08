package com.mpdeimos.webscraper.util;

/**
 * Utility methods for asserting various program states.
 * 
 * @author mpdeimos
 */
public class Assert
{
	/**
	 * Indicates that it is an unexpected program state that the given exception
	 * is caught. I.e. primary checks should prevent that the exception is
	 * actually thrown.
	 * 
	 * @throws AssertionError
	 *             If this method is called.
	 */
	public static void notCaught(Throwable cause, String justification)
			throws AssertionError
	{
		throw new AssertionError(
				"Unexpected program state, because " + justification, cause); //$NON-NLS-1$
	}
}
