package com.mpdeimos.webscraper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Asynchronous executor of {@link Callable}s that can {@link #await()} all
 * scheduled tasks.
 * 
 * @author mpdeimos
 */
public class AsyncExecutor<T>
{
	/** The executor service for scraping the document. */
	private final ExecutorService executor;

	/**
	 * The results of tasks added to the {@link #executor}. The collection is
	 * thread safe.
	 */
	private final List<Future<T>> futures = Collections.synchronizedList(
			new ArrayList<Future<T>>());

	/** Constructor. */
	public AsyncExecutor()
	{
		this(16);
	}

	/** Constructor. */
	public AsyncExecutor(int nThreads)
	{
		this.executor = Executors.newFixedThreadPool(nThreads);
	}

	/**
	 * Submits a task to the underlying executor service.
	 */
	public void async(Callable<T> task)
	{
		this.futures.add(this.executor.submit(task));
	}

	/**
	 * Waits till all asynchronous tasks are completed.
	 * <p>
	 * Thrown checked exception in async context are wrapped in
	 * {@link ScraperException}s and rethrown. {@link RuntimeException}s,
	 * {@link ScraperException}s and {@link Error}s are rethrown.
	 */
	public void await() throws ScraperException
	{
		while (!this.futures.isEmpty())
		{
			try
			{
				this.futures.remove(0).get();
			}
			catch (InterruptedException e)
			{
				throw new ScraperException("Scraper thread interruped", e); //$NON-NLS-1$
			}
			catch (ExecutionException e)
			{
				Throwable cause = e.getCause();
				if (cause instanceof ScraperException)
				{
					throw (ScraperException) cause;
				}
				if (cause instanceof Error)
				{
					throw (Error) cause;
				}
				if (cause instanceof RuntimeException)
				{
					throw (RuntimeException) cause;
				}
				throw new ScraperException("Unexpected exception", cause); //$NON-NLS-1$
			}
		}
	}
}
