package com.mpdeimos.webscraper;

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
/* package */class AnnotatedScraper implements Scraper
{
	/** The html element used as source for data binding. */
	private final Element source;

	/**
	 * The annotated destination element to receive data from the source
	 * element.
	 */
	private final Object destination;

	/** Constructor. */
	public AnnotatedScraper(Element element, Object object)
	{
		this.source = element;
		this.destination = object;
	}

	/** {@inheritDoc} */
	@Override
	public void scrape() throws ScraperException
	{
		for (final Field field : getAcessibleAnnotatedFields(this.destination))
		{
			final Scrape scrape = field.getAnnotation(Scrape.class);

			Elements elements = this.source.select(scrape.value());

			Object data = extractDataFromElements(
					elements,
					field,
					scrape);

			if (data != null || scrape.empty())
			{
				setFieldData(field, data);
			}
		}
	}

	/** Extracts the data for a list of elements returned by a CSS query. */
	private Object extractDataFromElements(
			Elements elements,
			Field field,
			Scrape scrape) throws ScraperException
	{
		Class<?> fieldType = field.getType();
		if (fieldType.isArray())
		{
			Class<?> componentType = fieldType.getComponentType();

			List<Object> dataList = new ArrayList<Object>();

			for (Element element : elements)
			{
				Object data = extractDataFromElement(
						element,
						field,
						scrape, componentType);

				if (data != null || scrape.empty())
				{
					dataList.add(data);
				}
			}

			Object array = Array.newInstance(componentType, dataList.size());
			for (int i = 0; i < dataList.size(); i++)
			{
				Array.set(array, i, dataList.get(i));
			}

			return array;
		}

		int resultIndex = scrape.resultIndex();
		if (resultIndex == Scrape.DEFAULT_RESULT_UNBOXING)
		{
			if (elements.size() > 1)
			{
				throw new ScraperException(
						"CSS query '" + scrape.value() + "' returned more than one elements."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			resultIndex = 0;
		}

		if (resultIndex >= elements.size())
		{
			if (scrape.lenient())
			{
				return null;
			}
			throw new ScraperException(
					"CSS query '" + scrape.value() + "' did not return a element at index " + resultIndex); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return extractDataFromElement(
				elements.get(resultIndex),
				field,
				scrape,
				fieldType);
	}

	/** Extracts the data for one element returned by a CSS query. */
	private Object extractDataFromElement(
			Element element,
			Field field,
			Scrape scrape,
			Class<?> type) throws ScraperException
	{
		String textData = extractTextData(element, scrape);

		if (!scrape.regex().isEmpty())
		{
			Pattern compile = Pattern.compile(scrape.regex());
			Matcher matcher = compile.matcher(textData);
			if (matcher.matches())
			{
				textData = matcher.replaceAll(scrape.replace());
			}
		}

		if (scrape.trim())
		{
			textData = textData.trim();
		}

		if (textData.isEmpty() && !scrape.empty())
		{
			return null;
		}

		validate(field, scrape, type, textData);

		return convert(field, scrape, type, textData);
	}

	/**
	 * Extracts the text data from the element depending of the configuration of
	 * the {@link Scrape} annotation.
	 */
	private String extractTextData(Element element, Scrape scrape)
	{
		if (scrape.attribute().isEmpty())
		{
			return element.text();
		}
		return element.attr(scrape.attribute());
	}

	/** Sets the specified value to the field of the destination object. */
	private void setFieldData(final Field field, Object data)
			throws ScraperException
	{
		try
		{
			field.set(this.destination, data);
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
	private void validate(
			Field field,
			Scrape scrape,
			Class<?> type,
			String textData)
			throws ScraperValidationException, ScraperException
	{
		try
		{
			Validator validator = scrape.validator().newInstance();
			validator.validate(textData, type, field);
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
	private Object convert(
			Field field,
			Scrape scrape,
			Class<?> type,
			String textData) throws ScraperException
	{
		try
		{
			Converter convertor = scrape.convertor().newInstance();
			return convertor.convert(textData, type, field);
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
