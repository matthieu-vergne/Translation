package fr.vergne.translation.util;

import fr.vergne.translation.impl.OnDemandMap;
import fr.vergne.translation.impl.OnDemandProject;

/**
 * A {@link MultiReader} aims at recovering a stored value identified by an ID.
 * This is the basic interface to retrieve a set of stored contents for
 * {@link OnDemandMap}, and {@link OnDemandProject}.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <ID>
 * @param <Value>
 */
public interface MultiReader<ID, Value> {
	public Value read(ID id);
}
