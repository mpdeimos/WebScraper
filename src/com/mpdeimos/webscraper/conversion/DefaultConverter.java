package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.scraper.ScraperContext;
import com.mpdeimos.webscraper.util.Reflections;

/**
 * Default converter that is handling strings, primitive types and enumerations.
 * 
 * @author mpdeimos
 */
public class DefaultConverter implements Converter
{

	/** {@inheritDoc} */
	@Override
	public Object convert(ScraperContext context)
			throws ScraperException
	{
		Class<?> type = context.getTargetType();
		String data = context.getSourceData();

		if (Reflections.isPrimitiveOrWrapper(type))
		{
			return Reflections.stringToPrimitive(type, data);
		}
		else if (type.isEnum())
		{
			return enumForTextData(type, data);
		}

		return type.cast(data);
	}

	/** @return the enumeration value corresponding to scraped data. */
	@SuppressWarnings("unchecked")
	private Object enumForTextData(Class<?> fieldType, String textData)
			throws ScraperException
	{
		if (ScrapedEnum.class.isAssignableFrom(fieldType))
		{
			for (ScrapedEnum scrapedEnum : (ScrapedEnum[]) fieldType.getEnumConstants())
			{
				if (scrapedEnum.equalsScrapedData(textData))
				{
					return scrapedEnum;
				}
			}
		}
		@SuppressWarnings("rawtypes")
		Class<? extends Enum> enumClazz = (Class<? extends Enum>) fieldType;
		try
		{
			return Enum.valueOf(enumClazz, textData);
		}
		catch (IllegalArgumentException e)
		{
			throw new ScraperException("Could not convert '" + textData //$NON-NLS-1$
					+ "' to enum " + fieldType, e); //$NON-NLS-1$
		}
	}

	/**
	 * Interface for enumerations that are constructed from scraping text.
	 */
	public interface ScrapedEnum
	{
		/**
		 * @return <code>true</code> if the enumeration corresponds to the
		 *         scraped text.
		 */
		public boolean equalsScrapedData(String data);
	}
}
