package com.mpdeimos.webscraper.implementation;

import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.ScraperException;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * {@link Scraper} impelmentation that is capable for scraping multiple
 * documents distributed on several threads.
 * 
 * @author mpdeimos
 */
public class ThreadedScraper extends Scraper
{
	/** The asynchronous executor for executing the passed scrapers. */
	private final AsyncExecutor executor;

	/** The passed scrapers to be executed. */
	private final List<? extends Scraper> scrapers;

	/** Constructor. */
	public ThreadedScraper(
			AsyncExecutor executor,
			List<? extends Scraper> scrapers)
	{
		this.executor = executor;
		this.scrapers = scrapers;
	}

	/** {@inheritDoc} */
	@Override
	public void scrape() throws ScraperException
	{
		for (final Scraper scraper : this.scrapers)
		{
			this.executor.async(new Callable<Void>()
			{
				@Override
				public Void call() throws ScraperException
				{
					scraper.scrape();
					return null;
				}
			});
		}

		this.executor.await();
	}
}
