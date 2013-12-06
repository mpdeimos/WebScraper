package com.mpdeimos.webscraper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mpdeimos.webscraper.util.Strings;
/**
 * Annotation for fields that are mapped to an element in the html document.
 * The annotated fields have to be accessible, i.e. public and not final.
 * 
 * The default mapping source of the element is its node text. Alternatively also XML attributes may be used by specifying the "attribute" field.
 * 
 * @author mpdeimos
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scrape
{
	/** The CSS query to the element that is used as data source. */
	String value();

	/** The attribute to map. Default is to use the text of the element (specify empty string). */
	String attribute() default Strings.EMPTY;

	/** Regular expression that is used for matching the extracted value. Default is empty, i.e. no regular expression matching. */
	String regex() default Strings.EMPTY;

	/** The replacement for the matched regular expression. Default is the first matching group. Has no effect if no regular expression is specified. */  
	String replace() default "$1";

	/** Flag specifying if the extracted string should be trimmed. Default <code>true</code>. */
	boolean trim() default true;
}
