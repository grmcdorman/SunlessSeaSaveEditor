package org.gmc.ssseditor.qualities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gmc.ssseditor.JSONUtil;

/**
 * This object contains a list of Qualities.
 * 
 * This list can be from a save file, which contains limited information, or from the 'qualties.json' in the 'entities' directory.
 *
 * The QualityItem factory is used to create the list of QualityItems; categories that are not known are added to the factory.
 * @author grant
 *
 */
public class QualitiesList
{
	Map<String, LinkedList<QualityItem>> loadedQualities = new HashMap<String, LinkedList<QualityItem>>();

	ArrayList<Object> list;

	/**
	 * Create a new QualitiesList from the supplied qualities list. 
	 * @param jsonQualities List of qualities (either save file or configuration file).
	 */
	@SuppressWarnings("unchecked")
	public QualitiesList(ArrayList<Object> jsonQualities, boolean isConfigurationFile)
	{
		this.list = jsonQualities;
		for (Object qualityObject: jsonQualities)
		{
			Map<String, Object> quality = null;
			if (qualityObject instanceof Map<?, ?>) {
				quality = (Map<String, Object>) qualityObject;
				if (!isConfigurationFile) {
					// Save file. Look for EquippedPossession/AssociatedQuality. quality.get("EquippedPossession")
					quality = JSONUtil.getObject(quality, "EquippedPossession", "AssociatedQuality");
				}
				if (quality != null) {
					this.load(quality);
				}
			}
		}
	}

	/**
	 * Get all QualityItems of the given category.
	 * @param category Category name, e.g. Ship.categoryName
	 * @return List of items; null if no such quality has been seen.
	 */
	public List<QualityItem> GetQualities(String category)
	{
		return this.loadedQualities.get(category);
	}

	/**
	 * Add a new quality to the JSON list and the loaded qualities list.
	 * @param newQuality New quality.
	 */
	public void addQuality(Map<String, Object> newQuality)
	{
		this.list.add(newQuality);
		this.load(newQuality);
	}

	public void removeQuality(Map<String, Object> quality)
	{
		this.list.remove(quality);
		// This does not remove the QualityItem from the loadedQualties list.
	}

	/**
	 * Load one quality.
	 * @param qualityItem Quality item to load.
	 */
	private void load(Map<String, Object> qualityItem)
	{
		String category = JSONUtil.getString(qualityItem, "Category");
		if (category != null) {
			IQualityFactory factory = QualityItem.getFactory(category);
			if (factory == null) {
				factory = new GenericItemFactory(category);
				QualityItem.addFactory(category, factory);
			}

			LinkedList<QualityItem> list = this.loadedQualities.get(category);
			if (list == null) {
				list = new LinkedList<QualityItem>();
				this.loadedQualities.put(factory.getQualityName(), list);
			}
			list.add(factory.createItem(qualityItem));
		}
	}
}
