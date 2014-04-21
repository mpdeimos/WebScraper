package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;

/**
 * Converter that continues scraping with target object. For this it has to
 * instantiate the target object with the default, parameterless constructor.
 * <p>
 * The method {@link #instantiateTargetObject(Class)} may be overridden by
 * implementing classes in order to provide a custom instantiation logic.
 * 
 * @author mpdeimos
 */
public class DeepScrapeConverter implements Converter
{
	/** {@inheritDoc} */
	@Override
	public final Object convert(ScraperContext context) throws ScraperException
	{
		Class<?> targetType = context.getTargetType();
		Object instance;
		instance = instantiateTargetObject(targetType);

		Scraper scraper = Scraper.builder().add(
				context.getSourceElement(),
				instance).build();
		scraper.scrape();
		return instance;
	}

	/**
	 * Instantiates a new instance of the target object using the default
	 * parameterless constructor or throws an exception if not instantiable.
	 */
	protected Object instantiateTargetObject(Class<?> targetType)
			throws ScraperException
	{
		try
		{
			return targetType.newInstance();
		}
		catch (ReflectiveOperationException e)
		{
			throw new ScraperException("Could not instantiate " //$NON-NLS-1$
					+ targetType.getCanonicalName(), e);
		}
	}
}
