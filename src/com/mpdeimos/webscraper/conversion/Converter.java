package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperException;

import java.lang.reflect.Field;

/**
 * Interface for converting scraped content.
 * 
 * @author mpdeimos
 */
public interface Converter
{
	/**
	 * Converts the scraped data to a desired data type.
	 */
	public Object convert(String textData, Class<?> type, Field field)
			throws ScraperException;

}
