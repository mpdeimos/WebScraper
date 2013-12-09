package com.mpdeimos.webscraper.validation;

import com.mpdeimos.webscraper.scraper.ScraperContext;

/**
 * Default validator that is always passing.
 * 
 * @author mpdeimos
 */
public class DefaultValidator implements Validator
{
	/** {@inheritDoc} */
	@Override
	public void validate(ScraperContext context)
			throws ScraperValidationException
	{
		// always pass
	}
}
