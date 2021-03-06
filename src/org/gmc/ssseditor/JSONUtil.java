package org.gmc.ssseditor;

import java.util.ArrayList;
import java.util.Map;

public class JSONUtil {

	/**
	 * Get a key value from a JSON path.
	 *
	 * For example, given the JSON {"currentPort":{"Name":"FieldhavenPort"}}, the call
	 * getKeyValue(root, "currentPort", "name") would return "FieldhavenPort".
	 *
	 * @param json JSON root.
	 * @param keys Path to target key.
	 * @return Final key; null if not found.
	 */
	@SuppressWarnings("unchecked")
	public static Object getKeyValue(Map<String, Object> json, String... keys)
	{
		Object last = null;
		for (String key : keys) {
			if (json == null) {
				last = null;
				break;
			}
			last = json.get(key);
			if (last instanceof Map<?, ?>) {
				json = (Map<String, Object>) last;
			} else {
				json = null;
			}
		}
		
		return last;
	}

	/**
	 * Get a Long value from a key path.
	 * @param json JSON root.
	 * @param keys Path to target key.
	 * @return Long value; null if not found.
	 */
	public static Long getLong(Map<String, Object> json, String... keys)
	{
		Object value = JSONUtil.getKeyValue(json, keys);
		if (value != null && value instanceof Long) {
			return (Long) value;
		}
		
		return null;
	}

	/**
	 * Get a String value from a key path.
	 * @param json JSON root.
	 * @param keys Path to target key.
	 * @return String value; null if not found.
	 */
	public static String getString(Map<String, Object> json, String... keys)
	{
		Object value = JSONUtil.getKeyValue(json, keys);
		if (value != null && value instanceof String) {
			return (String) value;
		}
		
		return null;
	}
	
	/**
	 * Get an Object, as a Map, from a key path.
	 * @param json JSON root.
	 * @param keys Path to target key.
	 * @return Object; null if not found or result is not Map<String, Object>.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getObject(Map<String, Object> json, String... keys)
	{
		Object value = JSONUtil.getKeyValue(json, keys);
		if (value != null && value instanceof Map<?, ?>) {
			return (Map<String, Object>) value;
		}
		
		return null;
	}

	/**
	 * Get a ArrayList<Object> value from a key path.
	 * @param json JSON root.
	 * @param keys Path to target key.
	 * @return ArrayList; null if not found or result is not ArrayList<Object>.
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Object> getArray(Map<String, Object> json, String... keys)
	{
		Object value = JSONUtil.getKeyValue(json, keys);
		if (value != null && value instanceof ArrayList<?>) {
			return (ArrayList<Object>) value;
		}
		
		return null;
	}
}
