package nuke.idea.plugins.action

import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiUtil
import groovy.transform.TypeChecked
import nuke.idea.plugins.model.EqualsAndHashCodeType

import static nuke.idea.plugins.model.EqualsAndHashCodeType.EQAULS
import static nuke.idea.plugins.model.EqualsAndHashCodeType.COMPARETO

@TypeChecked
class TypeChooser {
    EqualsAndHashCodeType chooseType(PsiClass psiClass) {
        PsiUtil.isLanguageLevel7OrHigher(psiClass) ? COMPARETO : EQAULS
    }
}
