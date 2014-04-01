package com.mpdeimos.webscraper;

import java.lang.reflect.Field;

import org.jsoup.nodes.Element;

/**
 * Context for scraping including the queried element, the field target type.
 * 
 * @author mpdeimos
 */
public abstract class ScraperContext
{
	/** The root element that is used for the CSS query. */
	protected Element rootElement;

	/** The source element that has been selected by the CSS query. */
	protected Element sourceElement;

	/** The source text that has been extracted from the source element. */
	protected String sourceText;

	/** The type of the target object. */
	protected Class<?> targetType;

	/** The target field. */
	protected Field targetField;

	/**
	 * @return The root element that is used for the CSS query.
	 */
	public Element getRootElement()
	{
		return this.rootElement;
	}

	/**
	 * @return The source element that has been selected by the CSS query.
	 */
	public Element getSourceElement()
	{
		return this.sourceElement;
	}

	/**
	 * @return The source text that has been extracted from the source element.
	 */
	public String getSourceText()
	{
		return this.sourceText;
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
