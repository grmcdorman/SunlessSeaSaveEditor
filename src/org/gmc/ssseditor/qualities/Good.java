package org.gmc.ssseditor.qualities;

import java.util.Map;

import org.gmc.ssseditor.JSONUtil;

public class Good extends QualityItem
{
	public static class GoodFactory implements IQualityFactory
	{
		@Override
		public QualityItem createItem(Map<String, Object> quality)
		{
			return Good.constructGoodFromQuality(quality);
		}

		@Override
		public boolean isA(Map<String, Object> quality)
		{
			return Good.isQualityAGood(quality);
		}

		@Override
		public String getQualityName()
		{
			return Good.categoryName;
		}
		
	}

	public static final String categoryName = "Goods";

	static
	{
		QualityItem.addFactory(Good.categoryName, new GoodFactory());
	}

	/**
	 * Good, as described by the EquippedPossession/AssociatedQuality node.
	 * 
	 * Note that Goods that are not assigned to slots apparently don't show up here.
	 * @param quality AssociatedQuality node.
	 */
	public Good(Map<String, Object> quality)
	{
		super(JSONUtil.getString(quality, "Name"), JSONUtil.getLong(quality, "Id"), JSONUtil.getString(quality, "AssignToSlot", "Name"), Good.categoryName);
		// Other nodes of interest:
		// "Tag": Usually where it can be equipped, e.g. "Equipment - Deck" or "Equipment - Bridge".
		//   Enhancements: Qualities enhanced (or degraded) by this Good, e.g. Mirrors will be
		//                 listed as "AssociatedQuality" { "Name": "Mirrors", Id 102895, "Tag": "Abilities" },
		//                 and "Level" is the enhancement level.
		// Some enhancements that are not basic qualities:
		//   "Name": "Engine Power", "Id": 109846, "Tag": "Ship Conditions" (also affected by Companions)
		// AssignToSlot probably varies, usually "Tag": "Ship Equipment Slot". Known values:
		//   "Name": "Deck", "Id": 102966 (usually guns)
		//   "Name": "Bridge", "Id": 102964 (usually lights)
		//   "Name": "Engines", "Id": 102904 (usually engines!)
		// It is not apparent that there is a field that identifies type, such as guns.
	}

	/**
	 * Create a Good from a name and ID.
	 * @param name Good name.
	 * @param id Good ID.
	 */
	public Good(String name, long id)
	{
		super(name, id, null, Good.categoryName);
	}

	/**
	 * Determine if the given qualityNode is a Good, and hence can be passed to the {@link #Good(Map)} constructor.
	 * @param qualityNode - Quality node to test.
	 * @return true if the node is a Good.
	 */
	public static boolean isQualityAGood(Object qualityNode)
	{
		if (qualityNode != null && qualityNode instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> quality = (Map<String, Object>) qualityNode;
			String category = JSONUtil.getString(quality, "Category");
			return Good.categoryName.equals(category);
		}
		
		return false;
	}
	
	/**
	 * Determine if the given Possession node is a Good (i.e. contains a Good in the AssociatedQuality node).
	 * @param possessionNode - Possession node to test.
	 * @return true if the node is a Good.
	 */
	public static boolean isPossessionAGood(Object possessionNode)
	{
		if (possessionNode != null && possessionNode instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> possession = (Map<String, Object>) possessionNode;
			return Good.isQualityAGood(possession.get("AssociatedQuality"));
		}

		return false;
	}


	/**
	 * Construct a Good from the given Possession node. 
	 * @param possessionNode - Possession node.
	 * @return New Good, or null if the node is not a Good node.
	 */
	@SuppressWarnings("unchecked")
	static Good constructGoodFromPosession(Object possessionNode)
	{
		if (Good.isPossessionAGood(possessionNode))
		{
			Map<String, Object> possession = (Map<String, Object>) possessionNode;
			return new Good((Map<String, Object>) possession.get("AssociatedQuality"));
		}
		
		return null;
	}

	/**
	 * Construct a Good from the given Quality node. 
	 * @param qualityNode - Quality node.
	 * @return New Good, or null if the node is not a Good node.
	 */
	@SuppressWarnings("unchecked")
	static Good constructGoodFromQuality(Object qualityNode)
	{
		if (Good.isQualityAGood(qualityNode))
		{
			return new Good((Map<String, Object>) qualityNode);
		}
		
		return null;
	}

	@Override
	public boolean isCargo()
	{
		return true;
	}
}
