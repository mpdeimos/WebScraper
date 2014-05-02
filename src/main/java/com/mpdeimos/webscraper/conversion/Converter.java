package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;

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
	public Object convert(ScraperContext context)
			throws ScraperException;

}
