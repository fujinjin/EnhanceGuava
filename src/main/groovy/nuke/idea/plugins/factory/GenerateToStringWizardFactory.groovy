package nuke.idea.plugins.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import groovy.transform.TypeChecked
import nuke.idea.plugins.wizard.GenerateToStringWizard

@TypeChecked
class GenerateToStringWizardFactory {
    GenerateToStringWizard createWizard(Project project, PsiClass aClass) {
        new GenerateToStringWizard(project, aClass)
    }
}
