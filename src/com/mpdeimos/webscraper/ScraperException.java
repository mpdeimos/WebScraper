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

	/**
	 * Throws a {@link ScraperException} if the given value is <code>null</code>
	 * .
	 * 
	 * @param message
	 *            The name or message for identifying the value.
	 * @param value
	 *            The value to check.
	 * @throws ScraperException
	 *             if value is null.
	 */
	public static void throwIfNull(Object value, String message)
			throws ScraperException
	{
		if (value == null)
		{
			throw new ScraperException("Item is null: " + message);
		}
	}
}
