package org.gmc.ssseditor.qualities;

import java.util.Map;

public interface IQualityFactory
{
	public QualityItem createItem(Map<String, Object> quality);
	public boolean isA(Map<String, Object> quality);
	public String getQualityName();
}
