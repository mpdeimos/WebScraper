package com.mpdeimos.webscraper.conversion;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import com.mpdeimos.webscraper.Scrape;
import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.ScraperSource;
import com.mpdeimos.webscraper.conversion.ConstructConverter.EArgumentType;

/** Tests {@link ConstructConverter} */
public class ConstructConverterTest
{
	@Test
	/** Tests creating an object with default arguments. */
	public void testWithDefaultArguments() throws ScraperException
	{
		ItemWithDefaultArgs item = new ItemWithDefaultArgs();
		Scraper.builder()
				.add(ScraperSource.fromHtml("<root><node>text</node></root>"),
						item).build().scrape();

		Assert.assertEquals("text", item.value.text);
	}

	@Test
	/** Tests creating an object with several arguments. */
	public void testWithAllArguments() throws ScraperException
	{
		ItemWithAllArgs item = new ItemWithAllArgs();
		Scraper.builder()
				.add(ScraperSource.fromHtml("<root><node>text</node></root>"),
						item).build().scrape();

		ScraperContext context = item.value.context;
		Assert.assertNotNull(context);
		Assert.assertEquals("#root", context.getRootElement().tagName());
		Assert.assertEquals("node", context.getSourceElement().tagName());
		Assert.assertEquals("text", context.getSourceElement().text());

		Assert.assertEquals("node", item.value.element.tagName());
		Assert.assertEquals("text", item.value.element.text());

		Assert.assertEquals("text", item.value.text);

		Assert.assertEquals("foo", item.value.arg1);
		Assert.assertEquals("bar", item.value.arg2);
	}

	/** Item that contains an object constructed with default arguments. */
	public static class ItemWithDefaultArgs
	{
		/** Dummy attribute that causes the ThreadNameExtractor to be called. */
		@Scrape(value = "node", converter = ConstructConverter.class)
		public ConstructedWithDefaultArgs value;
	}

	/** Item that contains an object constructed with all arguments. */
	public static class ItemWithAllArgs
	{
		/** Dummy attribute that causes the ThreadNameExtractor to be called. */
		@Scrape(value = "node", converter = ConstructConverter.class)
		@ConstructConverter.Option(value = { EArgumentType.CONTEXT,
				EArgumentType.ELEMENT, EArgumentType.TEXT }, strings = {
				"foo", "bar" })
		public ConstructedWithAllArgs value;
	}

	/** Object constructed with default arguments. */
	public static class ConstructedWithDefaultArgs
	{
		/** The text of this element. */
		private final String text;

		/** Constructor. */
		public ConstructedWithDefaultArgs(String text)
		{
			this.text = text;
		}
	}

	/** Object constructed with all arguments. */
	public static class ConstructedWithAllArgs
	{
		/** The scraper context. */
		private final ScraperContext context;

		/** The currently scraped element. */
		private final Element element;

		/** The text of this element. */
		private final String text;

		/** The first string argument */
		private final String arg1;

		/** The second string argument. */
		private final String arg2;

		/** Constructor. */
		public ConstructedWithAllArgs(ScraperContext context, Element element,
				String text, String arg1, String arg2)
		{
			this.context = context;
			this.element = element;
			this.text = text;
			this.arg1 = arg1;
			this.arg2 = arg2;
		}
	}
}
