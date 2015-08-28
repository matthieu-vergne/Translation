package fr.vergne.translation.util.impl;

import fr.vergne.translation.util.MapNamer;

public class DefaultMapNamer<MapID> implements MapNamer<MapID> {

	@Override
	public String getName() {
		return "Default Namer";
	}

	@Override
	public String getDescription() {
		return "Use the default string representation of the map IDs.";
	}

	@Override
	public String getNameFor(MapID id) {
		return id.toString();
	}

}
