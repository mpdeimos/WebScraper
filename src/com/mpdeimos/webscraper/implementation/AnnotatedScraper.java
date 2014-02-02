package com.mpdeimos.webscraper.implementation;

import com.mpdeimos.webscraper.Scrape;
import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.conversion.Converter;
import com.mpdeimos.webscraper.util.Assert;
import com.mpdeimos.webscraper.validation.Validator;
import com.mpdeimos.webscraper.validation.Validator.ScraperValidationException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Main class for scraping HTML documents to annotated java objects. For
 * scraping all accessible (public and non final) fields of the specified object
 * with {@link Scrape} annotations are taken into account.
 * 
 * @author mpdeimos
 */
public class AnnotatedScraper implements Scraper
{
	/** The html element used as source for data binding. */
	private final Element source;

	/**
	 * The annotated target element to receive data from the source element.
	 */
	private final Object target;

	/** Constructor. */
	public AnnotatedScraper(Element element, Object object)
	{
		this.source = element;
		this.target = object;
	}

	/** {@inheritDoc} */
	@Override
	public void scrape() throws ScraperException
	{
		for (final Field field : getAcessibleAnnotatedFields(this.target))
		{
			AnnotatedScraperContext context = new AnnotatedScraperContext(field);

			Elements elements = this.source.select(context.getConfiguration().value());

			Object data = extractDataFromElements(context, elements);

			if (data != null || context.getConfiguration().empty())
			{
				setFieldData(field, data);
			}
		}
	}

	/** Extracts the data for a list of elements returned by a CSS query. */
	private Object extractDataFromElements(
			AnnotatedScraperContext context,
			Elements elements)
			throws ScraperException
	{
		if (context.getTargetType().isArray())
		{
			context.setTargetType(context.getTargetType().getComponentType());

			List<Object> dataList = new ArrayList<Object>();

			for (Element element : elements)
			{
				context.setSourceElement(element);
				Object data = extractDataFromElement(context);

				if (data != null || context.getConfiguration().empty())
				{
					dataList.add(data);
				}
			}

			Object array = Array.newInstance(
					context.getTargetType(),
					dataList.size());
			for (int i = 0; i < dataList.size(); i++)
			{
				Array.set(array, i, dataList.get(i));
			}

			return array;
		}

		int resultIndex = context.getConfiguration().resultIndex();
		if (resultIndex == Scrape.DEFAULT_RESULT_UNBOXING)
		{
			if (elements.size() > 1)
			{
				throw new ScraperException(
						"CSS query '" + context.getConfiguration().value() + "' returned more than one elements."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			resultIndex = 0;
		}

		if (resultIndex >= elements.size())
		{
			if (context.getConfiguration().lenient())
			{
				return null;
			}
			throw new ScraperException(
					"CSS query '" + context.getConfiguration().value() + "' did not return an element at index " + resultIndex); //$NON-NLS-1$ //$NON-NLS-2$
		}

		context.setSourceElement(elements.get(resultIndex));
		return extractDataFromElement(context);
	}

	/** Extracts the data for one element returned by a CSS query. */
	private Object extractDataFromElement(AnnotatedScraperContext context)
			throws ScraperException
	{
		context.setSourceData(extractTextData(context));

		Scrape config = context.getConfiguration();
		if (!config.regex().isEmpty())
		{
			Pattern compile = Pattern.compile(config.regex());
			Matcher matcher = compile.matcher(context.getSourceData());
			if (matcher.matches())
			{
				context.setSourceData(matcher.replaceAll(config.replace()));
			}
		}

		if (config.trim())
		{
			context.setSourceData(context.getSourceData().trim());
		}

		if (context.getSourceData().isEmpty() && !config.empty())
		{
			return null;
		}

		validate(context);

		return convert(context);
	}

	/**
	 * Extracts the text data from the element depending of the configuration of
	 * the {@link Scrape} annotation.
	 */
	private String extractTextData(AnnotatedScraperContext context)
	{
		String attribute = context.getConfiguration().attribute();
		if (attribute.isEmpty())
		{
			return context.getSourceElement().text();
		}
		return context.getSourceElement().attr(attribute);
	}

	/** Sets the specified value to the field of the target object. */
	private void setFieldData(final Field field, Object data)
			throws ScraperException
	{
		try
		{
			field.set(this.target, data);
		}
		catch (IllegalArgumentException e)
		{
			throw new ScraperException("Cannot assign value to field '" //$NON-NLS-1$
					+ field.getName() + "'", e); //$NON-NLS-1$
		}
		catch (IllegalAccessException e)
		{
			Assert.notCaught(e, "just accessible fields are passed"); //$NON-NLS-1$
		}
	}

	/**
	 * @return All accessible (public & non-final) fields of the class and super
	 *         classes having the {@link Scrape} annotation.
	 */
	private static List<Field> getAcessibleAnnotatedFields(Object object)
	{
		Class<?> clazz = object.getClass();
		List<Field> fields = new ArrayList<Field>();
		for (Field field : clazz.getFields())
		{
			int modifiers = field.getModifiers();
			if (Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers)
					&& field.isAnnotationPresent(Scrape.class))
			{
				fields.add(field);
			}
		}
		return fields;
	}

	/** Validates the data with the specified validator. */
	private void validate(AnnotatedScraperContext context)
			throws ScraperValidationException, ScraperException
	{
		try
		{
			Validator validator = context.getConfiguration().validator().newInstance();
			validator.validate(context);
		}
		catch (InstantiationException e)
		{
			throw new ScraperException("Could not instantiate validator.", e); //$NON-NLS-1$
		}
		catch (IllegalAccessException e)
		{
			throw new ScraperException("Could not access validator.", e); //$NON-NLS-1$
		}
	}

	/** Converts the data with the specified converter. */
	private Object convert(AnnotatedScraperContext context)
			throws ScraperException
	{
		try
		{
			Converter convertor = context.getConfiguration().convertor().newInstance();
			return convertor.convert(context);
		}
		catch (InstantiationException e)
		{
			throw new ScraperException("Could not instantiate convertor.", e); //$NON-NLS-1$
		}
		catch (IllegalAccessException e)
		{
			throw new ScraperException("Could not access convertor.", e); //$NON-NLS-1$
		}
	}
}
