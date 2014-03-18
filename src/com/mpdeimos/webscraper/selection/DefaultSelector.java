package com.mpdeimos.webscraper.selection;

import com.mpdeimos.webscraper.ScraperContext;

import org.jsoup.nodes.Element;

/**
 * Default element selector that passes root element of the context.
 * 
 * @author mpdeimos
 */
public class DefaultSelector implements Selector
{
	/** {@inheritDoc} */
	@Override
	public Element select(ScraperContext context)
	{
		return context.getRootElement();
	}
}
