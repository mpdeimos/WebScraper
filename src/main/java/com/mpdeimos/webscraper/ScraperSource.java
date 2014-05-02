package com.mpdeimos.webscraper;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Class that encapsulates a means to retrieve a scrapable {@link Element}.
 * 
 * @author mpdeimos
 */
public abstract class ScraperSource
{
	/** Default amount of retries before an HTTP request fails. */
	private static final int DEFAULT_RETRIES = 3;

	/**
	 * Creates a {@link ScraperSource} from the document accessible from the
	 * given URL with a default of {@value #DEFAULT_RETRIES} retries.
	 */
	public static ScraperSource fromUrl(String url)
	{
		return fromUrl(url, DEFAULT_RETRIES);
	}

	/**
	 * Creates a {@link ScraperSource} from the document accessible from the
	 * given URL and specifies the maximum amount of retries.
	 */
	public static ScraperSource fromUrl(final String url, final int retries)
	{
		return new ScraperSource()
		{
			@Override
			public Element getElement() throws ScraperException
			{
				return fetchDocument(url, retries);
			}
		};
	}

	/**
	 * Creates a {@link ScraperSource} from an HTML text.
	 */
	public static ScraperSource fromHtml(String html)
	{
		return new DefaultScraperSource(Jsoup.parse(html));
	}

	/**
	 * Creates a {@link ScraperSource} from {@link Element}.
	 */
	public static ScraperSource fromElement(Element element)
	{
		return new DefaultScraperSource(element);
	}

	/** Fetches the document with the given amount of retries. */
	private static Document fetchDocument(String url, int retries)
			throws ScraperException
	{
		Document doc;
		try
		{
			doc = Jsoup.connect(url).get();
		}
		catch (IOException e)
		{
			if (retries == 0)
			{
				throw new ScraperException("Could not connect to website", e); //$NON-NLS-1$
			}
			return fetchDocument(url, retries - 1);
		}
		return doc;
	}

	/** @return The {@link Element} that will be scraped. */
	public abstract Element getElement() throws ScraperException;

	/**
	 * Default implementation of a ScraperSource that works on an
	 * {@link Element}.
	 */
	private static class DefaultScraperSource extends ScraperSource
	{
		/** The element to scrape. */
		private final Element element;

		/** Constructor. */
		private DefaultScraperSource(Element element)
		{
			this.element = element;
		}

		/** {@inheritDoc} */
		@Override
		public Element getElement()
		{
			return this.element;
		}
	}

	/**
	 * Interface for objects that provide a {@link ScraperSource}. If a field
	 * annotated with {@link Scrape} implements this interface, the
	 * {@link ScraperSource} will be resolved and the objects scraped using a
	 * nested scraper.
	 */
	public interface ScraperSourceProvider
	{
		/** @return The {@link ScraperSource} to scrape. */
		public ScraperSource getSource();
	}
}
