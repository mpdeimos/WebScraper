package com.mpdeimos.webscraper;

import com.mpdeimos.webscraper.conversion.Converter;
import com.mpdeimos.webscraper.conversion.DefaultConverter;
import com.mpdeimos.webscraper.util.Strings;
import com.mpdeimos.webscraper.validation.DefaultValidator;
import com.mpdeimos.webscraper.validation.Validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Annotation for fields that are mapped to an element in the HTML document. The
 * annotated fields have to be accessible, i.e. public and not final.
 * 
 * The default mapping source of the element is its node text. Alternatively
 * also XML attributes may be used by specifying the {@link #attribute()} field.
 * 
 * @author mpdeimos
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scrape
{
	/**
	 * The default unboxing strategy for selecting an element from the queried
	 * HTML elements.
	 */
	/* package */static int DEFAULT_RESULT_UNBOXING = Integer.MIN_VALUE;

	/**
	 * The CSS query to the element that is used for determining the data
	 * source.
	 */
	String value();

	/**
	 * The specified CSS query may return more than one object. If the annotated
	 * field is a single value (i.e. no {@link Collection}), the first matching
	 * element is used and an {@link ScraperException} is thrown if multiple or
	 * no element is found.
	 * 
	 * Specifying this attribute will allow picking one of multiple found
	 * elements.
	 * 
	 * For annotated {@link Collection} fields, always all found elements are
	 * stored. So specifying this attribute will throw an
	 * {@link IllegalArgumentException}.
	 */
	int resultIndex() default DEFAULT_RESULT_UNBOXING;

	/**
	 * Prevents throwing a {@link ScraperException} if the specified CSS query
	 * does not find any elements.
	 */
	boolean lenient() default false;

	/**
	 * The attribute that is used as data source. Default is to use the text of
	 * the element instead if this attribute is not specified.
	 */
	String attribute() default Strings.EMPTY;

	/**
	 * Regular expression that is used for matching the extracted value. Default
	 * is empty, i.e. no regular expression matching.
	 */
	String regex() default Strings.EMPTY;

	/**
	 * The replacement for the matched regular expression. Default is the first
	 * matching group. Has no effect if no regular expression is specified.
	 */
	String replace() default "$1";

	/**
	 * Flag specifying if the extracted string should be trimmed. Default
	 * <code>true</code>.
	 */
	boolean trim() default true;

	/**
	 * Flag specifying if (after regex replacement and trimming) the value
	 * should be assigned even if empty. Default <code>true</code>
	 */
	boolean empty() default true;

	/**
	 * The validation processor that is used for validating values. Default is
	 * to always pass.
	 */
	Class<? extends Validator> validator() default DefaultValidator.class;

	/**
	 * The conversion processor that is used to convert scraped text to data.
	 */
	Class<? extends Converter> convertor() default DefaultConverter.class;
}
