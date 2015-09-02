package fr.vergne.translation.util.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import fr.vergne.translation.util.Setting;
import fr.vergne.translation.util.Switcher;

public class PropertyFileSetting implements Setting<String> {

	private final FileBasedProperties settings;
	private final Map<String, SettingKey<?>> idKeys = new HashMap<>();
	private final Map<SettingKey<?>, String> keyIds = new HashMap<>();
	private final Map<SettingKey<?>, Object> defaultValues = new HashMap<>();
	private final Map<Class<?>, Switcher<String, ?>> classSwitchers = new HashMap<>();
	private final Map<SettingKey<?>, Switcher<String, ?>> keySwitchers = new HashMap<>();
	private final Map<String, Switcher<String, ?>> futureSwitchers = new HashMap<>();

	public PropertyFileSetting(File file) {
		settings = new FileBasedProperties(file, true);
	}

	@Override
	public <T> SettingKey<T> registerKey(final String id, T defaultValue) {
		if (idKeys.containsKey(id)) {
			throw new IllegalArgumentException("Key already registered: " + id);
		} else {
			SettingKey<T> key = new SettingKey<T>() {
				@Override
				public String toString() {
					return id;
				}
			};
			idKeys.put(id, key);
			keyIds.put(key, id);
			defaultValues.put(key, defaultValue);

			Switcher<String, ?> switcher = futureSwitchers.remove(id);
			if (switcher != null) {
				keySwitchers.put(key, switcher);
			} else {
				// no switcher set up
			}

			return key;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> SettingKey<T> getKey(String id) {
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
		if (keyIds.containsKey(key)) {
			String stringValue = settings.getProperty(keyIds.get(key));
			if (stringValue == null) {
				return (T) defaultValues.get(key);
			} else {
				Switcher<String, T> switcher = getSwitcher(key,
						(T) defaultValues.get(key));
				return switcher.switchForth(stringValue);
			}
		} else {
			throw new IllegalArgumentException("Unknown key: " + key);
		}
	}

	public <T> void setFutureSwitcher(String id, Switcher<String, T> switcher) {
		futureSwitchers.put(id, switcher);
	}

	@SuppressWarnings("unchecked")
	private <T> Switcher<String, T> getSwitcher(SettingKey<T> key,
			T referenceValue) {
		Switcher<String, ?> switcher = keySwitchers.get(key);
		if (switcher != null) {
			// reuse
		} else {
			if (referenceValue != null) {
				Class<? extends Object> valueClass = referenceValue.getClass();
				switcher = classSwitchers.get(valueClass);
				if (switcher != null) {
					// reuse
				} else {
					switcher = new SmartStringSwitcher<>(valueClass);
					classSwitchers.put(valueClass, switcher);
				}
			} else {
				Object defaultValue = defaultValues.get(key);
				if (defaultValue != null) {
					Class<?> valueClass = defaultValue.getClass();
					switcher = classSwitchers.get(valueClass);
					if (switcher != null) {
						// reuse
					} else {
						switcher = new SmartStringSwitcher<>(valueClass);
						classSwitchers.put(valueClass, switcher);
					}
				} else {
					throw new NoSuchElementException("Lack a switcher for key "
							+ key);
				}
			}
		}
		return (Switcher<String, T>) switcher;
	}

	@Override
	public <T> void set(SettingKey<T> key, T newValue) {
		if (keyIds.containsKey(key)) {
			Switcher<String, T> switcher = getSwitcher(key, newValue);
			settings.setProperty(keyIds.get(key), switcher.switchBack(newValue));
		} else {
			throw new IllegalArgumentException("Unknown key: " + key);
		}
	}

}
