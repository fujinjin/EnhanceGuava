package nuke.idea.plugins.factory

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import groovy.transform.TypeChecked
import nuke.idea.plugins.model.EqualsAndHashCodeType
import nuke.idea.plugins.wizard.GenerateEqualsHashCodeWizard

@TypeChecked
class GenerateEqualsHashCodeWizardFactory {

    GenerateEqualsHashCodeWizard createWizard(Project project, PsiClass aClass, boolean needEquals, boolean needHashCode, EqualsAndHashCodeType type) {
        new GenerateEqualsHashCodeWizard(project, aClass, needEquals, needHashCode,type)
    }
}
