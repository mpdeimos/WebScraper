package com.mpdeimos.webscraper.tests;

import com.mpdeimos.webscraper.Scrape;
import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.Scraper.ScraperBuilder;
import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.ScraperSource;
import com.mpdeimos.webscraper.ScraperSource.ScraperSourceProvider;
import com.mpdeimos.webscraper.conversion.ConstructConverter;
import com.mpdeimos.webscraper.conversion.ConstructConverter.EArgumentType;
import com.mpdeimos.webscraper.conversion.Converter;
import com.mpdeimos.webscraper.conversion.DeepScrapeConverter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests scraping parallelism.
 * 
 * @author mpdeimos
 */
public class ThreadingTest
{
	/** The default parallelism level (amount of threads). */
	private static final int PARALLELISM = 32;

	/**
	 * Tests that scraping the same amount of items as threads will occupy all
	 * threads.
	 */
	@Test
	public void testScrapingSameAsExecutors() throws Exception
	{
		ThreadExtractingItem comparee = new ThreadExtractingItem();
		comparee.value = "text"; //$NON-NLS-1$

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ThreadExtractingItem.class,
				PARALLELISM,
				PARALLELISM);
	}

	/**
	 * Tests that scraping less items than threads will occupy the same amount
	 * of threads as items.
	 */
	@Test
	public void testScrapingLessThanExecutors() throws Exception
	{
		ThreadExtractingItem comparee = new ThreadExtractingItem();
		comparee.value = "text"; //$NON-NLS-1$

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ThreadExtractingItem.class,
				PARALLELISM / 2,
				PARALLELISM / 2);
	}

	/**
	 * Tests that scraping more items than threads cannot cannot exceed the
	 * total amount of threads.
	 */
	@Test
	public void testScrapingMoreThanExecutors() throws Exception
	{
		ThreadExtractingItem comparee = new ThreadExtractingItem();
		comparee.value = "text"; //$NON-NLS-1$

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ThreadExtractingItem.class,
				PARALLELISM * 2,
				PARALLELISM);
	}

	/**
	 * Tests recursive scraping using a {@link ScraperSourceProvider}.
	 */
	@Test
	public void testSourceProviderRecursion() throws Exception
	{
		ConstructingThreadExtractingItem comparee = new ConstructingThreadExtractingItem();
		comparee.value = "text"; //$NON-NLS-1$
		comparee.constructed = new ThreadExtractingItem();
		comparee.constructed.value = "text"; //$NON-NLS-1$

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ConstructingThreadExtractingItem.class,
				1,
				2);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ConstructingThreadExtractingItem.class,
				PARALLELISM / 4,
				PARALLELISM / 2);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ConstructingThreadExtractingItem.class,
				PARALLELISM / 2,
				PARALLELISM);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				ConstructingThreadExtractingItem.class,
				PARALLELISM,
				PARALLELISM);
	}

	/**
	 * Tests recursive scraping using {@link DeepScrapeConverter}.
	 */
	@Test
	public void testDeepScrapeRecursion() throws Exception
	{
		DeepScrapeThreadExtractingItem comparee = new DeepScrapeThreadExtractingItem();
		comparee.value = "text"; //$NON-NLS-1$
		comparee.deepScraped = new SimpleThreadExtractingItem();
		comparee.deepScraped.value = "text"; //$NON-NLS-1$

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeThreadExtractingItem.class,
				1,
				2);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeThreadExtractingItem.class,
				PARALLELISM / 4,
				PARALLELISM / 2);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeThreadExtractingItem.class,
				PARALLELISM / 2,
				PARALLELISM);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeThreadExtractingItem.class,
				PARALLELISM,
				PARALLELISM);
	}

	/**
	 * Tests recursive array scraping using {@link DeepScrapeConverter}.
	 */
	@Test
	public void testDeepScrapeArrayRecursion() throws Exception
	{
		DeepScrapeArrayThreadExtractingItem comparee = new DeepScrapeArrayThreadExtractingItem();
		comparee.value = "abc"; //$NON-NLS-1$
		comparee.deepScraped = new SimpleThreadExtractingItem[3];
		comparee.deepScraped[0] = new SimpleThreadExtractingItem();
		comparee.deepScraped[0].value = "a"; //$NON-NLS-1$
		comparee.deepScraped[1] = new SimpleThreadExtractingItem();
		comparee.deepScraped[1].value = "b"; //$NON-NLS-1$
		comparee.deepScraped[2] = new SimpleThreadExtractingItem();
		comparee.deepScraped[2].value = "c"; //$NON-NLS-1$

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeArrayThreadExtractingItem.class,
				1,
				4);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeArrayThreadExtractingItem.class,
				PARALLELISM / 8,
				PARALLELISM / 2);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeArrayThreadExtractingItem.class,
				PARALLELISM / 4,
				PARALLELISM);

		assertScrapingNItemsYieldsMThreads(
				comparee,
				DeepScrapeArrayThreadExtractingItem.class,
				PARALLELISM,
				PARALLELISM);
	}

	/** Asserts that scraping N items will yield M threads. */
	private <T extends ScraperSourceProvider> T[] assertScrapingNItemsYieldsMThreads(
			T comparee,
			Class<T> clazz,
			int n,
			int m)
			throws Exception
	{
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(clazz, n);
		@SuppressWarnings("unchecked")
		T[] comparees = (T[]) Array.newInstance(clazz, n);
		Arrays.fill(comparees, comparee);

		ScraperBuilder builder = Scraper.builder().setParallelism(PARALLELISM);
		ThreadNameExtractor.threadNames.clear();
		for (int i = 0; i < n; i++)
		{
			array[i] = clazz.newInstance();
			builder.add(array[i]);
		}
		builder.build().scrape();

		Assert.assertEquals(m, ThreadNameExtractor.threadNames.size());

		Assert.assertEquals(
				Arrays.toString(comparees),
				Arrays.toString(array));

		return array;
	}

	/** Creates a dummy scraper source. */
	private static ScraperSource dummySource()
	{
		return ScraperSource.fromHtml("<root>text</root>"); //$NON-NLS-1$;
	}

	/** Item to store scraped data in. */
	public static class SimpleThreadExtractingItem
	{
		/** Dummy attribute that causes the ThreadNameExtractor to be called. */
		@Scrape(value = ":root", converter = ThreadNameExtractor.class)
		public String value;

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{value=" + this.value + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/** Item to store scraped data in. */
	public static class ThreadExtractingItem implements ScraperSourceProvider
	{
		/** Dummy attribute that causes the ThreadNameExtractor to be called. */
		@Scrape(value = ":root", converter = ThreadNameExtractor.class)
		public String value;

		/** The source to be scraped. */
		private final ScraperSource source;

		/** Constructor. */
		public ThreadExtractingItem(Element source)
		{
			this.source = ScraperSource.fromElement(source);
		}

		/** Constructor. */
		public ThreadExtractingItem()
		{
			this.source = dummySource();
		}

		/** {@inheritDoc} */
		@Override
		public ScraperSource getSource()
		{
			return this.source;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{value=" + this.value + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/** Item to store scraped data in. */
	public static class ConstructingThreadExtractingItem implements
			ScraperSourceProvider
	{
		/** Dummy attribute that causes the ThreadNameExtractor to be called. */
		@Scrape(value = ":root", converter = ThreadNameExtractor.class)
		public String value;

		/** Constructed item */
		@Scrape(value = ":root", converter = ConstructConverter.class)
		@ConstructConverter.Option(EArgumentType.ELEMENT)
		public ThreadExtractingItem constructed;

		/** {@inheritDoc} */
		@Override
		public ScraperSource getSource()
		{
			return dummySource();
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{value=" + this.value + ",constructed=" + String.valueOf(this.constructed) + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/** Item to store scraped data in. */
	public static class DeepScrapeThreadExtractingItem implements
			ScraperSourceProvider
	{
		/**
		 * Dummy attribute that causes the ThreadNameExtractor to be called.
		 */
		@Scrape(value = ":root", converter = ThreadNameExtractor.class)
		public String value;

		/** Constructed item */
		@Scrape(value = ":root", converter = DeepScrapeConverter.class)
		public SimpleThreadExtractingItem deepScraped;

		/** {@inheritDoc} */
		@Override
		public ScraperSource getSource()
		{
			return dummySource();
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return "{value=" + this.value + ",deepScraped=" + String.valueOf(this.deepScraped) + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/** Item to store scraped data in. */
	public static class DeepScrapeArrayThreadExtractingItem implements
			ScraperSourceProvider
	{
		/**
		 * Dummy attribute that causes the ThreadNameExtractor to be called.
		 */
		@Scrape(value = ":root", converter = ThreadNameExtractor.class)
		public String value;

		/** Constructed item */
		@Scrape(value = ":root n", converter = DeepScrapeConverter.class)
		public SimpleThreadExtractingItem[] deepScraped;

		/** {@inheritDoc} */
		@Override
		public ScraperSource getSource()
		{
			return ScraperSource.fromHtml("<r>" //$NON-NLS-1$
					+ "<n>a</n>" //$NON-NLS-1$
					+ "<n>b</n>" //$NON-NLS-1$
					+ "<n>c</n>" //$NON-NLS-1$
					+ "</r>"); //$NON-NLS-1$
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			List<SimpleThreadExtractingItem> list = new ArrayList<SimpleThreadExtractingItem>(
					Arrays.asList(this.deepScraped));
			Collections.sort(list, new Comparator<SimpleThreadExtractingItem>()
			{
				@Override
				public int compare(
						SimpleThreadExtractingItem a,
						SimpleThreadExtractingItem b)
				{
					return a.value.compareTo(b.value);
				}
			});
			return "{value=" + this.value + ",deepScraped=" //$NON-NLS-1$//$NON-NLS-2$
					+ list.toString() + "}"; //$NON-NLS-1$
		}
	}

	/**
	 * Dummy converter stores the name of the current thread in a
	 * {@link HashMap}.
	 */
	public static class ThreadNameExtractor implements Converter
	{
		/** The names of threads that this object was called with . */
		public static final Set<String> threadNames = Collections.synchronizedSet(new HashSet<String>());

		/** {@inheritDoc} */
		@Override
		public Object convert(ScraperContext context) throws ScraperException
		{
			String name = Thread.currentThread().getName();
			threadNames.add(name);
			return context.getSourceText();
		}
	}
}
