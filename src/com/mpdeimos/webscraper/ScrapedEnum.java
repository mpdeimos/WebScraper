package com.mpdeimos.webscraper;

/**
 * Interface for enumerations that are constructed from scraping text.
 * 
 * @author mpdeimos
 */
public interface ScrapedEnum
{
	/**
	 * @return <code>true</code> if the enumeration corresponds to the scraped
	 *         text.
	 */
	public boolean equalsScrapedData(String data);
}
