package com.mpdeimos.webscraper;

import java.io.IOException;

import org.jsoup.Connection;
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
	/** Options for Http Connections. */
	public static class ConnectionOptions
	{
		/**
		 * The user agent to use. <code>null</code> means default
		 * implementation.
		 */
		public String userAgent = null;

		/**
		 * The amount of retries before an HTTP request fails. Default: {@value}
		 */
		public int retries = 3;
	}

	/** The default Http connection options. */
	private static final ConnectionOptions DEFAULT_HTTP_OPTIONS = new ConnectionOptions();

	/**
	 * Creates a {@link ScraperSource} from the document accessible from the
	 * given URL with a default of {@value #DEFAULT_RETRIES} retries.
	 */
	public static ScraperSource fromUrl(String url)
	{
		return fromUrl(url, DEFAULT_HTTP_OPTIONS);
	}

	/**
	 * Creates a {@link ScraperSource} from the document accessible from the
	 * given URL and specifies the Http connection options.
	 */
	public static ScraperSource fromUrl(
			final String url,
			final ConnectionOptions options)
	{
		return new ScraperSource()
		{
			@Override
			public Element getElement() throws ScraperException
			{
				return fetchDocument(url, options);
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

	/** Fetches the document with the given Http connection options. */
	private static Document fetchDocument(String url, ConnectionOptions options)
			throws ScraperException
	{
		if (options == null)
		{
			options = DEFAULT_HTTP_OPTIONS;
		}

		for (int retries = options.retries; retries >= 0; retries--)
		{
			try
			{
				Connection connection = Jsoup.connect(url);
				if (options.userAgent != null)
				{
					connection.userAgent(options.userAgent);
				}

				return connection.get();
			}
			catch (IOException e)
			{
				if (retries == 0)
				{
					throw new ScraperException(
							"Could not connect to website", //$NON-NLS-1$
							e);
				}
			}
		}

		throw new IllegalStateException("This state should not be reached"); //$NON-NLS-1$
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
