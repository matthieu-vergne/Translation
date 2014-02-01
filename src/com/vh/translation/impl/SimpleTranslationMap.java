package com.vh.translation.impl;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vh.translation.TranslationEntry;
import com.vh.translation.TranslationMap;

// TODO adapt parsing capabilities to mistakes in the files
public class SimpleTranslationMap implements TranslationMap {

	private String RpgMakerTransPatchVersion = null;
	private File baseFile;
	private final List<TranslationEntry> entries = new LinkedList<TranslationEntry>();

	public List<TranslationEntry> getEntries() {
		return entries;
	}

	@Override
	public TranslationEntry getEntry(int index) {
		return entries.get(index);
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public Iterator<TranslationEntry> iterator() {
		return entries.iterator();
	}

	public void setBaseFile(File baseFile) {
		this.baseFile = baseFile;
	}

	@Override
	public File getBaseFile() {
		return baseFile;
	}

	public String getRpgMakerTransPatchVersion() {
		return RpgMakerTransPatchVersion;
	}

	public void setRpgMakerTransPatchVersion(String rpgMakerTransPatchVersion) {
		RpgMakerTransPatchVersion = rpgMakerTransPatchVersion;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + entries.size() + " entries]";
	}
}
