package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperException;

import java.lang.reflect.Field;
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
	public Object convert(String textData, Class<?> type, Field field)
			throws ScraperException
	{
		try
		{
			return NumberFormat.getInstance().parse(textData);
		}
		catch (ParseException e)
		{
			throw new ScraperException("Failed parsing data", e); //$NON-NLS-1$
		}
	}
}
