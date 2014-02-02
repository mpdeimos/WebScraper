package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
	public Date convert(ScraperContext context) throws ScraperException
	{
		try
		{
			DateFormat format = DateFormat.getInstance();
			if (context.getTargetField().isAnnotationPresent(Option.class))
			{
				Option option = context.getTargetField().getAnnotation(
						Option.class);
				format = new SimpleDateFormat(option.value(), Locale.US);
			}
			return format.parse(context.getSourceData());
		}
		catch (ParseException e)
		{
			throw new ScraperException("Failed parsing data", e); //$NON-NLS-1$
		}
	}
}
