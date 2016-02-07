package org.gmc.ssseditor.qualities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is an internal representation of the Quality as it exists in the save file or configuration file.
 * @author grant
 *
 */
public class QualityItem
{
	/**
	 * Factories to create new QualityItem objects for each category.
	 */
	private static Map<String, IQualityFactory> qualityFactory = new HashMap<String, IQualityFactory>();

	private final long tag;
	private final String name;
	private final String slot;
	private final String category;
	
	private Map<String, Object> quality;

	/**
	 * All items known, by ID.
	 */
	static Map<Long, QualityItem> allItems = new HashMap<Long, QualityItem>();

	/**
	 * Basic constructor for a QualityItem, with just a name and tag. This QualityItem will not have a Category.
	 * @param name QualityItem name.
	 * @param id QualityItem ID.
	 */
	public QualityItem(String name, long id)
	{
		this(name, id, null, "");
	}

	/**
	 * Construct a QualityItem.
	 * @param name QualityItem name; used on the UI.
	 * @param id "Id" field in the JSON.
	 * @param slot Assigned slot. May be null.
	 * @param category QualityItem's category.
	 */
	public QualityItem(String name, Long id, String slot, String category)
	{
		this.tag = id;
		this.name = name;
		this.slot = slot;
		this.category = category;
		
		QualityItem.allItems.put(id, this);
	}

	/**
	 * Get the tag; the integer ID used for this InventoryItem (or Quality) in the save file.
	 * @return Tag value.
	 */
	public long getTag()
	{
		return tag;
	}

	/**
	 * Return a value indicating whether this InventoryItem takes cargo space.
	 * @return true if the item takes cargo space.
	 */
	public boolean isCargo()
	{
		return false;
	}

	/**
	 * Return the user-friendly name of the InventoryItem or Quality.
	 * @return Name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Category string. May be blank for pre-defined items.
	 * @return Category.
	 */
	public String getCategory()
	{
		return this.category;
	}

	/**
	 * Get an inventory item by tag, if known.
	 * @param tag Item tag.
	 * @return Inventory item, or null if not known.
	 */
	public static QualityItem getKnownItem(int tag)
	{
		return QualityItem.allItems.get(tag);
	}

	/**
	 * Return the set of known tags.
	 * @return Tag set.
	 */
	public static Set<Long> getKnownItemTags()
	{
		return QualityItem.allItems.keySet();
	}

	public static Set<QualityItem> getItemsInCategory(String category)
	{
		Set<QualityItem> result = new TreeSet<QualityItem>(new Comparator<QualityItem>() {
			@Override
			public int compare(QualityItem o1, QualityItem o2)
			{
				return o1.name.compareTo(o2.name);
			}
		});

		for (QualityItem item : QualityItem.allItems.values()) {
			if (item.getCategory().equals(category)) {
				result.add(item);
			}
		}

		return result;
	}
	/**
	 * Return the set of known categories, sorted by category.
	 * @return Category set.
	 */
	public static Set<String> getKnownCategories()
	{
		return new TreeSet<String>(qualityFactory.keySet());
	}

	/**
	 * Return the factory for the given category.
	 * @param category Category name.
	 * @return Factory; null if not known.
	 */
	public static IQualityFactory getFactory(String category)
	{
		return qualityFactory.get(category);
	}
	
	@Override
	public String toString()
	{
		return this.getTag() + ": " + this.getName();
	}

	/**
	 * Get the slot this Companion is assigned to.
	 * @return Slot. May be null if not known.
	 */
	public String getSlot() {
		return this.slot;
	}

	/**
	 * Get an empty object suitable for insertion into the save file Qualities list.
	 * @param associatedQualityId ID of associated quality.
	 */
	public Map<String, Object> getTemplateObject()
	{
		// This list is copied from the save file.
		Map<String, Object> quality = new HashMap<String, Object>();
		quality.put("AssociatedQuality", null);
		quality.put("EffectiveLevelModifier", new Long(0));
		quality.put("QualityName", null);
		quality.put("TargetLevel", null);
		quality.put("AssociatedQualityId", new Long(this.getTag()));
		quality.put("TargetQuality", null);
		quality.put("Name", null);
		quality.put("QualityDescription", null);
		quality.put("QualityAllowedOn", null);
		quality.put("Relationships", new ArrayList<Object>());
		quality.put("QualityImage", null);
		quality.put("EquippedPossession", null);
		quality.put("QualityCategory", null);
		quality.put("XP", new Long(0));
		quality.put("Level", new Long(0));
		quality.put("QualityNature", null);
		quality.put("Id", new Long(0));
		quality.put("CompletionMessage", null);

		return quality;
	}

	public Map<String, Object> getQuality()
	{
		return quality;
	}

	protected void setQuality(Map<String, Object> quality)
	{
		this.quality = quality;
	}

	/**
	 * Add a Quality factory for a specific category of QualityItem objects.
	 * @param category Category name, such as Ship.
	 * @param factory Factory that will create instances of these QualityItem objects.
	 */
	protected static void addFactory(String category, IQualityFactory factory)
	{
		QualityItem.qualityFactory.put(category, factory);
	}
}
