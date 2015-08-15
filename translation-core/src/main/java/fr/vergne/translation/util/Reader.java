package fr.vergne.translation.util;

import fr.vergne.translation.impl.OnDemandEntry;
import fr.vergne.translation.impl.OnDemandMap;
import fr.vergne.translation.impl.OnDemandMetadata;
import fr.vergne.translation.impl.OnDemandProject;

/**
 * A {@link Reader} aims at recovering a stored value. This is the basic
 * interface to retrieve stored contents for {@link OnDemandMetadata},
 * {@link OnDemandEntry}, {@link OnDemandMap}, and {@link OnDemandProject}.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 */
public interface Reader<Value> {
	public Value read();
}
