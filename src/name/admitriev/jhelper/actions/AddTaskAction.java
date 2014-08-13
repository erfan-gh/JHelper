package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.Util;
import name.admitriev.jhelper.configuration.TaskConfigurationType;
import name.admitriev.jhelper.task.Task;
import name.admitriev.jhelper.ui.AddTaskDialog;
import net.egork.chelper.util.OutputWriter;

public class AddTaskAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = e.getProject();
		if(project == null) {
			return;
		}

		AddTaskDialog dialog = new AddTaskDialog(project);
		dialog.show();
		if(!dialog.isOK()) {
			return;
		}
		final Task task = dialog.getTask();

		final VirtualFile newTaskFile = Util.findOrCreateByRelativePath(project.getBaseDir(), task.getPath());
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				OutputWriter writer = Util.getOutputWriter(newTaskFile, this);
				task.saveTask(writer);
				writer.flush();
				writer.close();
			}
		});

		createConfigurationForTask(project, task);
	}

	private static void createConfigurationForTask(Project project, Task task) {
		TaskConfigurationType configurationType = new TaskConfigurationType();
		ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];

		RunManager manager = RunManager.getInstance(project);
		RunnerAndConfigurationSettings configuration = manager.createRunConfiguration(task.getName(), factory);
		manager.addConfiguration(configuration, true);

		manager.setSelectedConfiguration(configuration);
	}
}
