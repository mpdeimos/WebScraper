package com.mpdeimos.webscraper.validation;

import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.scraper.ScraperContext;

/**
 * Interface for validating scraped content.
 * 
 * @author mpdeimos
 */
public interface Validator
{
	/**
	 * @throw ScraperValidationException If validation fails.
	 */
	public void validate(ScraperContext context)
			throws ScraperValidationException;

	/** Exception indicating an validation error. */
	public class ScraperValidationException extends ScraperException
	{

		/** The scraper context that aused validation to fail. */
		private final ScraperContext context;

		/** Constructor. */
		public ScraperValidationException(ScraperContext context, String message)
		{
			super(message);
			this.context = context;
		}

		/** @return The context that caused validation to fail. */
		public ScraperContext getContext()
		{
			return this.context;
		}
	}
}
