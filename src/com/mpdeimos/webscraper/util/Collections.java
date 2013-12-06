package com.mpdeimos.webscraper.util;

import java.util.Collection;

/**
 * Utility functions for collections.
 * 
 * @author mpdeimos
 */
public class Collections
{
	/**
	 * Adds the given string in a trimmed fashion to the collection if it is not
	 * empty.
	 */
	public static void addIfNotEmpty(Collection<String> collection, String input)
	{
		input = input.trim();
		if (!input.isEmpty())
		{
			collection.add(input);
		}
	}
}
