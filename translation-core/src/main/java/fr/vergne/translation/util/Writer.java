package fr.vergne.translation.util;

import fr.vergne.translation.impl.OnDemandEntry;
import fr.vergne.translation.impl.OnDemandMap;
import fr.vergne.translation.impl.OnDemandMetadata;
import fr.vergne.translation.impl.OnDemandProject;

/**
 * A {@link Writer} aims at replacing a stored value by a new one. This is the
 * basic interface to save contents for {@link OnDemandMetadata},
 * {@link OnDemandEntry}, {@link OnDemandMap}, and {@link OnDemandProject}.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 */
public interface Writer<Value> {
	public void write(Value value);
}
