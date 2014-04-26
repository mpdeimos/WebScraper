package com.mpdeimos.webscraper;

/**
 * Exception that is thrown upon scraping errors.
 * 
 * @author mpdeimos
 */
public class ScraperException extends Exception
{
	/** Serialization ID. */
	private static final long serialVersionUID = 1L;

	/** Constructor. */
	public ScraperException(String message)
	{
		super(message);
	}

	/** Constructor. */
	public ScraperException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
