package com.mpdeimos.webscraper.validation;

import java.lang.reflect.Field;

/**
 * Default validator that is always passing.
 * 
 * @author mpdeimos
 */
public class DefaultValidator implements Validator
{
	/** {@inheritDoc} */
	@Override
	public void validateScrapedData(String data, Class<?> type, Field field)
			throws ScraperValidationException
	{
		// always pass
	}
}
