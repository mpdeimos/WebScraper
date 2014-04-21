package com.mpdeimos.webscraper.implementation.async;

import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Asynchronous executor of {@link Callable}s that can {@link #await()} all
 * scheduled tasks.
 * 
 * @author mpdeimos
 */
public class AsyncExecutor
{
	/** The unique id provider of the executor. */
	private static final AtomicInteger ID_PROVIDER = new AtomicInteger();

	/** The unique id if the executor. */
	private final int id = ID_PROVIDER.incrementAndGet();

	/** The executor service for scraping the document. */
	private final ExecutorService executor;

	/**
	 * The results of tasks added to the {@link #executor}. The collection is
	 * thread safe.
	 */
	private final List<Future<Void>> futures = Collections.synchronizedList(
			new ArrayList<Future<Void>>());

	/** Constructor. */
	private AsyncExecutor(int nThreads)
	{
		this.executor = Executors.newFixedThreadPool(
				nThreads, new AsyncExecutorThreadFactory());
	}

	/**
	 * Submits a task to the underlying executor service.
	 */
	public void async(Callable<Void> task)
	{
		this.futures.add(this.executor.submit(task));
	}

	/**
	 * Waits till all asynchronous tasks are completed if called from any thread
	 * but {@link AsyncExecutorThread}.
	 * <p>
	 * Thrown checked exception in async context are wrapped in
	 * {@link ScraperException}s and rethrown. {@link RuntimeException}s,
	 * {@link ScraperException}s and {@link Error}s are rethrown.
	 */
	public void await() throws ScraperException
	{
		if (Thread.currentThread() instanceof AsyncExecutorThread)
		{
			return;
		}

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
				Assert.notCaught(cause, "Unexpected exception in async context"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Creates a new {@link AsyncExecutor} of uses the {@link AsyncExecutor} of
	 * the current thread if the thread is a {@link AsyncExecutorThread}.
	 */
	public static AsyncExecutor createOrGetCurrent(int nThreads)
	{
		Thread thread = Thread.currentThread();
		if (thread instanceof AsyncExecutorThread)
		{
			return ((AsyncExecutorThread) thread).getAsyncExecutor();
		}
		return new AsyncExecutor(nThreads);
	}

	/** A {@link Thread} created by an {@link AsyncExecutor}. */
	private class AsyncExecutorThread extends Thread
	{
		/** Constructor. */
		private AsyncExecutorThread(Runnable r, String name)
		{
			super(r, name);
		}

		/**
		 * @return The {@link AsyncExecutor} that has created this
		 *         {@link Thread}.
		 */
		public AsyncExecutor getAsyncExecutor()
		{
			return AsyncExecutor.this;
		}
	}

	/** Factory for {@link AsyncExecutorThread}s. */
	private class AsyncExecutorThreadFactory implements ThreadFactory
	{
		/** The unique id provider for created threads. */
		private final AtomicInteger THREAD_ID_PROVIDER = new AtomicInteger();

		/** {@inheritDoc} */
		@Override
		public Thread newThread(Runnable r)
		{
			return new AsyncExecutorThread(r, "AsyncExecutor-" //$NON-NLS-1$
					+ AsyncExecutor.this.id + "-Thread-" //$NON-NLS-1$
					+ this.THREAD_ID_PROVIDER.incrementAndGet());
		}
	}
}
