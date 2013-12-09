package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.scraper.ScraperContext;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Converter for parsing formatted numbers.
 * 
 * @author mpdeimos
 */
public class NumberFormatConverter implements Converter
{

	/** {@inheritDoc} */
	@Override
	public Number convert(ScraperContext context) throws ScraperException
	{
		try
		{
			return NumberFormat.getInstance().parse(context.getSourceData());
		}
		catch (ParseException e)
		{
			throw new ScraperException("Failed parsing data", e); //$NON-NLS-1$
		}
	}
}
