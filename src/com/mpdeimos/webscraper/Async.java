package com.mpdeimos.webscraper;

import com.mpdeimos.webscraper.conversion.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates that the annotated type or field and should
 * be computed asynchronously.
 * <p>
 * This annotation is valid on:
 * <ul>
 * <li>fields annotated with {@link Scrape}</li>
 * <li>classes implementing {@link Converter}</li>
 * </ul>
 * 
 * @author mpdeimos
 */
@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Async
{
	// This is a marker annotation
}
