package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converter for parsing formatted numbers.
 * 
 * @author mpdeimos
 */
public class DateFormatConverter implements Converter
{
	/** Option annotation for {@link DateFormatConverter}. */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Option
	{
		/** The date format parsing pattern. */
		public String value();
	}

	/** {@inheritDoc} */
	@Override
	public Date convert(String textData, Class<?> type, Field field)
			throws ScraperException
	{
		try
		{
			DateFormat format = DateFormat.getInstance();
			if (field.isAnnotationPresent(Option.class))
			{
				Option option = field.getAnnotation(Option.class);
				format = new SimpleDateFormat(option.value());
			}
			return format.parse(textData);
		}
		catch (ParseException e)
		{
			throw new ScraperException("Failed parsing data", e); //$NON-NLS-1$
		}
	}
}
