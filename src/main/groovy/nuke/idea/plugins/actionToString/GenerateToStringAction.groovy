package nuke.idea.plugins.actionToString

import com.intellij.codeInsight.generation.actions.BaseGenerateAction
import groovy.transform.CompileStatic
import nuke.idea.plugins.factory.GenerateToStringWizardFactory
import nuke.idea.plugins.generator.ToStringGenerator
import nuke.idea.plugins.generator.ToStringMethodTextGenerator
import nuke.idea.plugins.psi.ParentClassChecker
import nuke.idea.plugins.psi.ToStringMethodFinder
import org.picocontainer.MutablePicoContainer
import org.picocontainer.defaults.DefaultPicoContainer

@SuppressWarnings('UnnecessaryObjectReferences')
@CompileStatic
class GenerateToStringAction extends BaseGenerateAction {

    private static MutablePicoContainer picoContainer = new DefaultPicoContainer()

    private static GenerateToStringActionHandler handler

    static {
        picoContainer.registerComponentImplementation(ParentClassChecker)
        picoContainer.registerComponentImplementation(ToStringGenerator)
        picoContainer.registerComponentImplementation(ToStringMethodFinder)
        picoContainer.registerComponentImplementation(ToStringMethodTextGenerator)
        picoContainer.registerComponentImplementation(GenerateToStringWizardFactory)
        picoContainer.registerComponentImplementation(GenerateToStringActionHandler)
        handler = (GenerateToStringActionHandler) picoContainer.getComponentInstanceOfType(GenerateToStringActionHandler)
    }

    GenerateToStringAction() {
        super(handler)
    }
}
