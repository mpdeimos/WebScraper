package com.mpdeimos.webscraper.scraper;

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
			ScraperContext context = new ScraperContext();
			context.scrape = field.getAnnotation(Scrape.class);
			context.targetField = field;
			context.targetType = field.getType();

			Elements elements = this.source.select(context.scrape.value());

			Object data = extractDataFromElements(context, elements);

			if (data != null || context.scrape.empty())
			{
				setFieldData(field, data);
			}
		}
	}

	/** Extracts the data for a list of elements returned by a CSS query. */
	private Object extractDataFromElements(
			ScraperContext context,
			Elements elements)
			throws ScraperException
	{
		if (context.targetType.isArray())
		{
			context.targetType = context.targetType.getComponentType();

			List<Object> dataList = new ArrayList<Object>();

			for (Element element : elements)
			{
				context.sourceElement = element;
				Object data = extractDataFromElement(context);

				if (data != null || context.scrape.empty())
				{
					dataList.add(data);
				}
			}

			Object array = Array.newInstance(
					context.targetType,
					dataList.size());
			for (int i = 0; i < dataList.size(); i++)
			{
				Array.set(array, i, dataList.get(i));
			}

			return array;
		}

		int resultIndex = context.scrape.resultIndex();
		if (resultIndex == Scrape.DEFAULT_RESULT_UNBOXING)
		{
			if (elements.size() > 1)
			{
				throw new ScraperException(
						"CSS query '" + context.scrape.value() + "' returned more than one elements."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			resultIndex = 0;
		}

		if (resultIndex >= elements.size())
		{
			if (context.scrape.lenient())
			{
				return null;
			}
			throw new ScraperException(
					"CSS query '" + context.scrape.value() + "' did not return a element at index " + resultIndex); //$NON-NLS-1$ //$NON-NLS-2$
		}

		context.sourceElement = elements.get(resultIndex);
		return extractDataFromElement(context);
	}

	/** Extracts the data for one element returned by a CSS query. */
	private Object extractDataFromElement(ScraperContext context)
			throws ScraperException
	{
		context.sourceData = extractTextData(context);

		if (!context.scrape.regex().isEmpty())
		{
			Pattern compile = Pattern.compile(context.scrape.regex());
			Matcher matcher = compile.matcher(context.sourceData);
			if (matcher.matches())
			{
				context.sourceData = matcher.replaceAll(context.scrape.replace());
			}
		}

		if (context.scrape.trim())
		{
			context.sourceData = context.sourceData.trim();
		}

		if (context.sourceData.isEmpty() && !context.scrape.empty())
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
	private String extractTextData(ScraperContext context)
	{
		if (context.scrape.attribute().isEmpty())
		{
			return context.sourceElement.text();
		}
		return context.sourceElement.attr(context.scrape.attribute());
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
	private void validate(ScraperContext context)
			throws ScraperValidationException, ScraperException
	{
		try
		{
			Validator validator = context.scrape.validator().newInstance();
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
	private Object convert(ScraperContext context) throws ScraperException
	{
		try
		{
			Converter convertor = context.scrape.convertor().newInstance();
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
