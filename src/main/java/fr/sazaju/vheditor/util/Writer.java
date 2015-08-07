package fr.sazaju.vheditor.util;

import fr.sazaju.vheditor.translation.impl.OnDemandEntry;
import fr.sazaju.vheditor.translation.impl.OnDemandMap;
import fr.sazaju.vheditor.translation.impl.OnDemandMetadata;
import fr.sazaju.vheditor.translation.impl.OnDemandProject;

/**
 * A {@link Writer} aims at replacing a stored value by a new one. This is the
 * basic interface to save contents for {@link OnDemandMetadata},
 * {@link OnDemandEntry}, {@link OnDemandMap}, and
 * {@link OnDemandProject}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <Value>
 */
public interface Writer<Value> {
	public void write(Value value);
}
