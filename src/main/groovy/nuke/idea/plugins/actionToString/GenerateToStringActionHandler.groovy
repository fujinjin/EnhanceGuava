package nuke.idea.plugins.actionToString

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.generation.*
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.util.MethodSignature
import com.intellij.psi.util.MethodSignatureUtil
import com.intellij.util.IncorrectOperationException
import nuke.idea.plugins.factory.GenerateToStringWizardFactory
import nuke.idea.plugins.generator.ToStringGenerator
import nuke.idea.plugins.wizard.GenerateToStringWizard

import static java.lang.String.format

class GenerateToStringActionHandler extends GenerateMembersHandlerBase {

    static
    final String METHODS_DEFINED_FOR_ANONYMOUS_CLASS = 'Methods "String toString()" are already defined \nfor this anonymous class. Do you want to delete them and proceed?'
    static
    final String METHODS_DEFINED_FOR_CLASS = 'Methods "String toString()" are already defined\nfor class %s. Do you want to delete them and proceed?'
    static final String TITLE = 'generate.toString.already.defined.title'

    static final PsiElementClassMember[] DUMMY_RESULT = new PsiElementClassMember[1]
    //cannot return empty array, but this result won't be used anyway
    static final String ONLY_STATIC_FIELDS_ERROR = 'No fields in toString have been found'

    ToStringGenerator toStringGenerator
    GenerateToStringWizardFactory stringWizardFactory

    PsiField[] toStringFields = null

    GenerateToStringActionHandler(ToStringGenerator toStringGenerator, GenerateToStringWizardFactory stringWizardFactory) {
        super('')
        this.toStringGenerator = toStringGenerator
        this.stringWizardFactory = stringWizardFactory
    }

    @Override
    protected List<? extends GenerationInfo> generateMemberPrototypes(PsiClass psiClass, ClassMember[] originalMembers) throws IncorrectOperationException {


        PsiMethod toStringMethod = toStringGenerator.toStringMethod(toStringFields as List)
        OverrideImplementUtil.convert2GenerationInfos([toStringMethod]);
    }

    @SuppressWarnings('ReturnsNullInsteadOfEmptyArray')
    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project, Editor editor) {
        toStringFields = null
        PsiMethod toStringMethod = GenerateEqualsHelper.findMethod(aClass, getToStringSignature());

        boolean needToString = toStringExit(toStringMethod)

        if (needToString) {
            String text = chooseText(aClass)
            if (shouldDeleteMethods(project, text) && methodsDeletedSuccessfully(toStringMethod)) {
            } else {
                return null
            }
        }
        if (hasOnlyStaticFields(aClass)) {
            showErrorMessage(editor)
            return null
        }

        GenerateToStringWizard wizard = stringWizardFactory.createWizard(project, aClass)

        wizard.show()
        if (!wizard.isOK()) {
            return null
        }
        toStringFields = wizard.toStringMemberInfosFields
        DUMMY_RESULT

    }


    private boolean toStringExit(PsiMethod toStringMethod) {
        toStringMethod != null
    }


    private showErrorMessage(Editor editor) {
        HintManager.instance.showErrorHint(editor, ONLY_STATIC_FIELDS_ERROR)
    }

    private boolean hasOnlyStaticFields(PsiClass aClass) {
        boolean hasOnlyStaticFields = true
        for (PsiField field : aClass.fields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                hasOnlyStaticFields = false
                break
            }
        }
        hasOnlyStaticFields
    }

    private boolean methodsDeletedSuccessfully(PsiMethod toStringMethod) {
        Application application = ApplicationManager.application
        application.runWriteAction(new DeleteExistingToStringMethodsComputable(toStringMethod))
    }

    private boolean shouldDeleteMethods(Project project, String text) {
        Messages.showYesNoDialog(project, text, CodeInsightBundle.message(TITLE), Messages.questionIcon) == DialogWrapper.OK_EXIT_CODE
    }

    private String chooseText(PsiClass aClass) {
        (aClass instanceof PsiAnonymousClass) ? METHODS_DEFINED_FOR_ANONYMOUS_CLASS : format(METHODS_DEFINED_FOR_CLASS, aClass.name)
    }

    @Override
    protected void cleanup() {
        super.cleanup()
        toStringFields = null
    }

    @Override
    protected ClassMember[] getAllOriginalMembers(PsiClass psiClass) {
        null
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(PsiClass psiClass, ClassMember classMember) {
        null
    }

    public static MethodSignature getToStringSignature() {
        return MethodSignatureUtil.createMethodSignature("toString", PsiType.EMPTY_ARRAY, PsiTypeParameter.EMPTY_ARRAY, PsiSubstitutor.EMPTY);
    }
}
