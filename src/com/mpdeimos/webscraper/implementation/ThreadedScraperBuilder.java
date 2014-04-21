package com.mpdeimos.webscraper.implementation;

import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.Scraper.ScraperBuilder;
import com.mpdeimos.webscraper.ScraperSource;
import com.mpdeimos.webscraper.ScraperSource.ScraperSourceProvider;
import com.mpdeimos.webscraper.implementation.async.AsyncExecutor;

import java.util.ArrayList;
import java.util.Collection;

import org.jsoup.nodes.Element;

/**
 * Implementation of a {@link ScraperBuilder} that builds an
 * {@link ThreadedScraper}.
 * <p>
 * The {@link ThreadedScraper} uses the same amount of threads as available
 * processors.
 * 
 * @author mpdeimos
 */
public class ThreadedScraperBuilder implements ScraperBuilder
{
	/** The amount of threads the scraper should use. */
	private final int nThreads = Runtime.getRuntime().availableProcessors();

	/** The annotated scrapers that are scraped within the threaded scraper. */
	private final ArrayList<Scraper> scrapers = new ArrayList<Scraper>();

	/** {@inheritDoc} */
	@Override
	public Scraper build()
	{
		AsyncExecutor executor = AsyncExecutor.createOrGetCurrent(this.nThreads);
		return new ThreadedScraper(executor, this.scrapers);
	}

	/** {@inheritDoc} */
	@Override
	public ScraperBuilder add(ScraperSourceProvider sourceAndTarget)
	{
		this.scrapers.add(new AnnotatedScraper(
				sourceAndTarget.getSource(),
				sourceAndTarget));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ScraperBuilder add(ScraperSourceProvider... sourceAndTargets)
	{
		for (ScraperSourceProvider sourceAndTarget : sourceAndTargets)
		{
			add(sourceAndTarget);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ScraperBuilder add(Collection<ScraperSourceProvider> sourceAndTargets)
	{
		for (ScraperSourceProvider sourceAndTarget : sourceAndTargets)
		{
			add(sourceAndTarget);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ScraperBuilder add(ScraperSource source, Object target)
	{
		this.scrapers.add(new AnnotatedScraper(source, target));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ScraperBuilder add(Element source, Object target)
	{
		return add(ScraperSource.fromElement(source), target);
	}
}
