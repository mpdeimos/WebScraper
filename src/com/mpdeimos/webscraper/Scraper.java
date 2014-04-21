package com.mpdeimos.webscraper;

import com.mpdeimos.webscraper.ScraperSource.ScraperSourceProvider;
import com.mpdeimos.webscraper.implementation.ThreadedScraperBuilder;

import java.util.Collection;

import org.jsoup.nodes.Element;

/**
 * Class for scraping HTML documents to plain java objects. Use
 * {@link ScraperBuilder} for instantiating such a scraper instance.
 * 
 * @author mpdeimos
 */
public abstract class Scraper
{
	/**
	 * Scrapes the source HTML element (i.e. a website document) to the
	 * specified target object.
	 */
	public abstract void scrape() throws ScraperException;

	/** Interface for building a {@link Scraper}. */
	public interface ScraperBuilder
	{
		/**
		 * @return The newly created {@link Scraper} instance.
		 */
		public Scraper build();

		public ScraperBuilder add(ScraperSourceProvider sourceAndTarget);

		public ScraperBuilder add(ScraperSourceProvider... sourceAndTarget);

		public ScraperBuilder add(
				Collection<ScraperSourceProvider> sourceAndTarget);

		public ScraperBuilder add(ScraperSource source, Object target);

		public ScraperBuilder add(Element source, Object target);
	}

	/** Creates a new ScraperBuilder */
	public static ScraperBuilder builder()
	{
		return new ThreadedScraperBuilder();
	}
}
