package nuke.idea.plugins.wizard;

import com.intellij.ide.wizard.StepAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.refactoring.ui.MemberSelectionPanel;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.util.containers.HashMap;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class GenerateToStringWizard extends AbstractToStringWizard {
    private final PsiClass myClass;

    private final MemberSelectionPanel myToStringPanel;
    private final HashMap myFieldsToString;

    private final List<MemberInfo> myClassFields;
    private static final MyMemberInfoFilter MEMBER_INFO_FILTER = new MyMemberInfoFilter();


    public GenerateToStringWizard(Project project, PsiClass aClass) {
        super("Generate toString()", project);
        myClass = aClass;
        myClassFields = MemberInfo.extractClassMembers(myClass, MEMBER_INFO_FILTER, false);
        for (MemberInfo myClassField : myClassFields) {
            PsiField psiField = ((PsiField) myClassField.getMember());
            if (psiField.hasModifierProperty(PsiModifier.STATIC) || psiField.hasModifierProperty(PsiModifier.TRANSIENT)){
                myClassField.setChecked(false);
            } else {
                myClassField.setChecked(true);
            }
        }
        myFieldsToString = createFieldToMemberInfoMap(true);
        final List<MemberInfo> toStringMemberInfos = myClassFields;

            myToStringPanel = new MemberSelectionPanel("Choose fields to be toString",
                    toStringMemberInfos, null);
            final MyTableModelListener listener = new MyTableModelListener();

            myToStringPanel.getTable().getModel().addTableModelListener(listener);
            addStep(new MyStep(myToStringPanel));

        init();
        updateStatus();
    }

    public PsiField[] getToStringMemberInfosFields() {
        if (myToStringPanel != null) {
            return memberInfosToFields(myToStringPanel.getTable().getSelectedMemberInfos());
        } else {
            return null;
        }
    }

    private static PsiField[] memberInfosToFields(Collection<MemberInfo> infos) {
        ArrayList<PsiField> list = new ArrayList<PsiField>();
        for (MemberInfo info : infos) {
            list.add((PsiField) info.getMember());
        }
        return list.toArray(new PsiField[list.size()]);
    }

    protected void updateStep() {
        super.updateStep();
    }

    protected String getHelpID() {
        return "editing.altInsert.equals";
    }

    private void toStringFieldsSelected() {
        Collection<MemberInfo> selectedMemberInfos = myToStringPanel.getTable().getSelectedMemberInfos();
        updateToStringMemberInfosMemberInfos(selectedMemberInfos);
    }

    @Override
    protected void doOKAction() {
        if (myToStringPanel != null) {
            toStringFieldsSelected();
        }
        super.doOKAction();
    }

    private HashMap<PsiElement, MemberInfo> createFieldToMemberInfoMap(boolean checkedByDefault) {
        Collection<MemberInfo> memberInfos = MemberInfo.extractClassMembers(myClass, MEMBER_INFO_FILTER, false);
        final HashMap<PsiElement, MemberInfo> result = new HashMap<PsiElement, MemberInfo>();
        for (MemberInfo memberInfo : memberInfos) {
            memberInfo.setChecked(checkedByDefault);
            result.put(memberInfo.getMember(), memberInfo);
        }
        return result;
    }

    private void updateToStringMemberInfosMemberInfos(Collection<MemberInfo> equalsMemberInfos) {
        if (myToStringPanel == null) return;
        List<MemberInfo> toStringMemberInfosFields = new ArrayList<MemberInfo>();

        for (MemberInfo equalsMemberInfo : equalsMemberInfos) {
            toStringMemberInfosFields.add((MemberInfo) myFieldsToString.get(equalsMemberInfo.getMember()));
        }

        myToStringPanel.getTable().setMemberInfos(toStringMemberInfosFields);
    }

    private void updateStatus() {
        boolean finishEnabled = true;
            boolean anyChecked = false;
            for (MemberInfo classField : myClassFields) {
                if (classField.isChecked()) {
                    anyChecked = true;
                    break;
                }
            }
            finishEnabled &= anyChecked;


        getFinishButton().setEnabled(finishEnabled);
        if (finishEnabled && getFinishButton().isVisible()) {
            getRootPane().setDefaultButton(getFinishButton());
        }
    }

    private class MyTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            updateStatus();
        }
    }

    private static class MyStep extends StepAdapter {
        final MemberSelectionPanel myPanel;

        public MyStep(MemberSelectionPanel panel) {
            myPanel = panel;
        }

        public Icon getIcon() {
            return null;
        }

        public JComponent getComponent() {
            return myPanel;
        }

    }

    private static class MyMemberInfoFilter implements MemberInfoBase.Filter<PsiMember> {
        public boolean includeMember(PsiMember element) {
            return element instanceof PsiField;
        }
    }
}

