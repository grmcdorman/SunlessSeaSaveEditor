package org.gmc.ssseditor.qualities;

import java.util.Map;

import org.gmc.ssseditor.JSONUtil;

/**
 * This class contains support for a generic QualityItem factory. Most QualityItem categories can be
 * made using this factory; a specific factory is only needed if the QualityItem should be a derived class
 * (see, for example, Ship).
 */
class GenericItemFactory implements IQualityFactory
{
	private final String category;

	public GenericItemFactory(String category)
	{
		this.category = category;
	}

	@Override
	public QualityItem createItem(Map<String, Object> quality)
	{
		return new QualityItem(JSONUtil.getString(quality, "Name"), JSONUtil.getLong(quality, "Id"), JSONUtil.getString(quality, "AssignToSlot", "Name"), this.category);
	}

	@Override
	public boolean isA(Map<String, Object> quality)
	{
		if (quality != null) {
			String category = JSONUtil.getString(quality, "Category");
			return this.category.equals(category);
		}
		return false;
	}

	@Override
	public String getQualityName()
	{
		return this.category;
	}
}