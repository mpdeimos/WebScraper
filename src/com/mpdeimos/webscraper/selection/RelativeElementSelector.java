package com.mpdeimos.webscraper.selection;

import com.mpdeimos.webscraper.ScraperContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jsoup.nodes.Element;

/**
 * Selector for relatively selecting an element. Configuration is done with
 * {@link Option}.
 * 
 * @author mpdeimos
 */
public class RelativeElementSelector implements Selector
{
	/** Option annotation for {@link RelativeElementSelector}. */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Option
	{
		/**
		 * The relative parent to select. A positive integer describing the n-th
		 * parent.
		 */
		public int parent() default 0;

		/**
		 * The relative sibling to select. Positive integer for succeeding
		 * sibling, negative integer for preceding sibling.
		 */
		public int sibling() default 0;
	}

	/** {@inheritDoc} */
	@Override
	public Element select(ScraperContext context)
	{
		Element root = context.getRootElement();

		Option options = context.getTargetField().getAnnotation(Option.class);
		if (options == null)
		{
			return root;
		}

		root = advanceToParent(root, options);
		root = advanceToSibling(root, options);

		return root;
	}

	/** Advances to the specified parent. */
	private Element advanceToParent(Element root, Option options)
	{
		for (int i = 0; i < options.parent(); i++)
		{
			Element newRoot = root.parent();

			if (newRoot == null)
			{
				break;
			}

			root = newRoot;
		}
		return root;
	}

	/** Advances to the specified sibling. */
	private Element advanceToSibling(Element root, Option options)
	{
		for (int i = 0; i < Math.abs(options.sibling()); i++)
		{
			Element newRoot = null;

			if (options.sibling() > 0)
			{
				newRoot = root.nextElementSibling();
			}
			else
			{
				newRoot = root.previousElementSibling();
			}

			if (newRoot == null)
			{
				break;
			}

			root = newRoot;
		}
		return root;
	}
}
