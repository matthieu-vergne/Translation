package fr.sazaju.vheditor.parsing.vh;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.sazaju.vheditor.translation.impl.MapFilesProject;
import fr.sazaju.vheditor.util.MultiReader;

public class VHProject extends MapFilesProject<VHMap> {

	// FIXME consider map labels
	public VHProject(File directory) {
		super(Arrays.asList(directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile() && file.length() > 0;
			}

		})), new MultiReader<File, VHMap>() {

			@Override
			public VHMap read(File file) {
				try {
					return new VHMap(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}
