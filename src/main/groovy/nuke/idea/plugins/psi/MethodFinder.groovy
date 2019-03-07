package nuke.idea.plugins.psi

import com.intellij.psi.PsiClass
import groovy.transform.CompileStatic

@CompileStatic
interface MethodFinder {

    boolean hasMethod(PsiClass psiClass)
}
