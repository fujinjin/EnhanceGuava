package nuke.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.*
import groovy.transform.TypeChecked
import nuke.idea.plugins.model.EqualsAndHashCodeType
import org.jetbrains.annotations.NotNull

@TypeChecked
class EqualsGenerator {

    private EqualsMethodTextCreator equalsMethodTextCreator

    EqualsGenerator(EqualsMethodTextCreator equalsMethodTextCreator) {
        this.equalsMethodTextCreator = equalsMethodTextCreator
    }

    PsiMethod equalsMethod(@NotNull List<PsiField> equalsPsiFields, PsiClass psiClass, EqualsAndHashCodeType type) {
        if (!equalsPsiFields.isEmpty()) {
            PsiElementFactory factory = getFactory(equalsPsiFields[0])
            String methodText = equalsMethodTextCreator.createMethodText(equalsPsiFields, psiClass, type)
            factory.createMethodFromText(methodText, null, LanguageLevel.JDK_1_6)
        }
    }

    private PsiElementFactory getFactory(PsiField psiField) {
        JavaPsiFacade.getInstance(psiField.project).elementFactory
    }
}
