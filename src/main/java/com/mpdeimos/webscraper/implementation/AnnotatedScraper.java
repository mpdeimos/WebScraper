package com.mpdeimos.webscraper.implementation;

import com.mpdeimos.webscraper.Scrape;
import com.mpdeimos.webscraper.Scraper;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.ScraperSource;
import com.mpdeimos.webscraper.ScraperSource.ScraperSourceProvider;
import com.mpdeimos.webscraper.conversion.Converter;
import com.mpdeimos.webscraper.selection.Selector;
import com.mpdeimos.webscraper.util.Assert;
import com.mpdeimos.webscraper.util.Strings;
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
public class AnnotatedScraper extends Scraper
{
	/** The source for data binding. */
	private final ScraperSource source;

	/**
	 * The annotated target element to receive data from the source element.
	 */
	private final Object target;

	/** Constructor. */
	public AnnotatedScraper(ScraperSource sourc, Object object)
	{
		this.source = sourc;
		this.target = object;
	}

	/** {@inheritDoc} */
	@Override
	public void scrape() throws ScraperException
	{
		final Element root = this.source.getElement();

		for (final Field field : getAcessibleAnnotatedFields(this.target))
		{
			AnnotatedScraperContext context = new AnnotatedScraperContext(
					field);
			scrapeField(context, root);
		}
	}

	/**
	 * Scrapes the data from the document and assigns it to the target field.
	 */
	private void scrapeField(AnnotatedScraperContext context, Element root)
			throws ScraperException
	{
		updateRootElement(context, root);
		Elements elements = context.getRootElement().select(
				context.getConfiguration().value());

		final Object data = extractDataFromElements(context, elements);

		if (data != null)
		{
			setFieldData(context.getTargetField(), data);
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
						"CSS query '" + context.getConfiguration().value() //$NON-NLS-1$
								+ "' returned more than one elements."); //$NON-NLS-1$
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
					"CSS query '" + context.getConfiguration().value() //$NON-NLS-1$
							+ "' did not return an element at index " //$NON-NLS-1$
							+ resultIndex + " on document " //$NON-NLS-1$
							+ context.getRootElement().ownerDocument().baseUri());
		}

		context.setSourceElement(elements.get(resultIndex));
		return extractDataFromElement(context);
	}

	/** Extracts the data for one element returned by a CSS query. */
	private Object extractDataFromElement(AnnotatedScraperContext context)
			throws ScraperException
	{
		context.setSourceText(extractTextData(context));

		Scrape config = context.getConfiguration();
		if (!config.regex().isEmpty())
		{
			Pattern compile = Pattern.compile(config.regex());
			Matcher matcher = compile.matcher(context.getSourceText());
			if (matcher.matches())
			{
				context.setSourceText(matcher.replaceAll(config.replace()));
			}
		}

		if (config.trim())
		{
			context.setSourceText(context.getSourceText().replace(
					Strings.NONBREAKING_SPACE,
					Strings.SPACE).replaceAll("\\s+", Strings.SPACE).trim()); //$NON-NLS-1$
		}

		if (context.getSourceText().isEmpty() && !config.empty())
		{
			return null;
		}

		validate(context);

		Object converted = convert(context);
		if (converted != null && converted instanceof ScraperSourceProvider)
		{
			Scraper.builder().add(
					(ScraperSourceProvider) converted).build().scrape();
		}
		return converted;
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
			if (context.getConfiguration().ownText())
			{
				return context.getSourceElement().ownText();
			}
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

	/** Gets the root element specified in the configuration. */
	private void updateRootElement(
			AnnotatedScraperContext context,
			Element root)
			throws ScraperException
	{
		try
		{
			context.setRootElement(root);
			Selector selector = context.getConfiguration().root().newInstance();
			context.setRootElement(selector.select(context));
		}
		catch (InstantiationException e)
		{
			throw new ScraperException("Could not instantiate selector.", e); //$NON-NLS-1$
		}
		catch (IllegalAccessException e)
		{
			throw new ScraperException("Could not access selector.", e); //$NON-NLS-1$
		}
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
			Converter convertor = context.getConfiguration().converter().newInstance();
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
