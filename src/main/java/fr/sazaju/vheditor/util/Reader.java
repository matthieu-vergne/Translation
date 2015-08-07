package fr.sazaju.vheditor.util;

import fr.sazaju.vheditor.translation.impl.OnDemandEntry;
import fr.sazaju.vheditor.translation.impl.OnDemandMap;
import fr.sazaju.vheditor.translation.impl.OnDemandMetadata;
import fr.sazaju.vheditor.translation.impl.OnDemandProject;

/**
 * A {@link Reader} aims at recovering a stored value. This is the basic
 * interface to retrieve stored contents for {@link OnDemandMetadata},
 * {@link OnDemandEntry}, {@link OnDemandMap}, and
 * {@link OnDemandProject}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <Value>
 */
public interface Reader<Value> {
	public Value read();
}
