package com.vh.util;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerConfiguration {

	private LoggerConfiguration() {
	}

	static public Logger getSimpleLogger() {
		Logger logger = Logger.getAnonymousLogger();
		configureSimpleLogger(logger, null);
		return logger;
	}

	static public void configureSimpleLogger(Logger logger) {
		configureSimpleLogger(logger, null);
	}

	static public void configureSimpleLogger(Logger logger, String file) {
		logger.setUseParentHandlers(false);
		addSimpleConsoleFormatting(logger);
		if (file == null) {
			// do not use any file
		} else {
			addFileHandler(logger, file);
		}
	}

	static public void addSimpleConsoleFormatting(Logger logger) {
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(createSimpleFormatter());
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
	}

	public static void addFileHandler(Logger logger, String filename) {
		try {
			FileHandler handler = new FileHandler(filename);
			handler.setFormatter(createSimpleFormatter());
			handler.setLevel(Level.ALL);
			logger.addHandler(handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Formatter createSimpleFormatter() {
		return new Formatter() {

			@Override
			public String format(LogRecord record) {
				long millis = record.getMillis();
				String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
						.format(new Date(millis));
				Level level = record.getLevel();
				String location = record.getSourceClassName() + "."
						+ record.getSourceMethodName() + "()";
				String message = record.getMessage();
				return date + " " + level + ": " + message + " [" + location
						+ "]\n";
			}
		};
	}
}
