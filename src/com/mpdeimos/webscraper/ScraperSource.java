package com.mpdeimos.webscraper;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class ScraperSource
{
	private static final int DEFAULT_RETRIES = 3;

	public static ScraperSource fromUrl(String url)
	{
		return fromUrl(url, DEFAULT_RETRIES);
	}

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

	public static ScraperSource fromHtml(String html)
	{
		return new DefaultScraperSource(Jsoup.parse(html));
	}

	public static ScraperSource fromElement(Element element)
	{
		return new DefaultScraperSource(element);
	}

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
				throw new ScraperException("Could not connect to website", e);
			}
			return fetchDocument(url, retries--);
		}
		return doc;
	}

	public abstract Element getElement() throws ScraperException;

	private static class DefaultScraperSource extends ScraperSource
	{
		private final Element element;

		private DefaultScraperSource(Element element)
		{
			this.element = element;
		}

		@Override
		public Element getElement()
		{
			return element;
		}
	}

	public interface ScraperSourceProvider
	{
		public ScraperSource getSource();
	}
}
