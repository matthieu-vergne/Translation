package fr.sazaju.vheditor.translation.impl.backed;

import fr.vergne.parsing.layer.impl.GreedyMode;
import fr.vergne.parsing.layer.impl.Loop;

public class EntryLoop extends Loop<MapEntry> {

	public EntryLoop() {
		super(new Generator<MapEntry>() {

			@Override
			public MapEntry generates() {
				return new MapEntry();
			}
		});
		setMode(GreedyMode.POSSESSIVE);
	}
}
