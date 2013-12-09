package com.mpdeimos.webscraper.validation;

import com.mpdeimos.webscraper.scraper.ScraperContext;

/**
 * Validator for enforcing non-empty text.
 * 
 * @author mpdeimos
 */
public class NotEmptyValidator implements Validator
{
	/** {@inheritDoc} */
	@Override
	public void validate(ScraperContext context)
			throws ScraperValidationException
	{
		String data = context.getSourceData();
		if (data == null || data.isEmpty())
		{
			throw new ScraperValidationException(
					context,
					"The scraped text is empty."); //$NON-NLS-1$
		}
	}
}
