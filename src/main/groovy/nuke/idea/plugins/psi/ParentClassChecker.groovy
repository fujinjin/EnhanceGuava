package nuke.idea.plugins.psi

import com.intellij.psi.PsiClass
import groovy.transform.CompileStatic

@CompileStatic
class ParentClassChecker {

    boolean hasClassWithOverriddenMethodInInheritanceHierarchy(MethodFinder finder, PsiClass psiClass) {
        boolean result = false
        PsiClass psiParentClass = getParentClass(psiClass)
        if (psiParentClass != null) {
            result = finder.hasMethod(psiParentClass)
            if (!result) {
                return hasClassWithOverriddenMethodInInheritanceHierarchy(finder, psiParentClass)
            }
        }
        result
    }

    private static PsiClass getParentClass(PsiClass psiClass) {
        PsiClass parent = null
        if (psiClass.extendsList?.referencedTypes?.length > 0) {
            parent = psiClass.extendsList.referencedTypes[0].resolve()
        }
        parent
    }
}
