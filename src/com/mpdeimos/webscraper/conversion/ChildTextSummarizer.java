package com.mpdeimos.webscraper.conversion;

import com.mpdeimos.webscraper.ScraperContext;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.util.Strings;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;

import org.jsoup.nodes.Element;

/**
 * Converter that summarizes the text of all (direct) child elements, with
 * {@link Option} for filtering.
 * 
 * @author mpdeimos
 */
public class ChildTextSummarizer implements Converter
{
	/** Option annotation for {@link ChildTextSummarizer}. */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Option
	{
		/** Tags to include for summarizing. Default all. */
		public String[] include() default {};

		/** Tags to exclude for summarizing. Default none. */
		public String[] exclude() default {};
	}

	/** {@inheritDoc} */
	@Override
	public String convert(ScraperContext context) throws ScraperException
	{
		HashSet<String> include = new HashSet<String>();
		HashSet<String> exclude = new HashSet<String>();
		if (context.getTargetField().isAnnotationPresent(Option.class))
		{
			Option option = context.getTargetField().getAnnotation(
					Option.class);
			include.addAll(Arrays.asList(option.include()));
			exclude.addAll(Arrays.asList(option.exclude()));
		}

		StringBuilder content = new StringBuilder();
		for (Element element : context.getSourceElement().children())
		{
			if (isFiltered(element, include, exclude))
			{
				continue;
			}

			String text = element.text();
			if (!text.isEmpty())
			{
				if (content.length() != 0)
				{
					content.append(element.isBlock() ? Strings.NEW_LINE
							: Strings.EMPTY);
				}
				content.append(element.text());
			}
		}
		return content.toString();
	}

	/**
	 * @returns <code>true</code> if the element should be filtered, i.e. its
	 *          text will not be appended to the result.
	 */
	private boolean isFiltered(
			Element element,
			HashSet<String> include,
			HashSet<String> exclude)
	{
		if (!include.isEmpty() && !include.contains(element.tagName()))
		{
			return true;
		}

		return exclude.contains(element.tagName());
	}
}
