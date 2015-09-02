package fr.vergne.translation.util.impl;

import java.util.HashMap;
import java.util.Map;

import fr.vergne.translation.util.Setting;

public class InternalSetting<ID> implements Setting<ID> {

	private final Map<ID, SettingKey<?>> idKeys = new HashMap<>();
	private final Map<SettingKey<?>, ID> keyIds = new HashMap<>();
	private final Map<SettingKey<?>, Object> values = new HashMap<>();

	@Override
	public <T> SettingKey<T> registerKey(final ID id, T defaultValue) {
		if (idKeys.containsKey(id)) {
			throw new IllegalArgumentException("Key already registered: " + id);
		} else {
			SettingKey<T> key = new SettingKey<T>() {
				@Override
				public String toString() {
					return id.toString();
				}
			};
			idKeys.put(id, key);
			keyIds.put(key, id);
			values.put(key, defaultValue);
			return key;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> SettingKey<T> getKey(ID id) {
		SettingKey<?> key = idKeys.get(id);
		if (key == null) {
			throw new IllegalArgumentException("Unknown key: " + id);
		} else {
			return (SettingKey<T>) key;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(SettingKey<T> key) {
		if (values.containsKey(key)) {
			return (T) values.get(key);
		} else {
			throw new IllegalArgumentException("Unknown key: " + key);
		}
	}

	@Override
	public <T> void set(SettingKey<T> key, T newValue) {
		if (values.containsKey(key)) {
			values.put(key, newValue);
		} else {
			throw new IllegalArgumentException("Unknown key: " + key);
		}
	}

}
