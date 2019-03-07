package nuke.idea.plugins.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier


class ToStringMethodFinder implements MethodFinder {
    boolean hasMethod(PsiClass psiClass) {
        psiClass.findMethodsByName('toString', false).any { PsiMethod method ->
            isPublic(method) && isNotStatic(method)&&isNotTransient(method)&&hasNoParameters(method) && returnsInt(method)
        }
    }

    private static boolean isNotStatic(PsiMethod method) {
        !method.hasModifierProperty(PsiModifier.STATIC)
    }

    private static boolean isNotTransient(PsiMethod method) {
        !method.hasModifierProperty(PsiModifier.TRANSIENT)
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
