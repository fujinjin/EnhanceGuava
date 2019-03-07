package nuke.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.annotations.NotNull

class ToStringGenerator {
    private ToStringMethodTextGenerator toStringMethodTextGenerator

    ToStringGenerator(ToStringMethodTextGenerator toStringMethodTextGenerator) {
        this.toStringMethodTextGenerator = toStringMethodTextGenerator
    }

    PsiMethod toStringMethod(@NotNull List<PsiField> toStringPsiFields) {
        if (!toStringPsiFields.isEmpty()) {
            PsiElementFactory factory = getFactory(toStringPsiFields[0])
            String methodText = toStringMethodTextGenerator.toStringMethod(toStringPsiFields)
            factory.createMethodFromText(methodText, null, LanguageLevel.JDK_1_6)
        }
    }

    private PsiElementFactory getFactory(PsiField psiField) {
        JavaPsiFacade.getInstance(psiField.project).elementFactory
    }
}
