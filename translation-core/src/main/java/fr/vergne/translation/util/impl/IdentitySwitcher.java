package fr.vergne.translation.util.impl;

import fr.vergne.translation.util.Switcher;

/**
 * An {@link IdentitySwitcher} simply provides a {@link Switcher} which does not
 * change the representation. It is mainly used for cases where you manage in a
 * generic way a set of {@link Switcher}s in which some of them may be useless
 * (no need to switch). Consequently, this {@link Switcher} does not switch
 * anything and just return back the exactly same value.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <T>
 */
public class IdentitySwitcher<T> implements Switcher<T, T> {

	@Override
	public T switchForth(T value) {
		return value;
	}

	@Override
	public T switchBack(T value) {
		return value;
	}

}
