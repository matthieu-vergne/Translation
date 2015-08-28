package fr.vergne.translation.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.util.ProjectLoader;

@SuppressWarnings("serial")
public class ProjectLoaderPanel<TProject extends TranslationProject<?, ?>>
		extends JPanel {

	private final JTextField folderPathField = new JTextField();
	private final Collection<ProjectLoadedListener<TProject>> listeners = new HashSet<>();
	private final ProjectLoader<TProject> projectLoader;

	public ProjectLoaderPanel(ProjectLoader<TProject> projectLoader) {
		this.projectLoader = projectLoader;

		folderPathField.setEditable(false);
		folderPathField.setText("Map folder...");

		JButton openButton = new JButton(new AbstractAction("Browse") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String path = folderPathField.getText();
				JFileChooser fileChooser = new JFileChooser(new File(path
						.isEmpty() ? "." : path));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setFileHidingEnabled(true);
				fileChooser.setMultiSelectionEnabled(false);
				int answer = fileChooser.showDialog(ProjectLoaderPanel.this,
						"Open");
				if (answer == JFileChooser.APPROVE_OPTION) {
					setProjectPath(fileChooser.getSelectedFile());
				} else {
					// do not consider it
				}
			}
		});
		openButton
				.setToolTipText("Select the folder of the translation project.");

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 1;
		add(openButton, constraints);
		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		add(folderPathField, constraints);
	}
	
	public void setProjectPath(File directory) {
		TProject currentProject = projectLoader.load(directory);
		folderPathField.setText(directory.toString());
		for (ProjectLoadedListener<TProject> listener : listeners) {
			listener.projectLoaded(directory, currentProject);
		}
	}

	public void addProjectLoadedListener(
			ProjectLoadedListener<TProject> listener) {
		listeners.add(listener);
	}

	public void removeProjectLoadedListener(
			ProjectLoadedListener<TProject> listener) {
		listeners.remove(listener);
	}

	public static interface ProjectLoadedListener<TProject extends TranslationProject<?, ?>> {
		public void projectLoaded(File directory, TProject project);
	}
}
