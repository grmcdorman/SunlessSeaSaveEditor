package org.gmc.ssseditor.qualities;

import java.util.Map;

import org.gmc.ssseditor.JSONUtil;

/**
 * Companion: a Quality that is a companion such as an officer or mascot.
 * @author grant
 *
 */
public class Companion extends QualityItem
{
	public static class CompanionFactory implements IQualityFactory
	{
		@Override
		public QualityItem createItem(Map<String, Object> quality)
		{
			return Companion.constructCompanionFromQuality(quality);
		}

		@Override
		public boolean isA(Map<String, Object> quality)
		{
			return Companion.isQualityACompanion(quality);
		}

		@Override
		public String getQualityName()
		{
			return Companion.categoryName;
		}
	}

	public static final String categoryName = "Companion";

	static
	{
		QualityItem.addFactory(Companion.categoryName, new CompanionFactory());
	}

	/**
	 * Companion, as described by the EquippedPossession/AssociatedQuality node.
	 * 
	 * Note that Companions that are not assigned to slots apparently don't show up here.
	 * @param quality AssociatedQuality node.
	 */
	public Companion(Map<String, Object> quality)
	{
		super(JSONUtil.getString(quality, "Name"), JSONUtil.getLong(quality, "Id"), JSONUtil.getString(quality, "AssignToSlot", "Name"), Companion.categoryName);
		// Other nodes of interest:
		//   AvailableAt: Text, e.g. "Sometimes available for hire in London."
		//   Enhancements: Qualities enhanced by this Companion, e.g. Mirrors will be
		//                 listed as "AssociatedQuality" { "Name": "Mirrors", Id 102895, "Tag": "Abilities" },
		//                 and "Level" is the enhancement level.
		// Some enhancements that are not basic qualities:
		//   "Name": "Engine Power", "Id": 109846, "Tag": "Ship Conditions" (also affected by Goods/Equipment - Engines)
		//   "Name": "A Doctor Aboard", "Id": 116058, "Tag": "Ship Conditions"
		// There's quite a bit of data under "UseEvent", presumably describing events and capabilities.
		// AssignToSlot probably varies, usually "Tag": "Officer Roles". Known values:
		//   "Name": "Cook", "Id": 102771
		//   "Name": "First Officer", "Id": 102769
		//   "Name": "Chief Engineer", "Id": 102770
		//   "Name": "Gunnery Officer", "Id": 102775
		//   "Name": "Mascot", "Id": 102774
		//   "Name": "Surgeon", "Id": 102772
	}

	/**
	 * Determine if the given qualityNode is a companion, and hence can be passed to the {@link #Companion(Map)} constructor.
	 * @param qualityNode - Quality node to test.
	 * @return true if the node is a companion.
	 */
	public static boolean isQualityACompanion(Object qualityNode)
	{
		if (qualityNode != null && qualityNode instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> quality = (Map<String, Object>) qualityNode;
			String category = JSONUtil.getString(quality, "Category");
			return Companion.categoryName.equals(category);
		}
		
		return false;
	}
	
	/**
	 * Determine if the given Possession node is a Companion (i.e. contains a Companion in the AssociatedQuality node).
	 * @param possessionNode - Possession node to test.
	 * @return true if the node is a Companion.
	 */
	static boolean isPossessionACompanion(Object possessionNode)
	{
		if (possessionNode != null && possessionNode instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> possession = (Map<String, Object>) possessionNode;
			return Companion.isQualityACompanion(possession.get("AssociatedQuality"));
		}

		return false;
	}


	/**
	 * Construct a Companion from the given Possession node. 
	 * @param possessionNode - Possession node.
	 * @return New Companion, or null if the node is not a Companion node.
	 */
	@SuppressWarnings("unchecked")
	static QualityItem constructCompanionFromPosession(Object possessionNode)
	{
		if (Companion.isPossessionACompanion(possessionNode))
		{
			Map<String, Object> possession = (Map<String, Object>) possessionNode;
			return new Companion((Map<String, Object>) possession.get("AssociatedQuality"));
		}
		
		return null;
	}

	/**
	 * Construct a Companion from the given Quality node. 
	 * @param qualityNode - Quality node.
	 * @return New Companion, or null if the node is not a Companion node.
	 */
	@SuppressWarnings("unchecked")
	static QualityItem constructCompanionFromQuality(Object qualityNode)
	{
		if (Companion.isQualityACompanion(qualityNode))
		{
			return new Companion((Map<String, Object>) qualityNode);
		}
		
		return null;
	}
}
