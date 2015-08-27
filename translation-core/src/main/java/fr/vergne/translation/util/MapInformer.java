package fr.vergne.translation.util;

public interface MapInformer<MapID> {

	public int getEntriesCount(MapID mapId) throws NoDataException;

	public int getEntriesRemaining(MapID mapId) throws NoDataException;

	public boolean isModified(MapID mapId) throws NoDataException;

	public int getAllEntriesCount() throws NoDataException;

	public int getAllEntriesRemaining() throws NoDataException;

	public void addMapSummaryListener(MapSummaryListener<MapID> listener);

	public void removeMapSummaryListener(MapSummaryListener<MapID> listener);

	public interface MapSummaryListener<MapID> {
		public void mapSummarized(MapID mapId);
	}

	@SuppressWarnings("serial")
	public static class NoDataException extends Exception {

		public NoDataException() {
		}

		public NoDataException(String message) {
			super(message);
		}

		public NoDataException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
