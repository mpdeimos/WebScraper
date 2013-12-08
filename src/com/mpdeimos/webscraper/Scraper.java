package com.mpdeimos.webscraper;

import org.jsoup.nodes.Element;

/**
 * Interface definition for scraping HTML documents to plain java objects. Use
 * {@link Builder} for instantiating such a scraper instance.
 * 
 * @author mpdeimos
 */
public interface Scraper
{
	/**
	 * Scrapes the source HTML element (i.e. a website document) to the
	 * specified destination object.
	 */
	public void scrape() throws ScraperException;

	/**
	 * Builder factory for {@link Scraper} instances.
	 * 
	 * The default implementation uses {@link Scrape} annotations to define
	 * scraping rules.
	 */
	public class Builder
	{
		/** The source HTML Element. */
		private Element source;

		/** The destination object to scrape the website into. */
		private Object destination;

		/**
		 * @return The newly created {@link Scraper} instance.
		 * @throws NullPointerException
		 *             if either source or destination is <code>null</code>.
		 */
		public Scraper build()
		{
			if (this.source == null || this.destination == null)
			{
				throw new NullPointerException();
			}

			return new AnnotatedScraper(this.source, this.destination);
		}

		/** Sets the source HTML element to scrape data from. */
		public Builder setSource(Element source)
		{
			this.source = source;
			return this;
		}

		/** Sets the destination object to scrape data to. */
		public Builder setDestination(Object destination)
		{
			this.destination = destination;
			return this;
		}
	}

}
