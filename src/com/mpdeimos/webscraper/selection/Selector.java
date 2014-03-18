package com.mpdeimos.webscraper.selection;

import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;

import org.jsoup.nodes.Element;

/**
 * Interface for changing the selected root node.
 * 
 * @author mpdeimos
 */
public interface Selector
{
	/** Selects an root node based on the initial root node. */
	public Element select(ScraperContext context)
			throws ScraperException;
}
