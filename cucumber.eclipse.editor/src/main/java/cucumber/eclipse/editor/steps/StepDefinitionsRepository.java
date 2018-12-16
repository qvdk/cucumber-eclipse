package cucumber.eclipse.editor.steps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import cucumber.eclipse.steps.integration.StepDefinition;

/**
 * Storage class for step definitions.
 * 
 * Step definitions are store by resource name in order to be able to update
 * them partially.
 * 
 * Thus, when we computes step definitions from only one resource, the previous
 * computed step definitions for this resource are overwritten. And we don't 
 * have to compute step definitions for all projects to have a real view of 
 * step definitions. 
 * 
 * 
 * @author qvdk
 *
 */
public class StepDefinitionsRepository {

	private Map<IFile, Set<StepDefinition>> stepsByResourceName;

	protected StepDefinitionsRepository() {
		this.reset();
	}

	public void add(IFile stepDefinitionsFile, List<StepDefinition> steps) {
		this.stepsByResourceName.put(stepDefinitionsFile, new HashSet<StepDefinition>(steps));
	}

	public void add(IFile stepDefinitionsFile, Set<StepDefinition> steps) {
		if(steps.isEmpty()) {
			this.stepsByResourceName.remove(stepDefinitionsFile);
		}
		else {
			this.stepsByResourceName.put(stepDefinitionsFile, steps);
		}
	}

	public Set<IFile> getAllStepDefinitionsFiles() {
		return this.stepsByResourceName.keySet();
	}
	
	public Set<StepDefinition> getAllStepDefinitions() {
		Set<StepDefinition> allSteps = new HashSet<StepDefinition>();
		for (Set<StepDefinition> steps : stepsByResourceName.values()) {
			allSteps.addAll(steps);
		}
		return allSteps;
	}
	
	public boolean isStepDefinitions(IFile file) {
		return this.stepsByResourceName.containsKey(file);
	}

	public void reset() {
		this.stepsByResourceName = new HashMap<IFile, Set<StepDefinition>>();
	}


}