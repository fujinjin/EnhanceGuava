package nuke.idea.plugins.action

import com.intellij.codeInsight.generation.actions.BaseGenerateAction
import groovy.transform.CompileStatic
import nuke.idea.plugins.factory.GenerateEqualsHashCodeWizardFactory
import nuke.idea.plugins.generator.EqualsGenerator
import nuke.idea.plugins.generator.EqualsMethodTextCreator
import nuke.idea.plugins.generator.HashCodeGenerator
import nuke.idea.plugins.psi.EqualsMethodFinder
import nuke.idea.plugins.psi.HashCodeMethodFinder
import nuke.idea.plugins.psi.ParentClassChecker
import org.picocontainer.MutablePicoContainer
import org.picocontainer.defaults.DefaultPicoContainer

@SuppressWarnings('UnnecessaryObjectReferences')
@CompileStatic
class GenerateEqualsHashCodeAction extends BaseGenerateAction {

    private static MutablePicoContainer picoContainer = new DefaultPicoContainer()

    private static GenerateEqualsHashCodeActionHandler handler

    static {
        picoContainer.registerComponentImplementation(EqualsMethodFinder)
        picoContainer.registerComponentImplementation(TypeChooser)
        picoContainer.registerComponentImplementation(HashCodeMethodFinder)
        picoContainer.registerComponentImplementation(ParentClassChecker)
        picoContainer.registerComponentImplementation(EqualsMethodTextCreator)
        picoContainer.registerComponentImplementation(HashCodeGenerator)
        picoContainer.registerComponentImplementation(EqualsGenerator)
        picoContainer.registerComponentImplementation(GenerateEqualsHashCodeWizardFactory)
        picoContainer.registerComponentImplementation(GenerateEqualsHashCodeActionHandler)
        handler = (GenerateEqualsHashCodeActionHandler) picoContainer.getComponentInstanceOfType(GenerateEqualsHashCodeActionHandler)
    }

    protected GenerateEqualsHashCodeAction() {
        super(handler)
    }
}
