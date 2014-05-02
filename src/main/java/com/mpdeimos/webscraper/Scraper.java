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

	/**
	 * Interface for building a {@link Scraper}. Use the methods {@link #add()}
	 * to add objects to scrape and {@link #build()} to create the
	 * {@link Scraper}.
	 */
	public interface ScraperBuilder
	{
		/**
		 * @return The newly created {@link Scraper} instance.
		 */
		public Scraper build();

		/**
		 * Adds a {@link ScraperSourceProvider} to the list of scraped objects.
		 */
		public ScraperBuilder add(ScraperSourceProvider sourceAndTarget);

		/**
		 * Adds multiple {@link ScraperSourceProvider}s to the list of scraped
		 * objects.
		 */
		public ScraperBuilder add(ScraperSourceProvider... sourceAndTarget);

		/**
		 * Adds multiple {@link ScraperSourceProvider}s to the list of scraped
		 * objects.
		 */
		public ScraperBuilder add(
				Collection<ScraperSourceProvider> sourceAndTarget);

		/**
		 * Adds a source and target to the list of scraped objects.
		 */
		public ScraperBuilder add(ScraperSource source, Object target);

		/**
		 * Adds a source element and target to the list of scraped objects.
		 */
		public ScraperBuilder add(Element source, Object target);

		/**
		 * Sets the number of threads that are used for scraping. Default equals
		 * to he number of available processors.
		 */
		public ScraperBuilder setParallelism(int nThreads);
	}

	/** Creates a new ScraperBuilder */
	public static ScraperBuilder builder()
	{
		return new ThreadedScraperBuilder();
	}
}
