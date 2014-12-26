package fr.sazaju.vheditor.util;

import java.io.File;

public interface MapInformer {

	public int getEntriesCount(File mapFile) throws NoDataException;

	public int getEntriesRemaining(File mapFile) throws NoDataException;

	public String getLabel(File mapFile) throws NoDataException;

	public void addMapSummaryListener(MapSummaryListener listener);

	public void removeMapSummaryListener(MapSummaryListener listener);

	public interface MapSummaryListener {
		public void mapSummarized(File map);
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
