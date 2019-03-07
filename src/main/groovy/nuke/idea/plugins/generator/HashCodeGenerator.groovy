package nuke.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.*
import groovy.transform.TypeChecked
import nuke.idea.plugins.psi.HashCodeMethodFinder
import nuke.idea.plugins.psi.ParentClassChecker
import org.jetbrains.annotations.NotNull

@TypeChecked
class HashCodeGenerator {

    private ParentClassChecker parentClassChecker
    private HashCodeMethodFinder hashCodeMethodFinder

    HashCodeGenerator(ParentClassChecker parentClassChecker, HashCodeMethodFinder hashCodeMethodFinder) {
        this.parentClassChecker = parentClassChecker
        this.hashCodeMethodFinder = hashCodeMethodFinder
    }

    PsiMethod hashCodeMethod(@NotNull List<PsiField> hashCodePsiFields, PsiClass psiClass) {
        StringBuilder methodText = new StringBuilder()
        methodText << '@Override\n@SuppressWarnings("all") public int hashCode() {return '
        PsiElementFactory factory = getFactory(psiClass)
        if (hashCodePsiFields.empty) {
            methodText << '0;}'
        } else {
            String fieldsString = hashCodePsiFields*.name.join(',')
            if (parentClassChecker.hasClassWithOverriddenMethodInInheritanceHierarchy(hashCodeMethodFinder, psiClass)) {
                methodText << '31 * super.hashCode() + '
            }
            methodText << "Objects.hashCode(${fieldsString});}"
        }
        factory.createMethodFromText(methodText.toString(), null, LanguageLevel.JDK_1_6)
    }

    private PsiElementFactory getFactory(PsiClass psiClass) {
        JavaPsiFacade.getInstance(psiClass.project).elementFactory
    }
}
