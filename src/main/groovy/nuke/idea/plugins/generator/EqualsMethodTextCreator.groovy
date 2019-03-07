package nuke.idea.plugins.generator

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import groovy.transform.CompileStatic
import nuke.idea.plugins.model.EqualsAndHashCodeType
import nuke.idea.plugins.psi.EqualsMethodFinder
import nuke.idea.plugins.psi.ParentClassChecker

import static nuke.idea.plugins.model.EqualsAndHashCodeType.COMPARETO

@CompileStatic
class EqualsMethodTextCreator {

    private ParentClassChecker parentClassChecker
    private EqualsMethodFinder equalsMethodFinder

    EqualsMethodTextCreator(ParentClassChecker parentClassChecker, EqualsMethodFinder equalsMethodFinder) {
        this.parentClassChecker = parentClassChecker
        this.equalsMethodFinder = equalsMethodFinder
    }

    String createMethodText(List<PsiField> equalsPsiFields, PsiClass psiClass, EqualsAndHashCodeType type) {
        StringBuilder methodText = new StringBuilder()
        methodText << '@Override\n@SuppressWarnings("all")\npublic boolean equals(Object object) {'
        methodText << " if (object instanceof ${psiClass.name}){"
        if (parentClassChecker.hasClassWithOverriddenMethodInInheritanceHierarchy(equalsMethodFinder, psiClass)) {
            methodText << ' if (!super.equals(object)) {return false;} '
        }
        methodText << "${psiClass.name} that = (${psiClass.name}) object;"
        methodText << ' return '
        equalsPsiFields.eachWithIndex { PsiField field, int index ->

            if (isNotFirstField(index)) {
                methodText << '\n && '
            }
            if (type == COMPARETO) {
                if (field.getType().toString() == "PsiType:BigDecimal") {
                    methodText << "com.google.common.collect.Ordering.natural().nullsFirst().compare(this.${field.name},that.${field.name}) == 0"
                } else {
                    methodText << "com.google.common.base.Objects.equal(this.${field.name}, that.${field.name})"
                }
            } else {
                methodText << "com.google.common.base.Objects.equal(this.${field.name}, that.${field.name})"
            }

        }
        methodText << ';}\nreturn false;}'
        methodText.toString()
    }

    private boolean isNotFirstField(int index) {
        index > 0
    }

}

