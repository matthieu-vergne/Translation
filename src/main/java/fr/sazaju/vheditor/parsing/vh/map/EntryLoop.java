package fr.sazaju.vheditor.parsing.vh.map;

import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop;

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
