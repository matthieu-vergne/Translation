package fr.sazaju.vheditor.util;

import fr.sazaju.vheditor.translation.impl.OnDemandMap;
import fr.sazaju.vheditor.translation.impl.OnDemandProject;

/**
 * A {@link MultiReader} aims at recovering a stored value identified by an ID.
 * This is the basic interface to retrieve a set of stored contents for
 * {@link OnDemandMap}, and {@link OnDemandProject}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <ID>
 * @param <Value>
 */
public interface MultiReader<ID, Value> {
	public Value read(ID id);
}
