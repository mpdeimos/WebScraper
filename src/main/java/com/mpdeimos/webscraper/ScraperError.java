package com.mpdeimos.webscraper;

/**
 * Error that is thrown upon severe configuration errors.
 * 
 * @author mpdeimos
 */
public class ScraperError extends Error
{
	/** Serialization ID. */
	private static final long serialVersionUID = 1L;

	/** Constructor. */
	public ScraperError(String message)
	{
		super(message);
	}

	/** Constructor. */
	public ScraperError(String message, Throwable cause)
	{
		super(message, cause);
	}
}
