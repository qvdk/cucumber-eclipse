package cucumber.eclipse.steps.integration.marker;

import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import cucumber.eclipse.steps.integration.Activator;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.StepSerialization;

public class MarkerFactory {
	
	public static final MarkerFactory INSTANCE = new MarkerFactory();
	
	public static final String STEPDEF_SYNTAX_ERROR = "cucumber.eclipse.marker.stepdef.syntaxerror";
	public static final String GHERKIN_SYNTAX_ERROR = "cucumber.eclipse.marker.gherkin.syntaxerror";
	public static final String STEP_DEFINTION_MATCH = "cucumber.eclipse.marker.stepdef.matches";
	public static final String SCENARIO_OUTLINE_EXAMPLE_UNMATCH = "cucumber.eclipse.marker.scenario_outline_example_unmatch";
	public static final String MULTIPLE_STEP_DEFINTIONS_MATCH = "cucumber.eclipse.marker.stepdef.multiple_matches";

	public static final String UNMATCHED_STEP = "cucumber.eclipse.marker.gherkin.unmatched_step";
	public static final String UNMATCHED_STEP_STEP_ATTRIBUTE = UNMATCHED_STEP + ".step";
	public static final String UNMATCHED_STEP_PATH_ATTRIBUTE = UNMATCHED_STEP + ".path";
	
	private MarkerFactory() {}
	
	public void syntaxErrorOnStepDefinition(IResource stepDefinitionResource, Exception e) {
		syntaxErrorOnStepDefinition(stepDefinitionResource, e, 0);
	}

	public void multipleStepDefinitionsMatched(final IResource gherkinFile, final gherkin.formatter.model.Step gherkinStep, final StepDefinition... stepDefinitions) {
		this.mark(gherkinFile, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				
				
				try {
					marker = gherkinFile.createMarker(MULTIPLE_STEP_DEFINTIONS_MATCH);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.LINE_NUMBER, gherkinStep.getLine());
					
					StringBuffer stepDefinitionsNamesStringBuffer = new StringBuffer();
					StringBuffer stepDefinitionsFullPathsStringBuffer = new StringBuffer();

					for (int it = 0 ; it < stepDefinitions.length; it++) {
						StepDefinition stepDefinition = stepDefinitions[it];
						stepDefinitionsNamesStringBuffer.append(stepDefinition.getSource().getName()).append(":")
								.append(stepDefinition.getLineNumber());
						
						stepDefinitionsFullPathsStringBuffer.append(stepDefinition.getSource().getFullPath()).append(":")
						.append(stepDefinition.getLineNumber());
						
						if(it == stepDefinitions.length) {
							stepDefinitionsNamesStringBuffer.append(",");
							stepDefinitionsFullPathsStringBuffer.append(",");
						}
					}
					
					String message = String.format(
							"Step '%s' have more than one glue code: %s",
							gherkinStep.getName(), stepDefinitionsNamesStringBuffer.toString());
					
					marker.setAttribute(IMarker.MESSAGE, message);
					marker.setAttribute("duplicates", stepDefinitionsFullPathsStringBuffer.toString());
					
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
	}
	
	public void unmatchedStep(final IDocument gherkinDocument, final IResource gherkinFile, final gherkin.formatter.model.Step gherkinStep, final int lineNumber) {
		this.mark(gherkinFile, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				
				String warningMessage = String.format(
						"Step '%s' does not have a matching glue code%s",
						gherkinStep.getName(), "");
				
				int lineStartOffset = 0;
				
				IRegion lineInfo = null;
				String currentLine = null;
				try {
					lineInfo = gherkinDocument.getLineInformation(lineNumber-1);
					lineStartOffset = lineInfo.getOffset();
					currentLine = gherkinDocument.get(lineStartOffset, lineInfo.getLength());
				} catch (BadLocationException e) {
					return null;
				}
				
				String textStatement = gherkinStep.getName();
				int statementStartOffset = lineStartOffset + currentLine.indexOf(textStatement);

				IRegion stepRegion = new Region(statementStartOffset, textStatement.length());
				try {
					marker = gherkinFile.createMarker(UNMATCHED_STEP);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.MESSAGE, warningMessage);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					marker.setAttribute(IMarker.CHAR_START, stepRegion.getOffset());
					marker.setAttribute(IMarker.CHAR_END, stepRegion.getOffset() + stepRegion.getLength());
					marker.setAttribute(UNMATCHED_STEP_STEP_ATTRIBUTE, StepSerialization.serialize(gherkinStep));
					marker.setAttribute(UNMATCHED_STEP_PATH_ATTRIBUTE, gherkinFile.getFullPath().toString());
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
	}
	
	
	public void syntaxErrorOnStepDefinition(final IResource stepDefinitionResource, final Exception e, final int lineNumber) {
		
		this.mark(stepDefinitionResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					marker = stepDefinitionResource.createMarker(STEPDEF_SYNTAX_ERROR);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, e.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
		
	}

	public void syntaxErrorOnGherkin(IResource stepDefinitionResource, Exception e) {
		syntaxErrorOnGherkin(stepDefinitionResource, e, 0);
	}
	
	public void syntaxErrorOnGherkin(final IResource gherkinResource, final Exception e, final int lineNumber) {
		
		this.mark(gherkinResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					marker = gherkinResource.createMarker(GHERKIN_SYNTAX_ERROR);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, e.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
		
	}
	
	public void gherkinStepWithDefinitionFound(final IResource gherkinResource, final StepDefinition stepDefinition, final int lineNumber) {
		
		this.mark(gherkinResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				try {
					marker = gherkinResource.createMarker(STEP_DEFINTION_MATCH);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
					String message = String.format("Glued with %s:%s", stepDefinition.getSource().getName(), stepDefinition.getLineNumber());
					marker.setAttribute(IMarker.MESSAGE, message);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
		
	}
	
	public void gherkinStepExampleUnmatch(final IDocument gherkinDocument, final IResource gherkinResource, final int lineNumber) {
		
		this.mark(gherkinResource, new IMarkerBuilder() {
			@Override
			public IMarker build() {
				IMarker marker = null;
				int lineStartOffset = 0;
				
				IRegion lineInfo = null;
				String currentLine = null;
				try {
					lineInfo = gherkinDocument.getLineInformation(lineNumber-1);
					lineStartOffset = lineInfo.getOffset();
					currentLine = gherkinDocument.get(lineStartOffset, lineInfo.getLength());
				} catch (BadLocationException e) {
					return null;
				}
				
				String currentLineTrim = currentLine.trim();
				int statementStartOffset = lineStartOffset + currentLine.indexOf(currentLineTrim);

				IRegion stepRegion = new Region(statementStartOffset, currentLineTrim.length());
				
				try {
					marker = gherkinResource.createMarker(SCENARIO_OUTLINE_EXAMPLE_UNMATCH);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.MESSAGE, String.format("No compatible step definition with %s", currentLineTrim));
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					marker.setAttribute(IMarker.CHAR_START, stepRegion.getOffset());
					marker.setAttribute(IMarker.CHAR_END, stepRegion.getOffset() + stepRegion.getLength());
					
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return marker;
			}
		});
		
	}
	
	public void cleanMarkers(IResource resource) {
		try {
			resource.deleteMarkers(STEPDEF_SYNTAX_ERROR, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(GHERKIN_SYNTAX_ERROR, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(STEP_DEFINTION_MATCH, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(SCENARIO_OUTLINE_EXAMPLE_UNMATCH, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(MULTIPLE_STEP_DEFINTIONS_MATCH, false, IResource.DEPTH_ZERO);
			resource.deleteMarkers(UNMATCHED_STEP, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Couldn't remove markers from %s", resource), e));
		}
	}
	
	private void mark(final IResource resource, final IMarkerBuilder markerBuilder) {
		try {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					markerBuilder.build();
				}
			};

			resource.getWorkspace().run(runnable, null,IWorkspace.AVOID_UPDATE, null);
			
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					String.format("Failed to place marker %s", resource), e));
		}
	}
	
	
	public interface IMarkerBuilder {
		IMarker build();
	}
}