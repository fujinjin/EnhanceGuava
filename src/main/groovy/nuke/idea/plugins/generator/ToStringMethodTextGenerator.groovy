package nuke.idea.plugins.generator

import com.intellij.psi.PsiField
import groovy.transform.TypeChecked
import nuke.idea.plugins.psi.ParentClassChecker
import nuke.idea.plugins.psi.ToStringMethodFinder

@TypeChecked
class ToStringMethodTextGenerator {
    private ParentClassChecker parentClassChecker
    private ToStringMethodFinder toStringMethodFinder

    ToStringMethodTextGenerator(ParentClassChecker parentClassChecker, ToStringMethodFinder toStringMethodFinder) {
        this.parentClassChecker = parentClassChecker
        this.toStringMethodFinder = toStringMethodFinder
    }

    String toStringMethod(List<PsiField> toStringPsiFields) {
        StringBuilder methodText = new StringBuilder()
        methodText << '@Override\n@SuppressWarnings("all")\npublic String toString() {\n'
        methodText << 'return com.google.common.base.MoreObjects.toStringHelper(this)\n'
        toStringPsiFields.eachWithIndex { PsiField field, int index ->
            methodText << ".add(\"${field.name}\",${field.name})\n"
        }
        methodText << '.toString();\n}'
        methodText.toString()
    }

}
