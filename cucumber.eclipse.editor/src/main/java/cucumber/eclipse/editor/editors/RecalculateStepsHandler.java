package cucumber.eclipse.editor.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import cucumber.eclipse.editor.builder.BuilderUtil;

public class RecalculateStepsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);

		// Needs to be a gherkin editor for this to work, if not then simply do nothing.
		if (!(editorPart instanceof Editor)) {
			return null;
		}
		
		Editor editor = (Editor) editorPart;
		
		IFile file = editor.getFile();
		try {
			file.touch(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		BuilderUtil.buildProject(file.getProject(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
		
		return null;
	}

}
