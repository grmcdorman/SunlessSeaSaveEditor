package org.gmc.ssseditor.qualities;

import java.util.ArrayList;
import java.util.Map;

import org.gmc.ssseditor.JSONUtil;

/**
 * This class encapsulates Ship information from the configuration and save file.
 * @author grant
 *
 */
public class Ship extends QualityItem
{
	public static class ShipFactory implements IQualityFactory
	{

		@Override
		public QualityItem createItem(Map<String, Object> quality)
		{
			return Ship.constructShipFromQuality(quality);
		}

		@Override
		public boolean isA(Map<String, Object> quality)
		{
			return Ship.isQualityAShip(quality);
		}

		@Override
		public String getQualityName()
		{
			return Ship.categoryName;
		}
	}

	public static final String categoryName = "Ship";

	static
	{
		QualityItem.addFactory(Ship.categoryName, new ShipFactory());
	}

	private Map<String, Object> crewCapacity = null;
	private Map<String, Object> weight = null;
	private Map<String, Object> holdCapacity = null;
	private Map<String, Object> maxHull = null;
	/**
	 * Ship, as described by the EquippedPossession/AssociatedQuality node.
	 * @param quality AssociatedQuality node.
	 */
	@SuppressWarnings("unchecked")
	public Ship(Map<String, Object> quality)
	{
		// There are two AssociatedQualityId nodes: one at the top level, and one
		// within the EquippedPossession node. I suspect the former is the slot,
		// and the latter the actual entity.
		// AssignToSlot will have ID 102889? and Tag "Ship Equipment Slot".
		super(JSONUtil.getString(quality, "Name"), JSONUtil.getInteger(quality, "Id"), JSONUtil.getString(quality, "AssignToSlot", "Name"), Ship.categoryName);
		// Extract nodes of interest.
		//   AssignedToSlot: where equipped.
		//   Enhancements: Array. Known entries:
		//        4464 (AssociatedQuality: 106992, Name: "Quarters"): Quarters. Level defines max crew.
		//        4788 (AssociatedQuality: 109845, Name: "Ship Weight"): Ship weight. Level defines weight.
		//        4797 (AssociatedQuality: 105361, Name: "MaxHull"): Hull strength. Level defines max strength. 
		//        4798 (AssociatedQuality: 102031, Name: "Hold"): Hold. Level defines max capacity.
		ArrayList<Object> enhancements = JSONUtil.getArray(quality, "Enhancements");
		if (enhancements != null) {
			for (Object enhancementItem: enhancements) {
				if (enhancementItem instanceof Map<?, ?>) {
					Map<String, Object> enhancement = (Map<String, Object>) enhancementItem;
					String associatedName = JSONUtil.getString(enhancement, "AssociatedQuality", "Name");
					if (associatedName != null) {
						switch (associatedName) {
							case "Quarters":
							{
								this.crewCapacity = enhancement;
								break;
							}
							case "Ship Weight":
							{
								this.weight = enhancement;
								break;
							}
							case "MaxHull":
							{
								this.maxHull = enhancement;
								break; 
							}
							case "Hold":
							{
								this.holdCapacity = enhancement;
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Determine if the given qualityNode is a ship, and hence can be passed to the {@link #Ship(Map)} constructor.
	 * @param qualityNode - Quality node to test.
	 * @return true if the node is a ship.
	 */
	public static boolean isQualityAShip(Object qualityNode)
	{
		if (qualityNode != null && qualityNode instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> quality = (Map<String, Object>) qualityNode;
			String category = JSONUtil.getString(quality, "Category");
			return Ship.categoryName.equals(category);
		}
		
		return false;
	}
	
	/**
	 * Determine if the given Possession node is a ship (i.e. contains a Ship in the AssociatedQuality node).
	 * @param possessionNode - Possession node to test.
	 * @return true if the node is a ship.
	 */
	static boolean isPossessionAShip(Object possessionNode)
	{
		if (possessionNode != null && possessionNode instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> possession = (Map<String, Object>) possessionNode;
			return Ship.isQualityAShip(possession.get("AssociatedQuality"));
		}

		return false;
	}

	/**
	 * Construct a ship from the given Possession node. 
	 * @param possessionNode - Possession node.
	 * @return New ship, or null if the node is not a Ship node.
	 */
	@SuppressWarnings("unchecked")
	static Ship constructShipFromPosession(Object possessionNode)
	{
		if (Ship.isPossessionAShip(possessionNode))
		{
			Map<String, Object> possession = (Map<String, Object>) possessionNode;
			return new Ship((Map<String, Object>) possession.get("AssociatedQuality"));
		}
		
		return null;
	}

	/**
	 * Construct a ship from the given Quality node. 
	 * @param qualityNode - Quality node.
	 * @return New ship, or null if the node is not a Ship node.
	 */
	@SuppressWarnings("unchecked")
	static Ship constructShipFromQuality(Object qualityNode)
	{
		if (Ship.isQualityAShip(qualityNode))
		{
			return new Ship((Map<String, Object>) qualityNode);
		}
		
		return null;
	}

	/**
	 * Get the crew capacity. 0 if not known.
	 * @return Crew capacity.
	 */
	public int getCrewCapacity()
	{
		return this.crewCapacity != null ? JSONUtil.getInteger(this.crewCapacity, "Level") : 0;
	}

	/**
	 * Set the crew capacity.
	 * @param crewCapacity New crew capacity.
	 */
	public void setCrewCapacity(int crewCapacity)
	{
		if (this.crewCapacity != null) {
			this.crewCapacity.put("Level", new Long(crewCapacity));
		}
	}

	/**
	 * Get the weight. 0 if not known.
	 * @return  Weight.
	 */
	public int getWeight()
	{
		return this.weight != null ? JSONUtil.getInteger(this.weight, "Level") : 0;
	}

	/**
	 * Set the weight.
	 * @param weight New weight.
	 */
	public void setWeight(int weight)
	{
		if (this.weight != null) {
			this.weight.put("Level", new Long(weight));
		}
	}

	/**
	 * Get the hold capacity. 0 if not known.
	 * @return Hold capacity.
	 */
	public int getHoldCapacity()
	{
		return this.holdCapacity != null ? JSONUtil.getInteger(this.holdCapacity, "Level") : 0;
	}

	/**
	 * Set the hold capacity.
	 * @param holdCapacity New hold capacity.
	 */
	public void setHoldCapacity(int holdCapacity)
	{
		if (this.holdCapacity != null) {
			this.holdCapacity.put("Level", new Long(holdCapacity));
		}
	}

	/**
	 * Get the maximum hull value. 0 if not known.
	 * @return Maximum hull value.
	 */
	public int getMaxHull()
	{
		return this.maxHull != null ? JSONUtil.getInteger(this.maxHull, "Level") : 0;
	}

	/**
	 * Set the maximum hull value.
	 * @param maxHull New maximum hull value.
	 */
	public void setMaxHull(int maxHull)
	{
		if (this.maxHull != null) {
			this.maxHull.put("Level", new Long(maxHull));
		}
	}
}
