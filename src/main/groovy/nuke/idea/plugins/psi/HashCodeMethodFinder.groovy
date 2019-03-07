package nuke.idea.plugins.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import groovy.transform.CompileStatic

@CompileStatic
class HashCodeMethodFinder implements MethodFinder {
    boolean hasMethod(PsiClass psiClass) {
        psiClass.findMethodsByName('hashCode', false).any { PsiMethod method ->
            isPublic(method) && hasNoParameters(method) && returnsInt(method)
        }
    }

    private static boolean returnsInt(PsiMethod method) {
        method.returnType.equalsToText('int')
    }

    private static boolean isPublic(PsiMethod method) {
        method.hasModifierProperty(PsiModifier.PUBLIC)
    }

    private static boolean hasNoParameters(PsiMethod method) {
        method.parameterList?.parameters?.size() == 0
    }

}
