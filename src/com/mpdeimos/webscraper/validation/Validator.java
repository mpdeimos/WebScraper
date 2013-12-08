package com.mpdeimos.webscraper.validation;

import com.mpdeimos.webscraper.ScraperException;

import java.lang.reflect.Field;

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
	public void validateScrapedData(String textData, Class<?> type, Field field)
			throws ScraperValidationException;

	/** Exception indicating an validation error. */
	public class ScraperValidationException extends ScraperException
	{

		/** The field which validation failed for. */
		private final Field field;

		/** The data that caused the validation to fail. */
		private final Object data;

		/** Constructor. */
		public ScraperValidationException(
				Field field,
				Object data,
				String message)
		{
			super(message);
			this.field = field;
			this.data = data;
		}

		/** @return The field which validation failed for. */
		public Field getField()
		{
			return this.field;
		}

		/** @return The data that caused the validation to fail. */
		public Object getData()
		{
			return this.data;
		}
	}
}
