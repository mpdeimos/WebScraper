package com.mpdeimos.webscraper.scraper;

import com.mpdeimos.webscraper.Scrape;

import java.lang.reflect.Field;

import org.jsoup.nodes.Element;

/**
 * Context for scraping including the queried element, the field target type.
 * 
 * @author mpdeimos
 */
public class ScraperContext
{
	/** The source element that has been selected by the CSS query. */
	/* package */Element sourceElement;

	/** The source data that has been extracted from the source element. */
	/* package */String sourceData;

	/** The type of the target object. */
	/* package */Class<?> targetType;

	/** The target field. */
	/* package */Field targetField;

	/** The scraper configuration. */
	/* package */Scrape scrape;

	/** Constructor. */
	/* package */ScraperContext()
	{
		// explicitly make package private.
	}

	/**
	 * @return The source element that has been selected by the CSS query.
	 */
	public Element getSourceElement()
	{
		return this.sourceElement;
	}

	/**
	 * @return The source data that has been extracted from the source element.
	 */
	public String getSourceData()
	{
		return this.sourceData;
	}

	/** @return The type of the target object. */
	public Class<?> getTargetType()
	{
		return this.targetType;
	}

	/** @return The target field. */
	public Field getTargetField()
	{
		return this.targetField;
	}

}
