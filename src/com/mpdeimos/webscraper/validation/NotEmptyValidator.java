package com.mpdeimos.webscraper.validation;

import java.lang.reflect.Field;

/**
 * Validator for enforcing non-empty text.
 * 
 * @author mpdeimos
 */
public class NotEmptyValidator implements Validator
{
	/** {@inheritDoc} */
	@Override
	public void validate(String data, Class<?> type, Field field)
			throws ScraperValidationException
	{
		if (data == null || data.isEmpty())
		{
			throw new ScraperValidationException(
					field,
					data, "The scraped text is empty."); //$NON-NLS-1$
		}
	}
}
