package nuke.idea.plugins.actionToString

import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiMethod
import com.intellij.util.IncorrectOperationException


class DeleteExistingToStringMethodsComputable implements Computable<Boolean> {
    PsiMethod toStringMethod

    DeleteExistingToStringMethodsComputable(PsiMethod toStringMethod) {
        this.toStringMethod = toStringMethod
    }

    Boolean compute() {
        try {
            toStringMethod?.delete()
            true
        }
        catch (IncorrectOperationException e) {
            false
        }
    }
}
