package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.util.Reflections;

import java.lang.reflect.Field;

/**
 * Default converter that is handling strings, primitive types and enumerations.
 * 
 * @author mpdeimos
 */
public class DefaultConverter implements Converter
{

	/** {@inheritDoc} */
	@Override
	public Object convert(String textData, Class<?> type, Field field)
			throws ScraperException
	{
		if (Reflections.isPrimitiveOrWrapper(type))
		{
			return Reflections.stringToPrimitive(type, textData);
		}
		else if (type.isEnum())
		{
			return enumForTextData(type, textData);
		}

		return type.cast(textData);
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
