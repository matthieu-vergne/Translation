package fr.sazaju.vheditor.parsing.vh.map;

import fr.sazaju.vheditor.parsing.vh.map.MapEntry.MapSaver;
import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop;

public class EntryLoop extends Loop<MapEntry> {

	public EntryLoop(final MapSaver saver) {
		super(new Generator<MapEntry>() {

			@Override
			public MapEntry generates() {
				return new MapEntry(saver);
			}
		});
		setMode(GreedyMode.POSSESSIVE);
	}
}
