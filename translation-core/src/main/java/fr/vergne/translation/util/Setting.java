package fr.vergne.translation.util;

public interface Setting<ID> {

	public <T> SettingKey<T> registerKey(ID id, T defaultValue);

	public <T> SettingKey<T> getKey(ID id);

	public <T> T get(SettingKey<T> key);

	public <T> void set(SettingKey<T> key, T newValue);

	public class SettingKey<T> {
	}
}
