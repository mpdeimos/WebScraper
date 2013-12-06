package com.mpdeimos.webscraper;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import com.mpdeimos.webscraper.util.Html;

/**
 * Main class for scraping websites to annotated java objects. For scraping all
 * accessible (public and non final) fields of the specified object with
 * {@link Scrape} annotations are taken into account.
 * 
 * @author mpdeimos
 */
public class AnnotatedScraper
{
	/** The html element used as source for data binding. */
	private final Element element;

	/** The annotated element to receive data from the mapped element. */
	private final Object object;

	/** Constructor. */
	public AnnotatedScraper(Element element, Object object)
	{
		this.element = element;
		this.object = object;
	}

	/** Performs mapping from the HTML Document to the annotated element. */
	public void scrape() throws ScraperException
	{
		for (final Field field : object.getClass().getDeclaredFields())
		{
			final Scrape scrape = field.getAnnotation(Scrape.class);
			if (scrape == null)
			{
				continue;
			}

			String selector = scrape.value();
			Element element = Html.firstElement(this.element, selector);
			String text = element.text();

			Pattern compile = Pattern.compile(scrape.regex());
			Matcher matcher = compile.matcher(text);
			if (matcher.matches())
			{
				text = matcher.replaceAll(scrape.replace());
			}

			if (scrape.trim())
			{
				text = text.trim();
			}

			try
			{
				field.set(this.object, text);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
