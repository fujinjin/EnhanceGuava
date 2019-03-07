package nuke.idea.plugins.wizard;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ide.wizard.StepListener;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.UIUtil;
import nuke.idea.plugins.model.EqualsAndHashCodeType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static nuke.idea.plugins.model.EqualsAndHashCodeType.COMPARETO;
import static nuke.idea.plugins.model.EqualsAndHashCodeType.EQAULS;

public abstract class AbstractWizard<T extends Step> extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance("#nuke.idea.plugins.wizard");

    protected int myCurrentStep;
    protected final ArrayList<T> mySteps;
    private JButton myPreviousButton;
    private JButton myNextButton;
    private JButton myFinishButton;
    private JButton myCancelButton;
    private JButton myHelpButton;
    private JPanel myContentPanel;
    private TallImageComponent myIcon;
    private Component myCurrentStepComponent;
    private final Map<Component, String> myComponentToIdMap = new HashMap<Component, String>();
    private final StepListener myStepListener = new StepListener() {
        public void stateChanged() {
            updateStep();
        }
    };
    private EqualsAndHashCodeType type;

    public AbstractWizard(final String title, final Component dialogParent) {
        super(dialogParent, true);
        mySteps = new ArrayList<T>();
        initWizard(title);
    }

    public AbstractWizard(final String title, final Project project, EqualsAndHashCodeType type) {
        super(project, true);
        mySteps = new ArrayList<T>();
        this.type = type;
        initWizard(title);
    }

    private void initWizard(final String title) {
        setTitle(title);
        myCurrentStep = 0;
        myPreviousButton = new JButton(IdeBundle.message("button.wizard.previous"));
        myNextButton = new JButton(IdeBundle.message("button.wizard.next"));
        myFinishButton = new JButton(IdeBundle.message("button.finish"));
        myCancelButton = new JButton(CommonBundle.getCancelButtonText());
        myHelpButton = new JButton(CommonBundle.getHelpButtonText());
        myContentPanel = new JPanel(new CardLayout());

        myIcon = new TallImageComponent(null);

        JRootPane rootPane = getRootPane();
        if (rootPane != null) {        // it will be null in headless mode, i.e. tests
            rootPane.registerKeyboardAction(
                    new ActionListener() {
                        public void actionPerformed(final ActionEvent e) {
                            helpAction();
                        }
                    },
                    KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );

            rootPane.registerKeyboardAction(
                    new ActionListener() {
                        public void actionPerformed(final ActionEvent e) {
                            helpAction();
                        }
                    },
                    KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }
    }

    protected JComponent createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        panel.add(buttonPanel, BorderLayout.EAST);

        if (SystemInfo.isMac) {
            myHelpButton.putClientProperty("JButton.buttonType", "help");
            if (UIUtil.isUnderAquaLookAndFeel()) {
                myHelpButton.setText("");
            }

            JPanel leftPanel = new JPanel();
            if (ApplicationInfo.contextHelpAvailable()) {
                leftPanel.add(myHelpButton);
            }
            leftPanel.add(myCancelButton);

            panel.add(leftPanel, BorderLayout.WEST);

            buttonPanel.add(myFinishButton);
            if (mySteps.size() > 1) {
                buttonPanel.add(Box.createHorizontalStrut(5));
                buttonPanel.add(myPreviousButton);
                buttonPanel.add(Box.createHorizontalStrut(5));
                buttonPanel.add(myNextButton);
            }
        } else {
            if (mySteps.size() > 1) {
                buttonPanel.add(myPreviousButton);
                buttonPanel.add(Box.createHorizontalStrut(5));
                buttonPanel.add(myNextButton);
                buttonPanel.add(Box.createHorizontalStrut(5));
            }

            buttonPanel.add(myFinishButton);
            buttonPanel.add(Box.createHorizontalStrut(5));
            buttonPanel.add(myCancelButton);
            buttonPanel.add(Box.createHorizontalStrut(5));
            if (ApplicationInfo.contextHelpAvailable()) {
                buttonPanel.add(myHelpButton);
            }
        }

        myPreviousButton.setEnabled(false);
        myPreviousButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doPreviousAction();
            }
        });
        myNextButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                doNextAction();
            }
        });
        myFinishButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        // Commit data of current step and perform OK action
                        final Step currentStep = mySteps.get(myCurrentStep);
                        LOG.assertTrue(currentStep != null);
                        try {
                            currentStep._commit(true);
                            doOKAction();
                        } catch (final CommitStepException exc) {
                            String message = exc.getMessage();
                            if (message != null) {
                                Messages.showErrorDialog(myContentPanel, message);
                            }
                        }
                    }
                }
        );
        myCancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        doCancelAction();
                    }
                }
        );
        myHelpButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                helpAction();
            }
        });

        return panel;
    }

    private static class TallImageComponent extends OpaquePanel {
        private Icon myIcon;

        private TallImageComponent(Icon icon) {
            myIcon = icon;
        }

        @Override
        protected void paintChildren(Graphics g) {
            if (myIcon == null) return;

            final BufferedImage image = new BufferedImage(myIcon.getIconWidth(), myIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D gg = image.createGraphics();
            myIcon.paintIcon(this, gg, 0, 0);

            final Rectangle bounds = g.getClipBounds();
            int y = myIcon.getIconHeight() - 1;
            while (y < bounds.y + bounds.height) {
                g.drawImage(image,
                        bounds.x, y, bounds.x + bounds.width, y + 1,
                        0, myIcon.getIconHeight() - 1, bounds.width, myIcon.getIconHeight(), this);

                y++;
            }


            g.drawImage(image, 0, 0, this);
        }

        public void setIcon(Icon icon) {
            myIcon = icon;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(myIcon != null ? myIcon.getIconWidth() : 0, 0);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(myIcon != null ? myIcon.getIconWidth() : 0, 0);
        }
    }

    protected JComponent createCenterPanel() {
        final JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        iconPanel.add(myIcon, BorderLayout.CENTER);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(myContentPanel, BorderLayout.CENTER);

        JBRadioButton rbNuke = createNukeRadioButton();
        JBRadioButton rbGuava = createGuavaRadioButton();

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(rbNuke);
        buttonsPanel.add(rbGuava);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rbNuke);
        buttonGroup.add(rbGuava);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        setButtonSelectedByDefault(rbNuke, rbGuava);

        return panel;
    }

    private JBRadioButton createGuavaRadioButton() {
        JBRadioButton rbGuava = new JBRadioButton("Equals on BigDecimal");
        rbGuava.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                type = EQAULS;
            }
        });
        return rbGuava;
    }

    private JBRadioButton createNukeRadioButton() {
        JBRadioButton rbGuava = new JBRadioButton("CompareTo on BigDecimal");
        rbGuava.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                type = COMPARETO;
            }
        });
        return rbGuava;
    }

    private void setButtonSelectedByDefault(JBRadioButton rbNuke, JBRadioButton rbGuava) {
        if (type == COMPARETO) {
            rbNuke.setSelected(true);
        } else {
            rbGuava.setSelected(true);
        }
    }

    public int getCurrentStep() {
        return myCurrentStep;
    }

    protected T getCurrentStepObject() {
        return mySteps.get(myCurrentStep);
    }

    public void addStep(@NotNull final T step) {
        mySteps.add(step);

        if (step instanceof StepAdapter) {
            ((StepAdapter) step).registerStepListener(myStepListener);
        }
        // card layout is used
        final Component component = step.getComponent();
        if (component != null) {
            addStepComponent(component);
        }

        if (mySteps.size() > 1) {
            myFinishButton.setText(IdeBundle.message("button.finish"));
        } else {
            myFinishButton.setText(IdeBundle.message("button.ok"));
        }
    }

    protected void init() {
        super.init();
        updateStep();
    }


    private String addStepComponent(final Component component) {
        String id = myComponentToIdMap.get(component);
        if (id == null) {
            id = Integer.toString(myComponentToIdMap.size());
            myComponentToIdMap.put(component, id);
            myContentPanel.add(component, id);
        }
        return id;
    }

    private void showStepComponent(final Component component) {
        String id = myComponentToIdMap.get(component);
        if (id == null) {
            id = addStepComponent(component);
            myContentPanel.revalidate();
            myContentPanel.repaint();
        }
        ((CardLayout) myContentPanel.getLayout()).show(myContentPanel, id);
    }

    protected void doPreviousAction() {
        // Commit data of current step
        final Step currentStep = mySteps.get(myCurrentStep);
        LOG.assertTrue(currentStep != null);
        try {
            currentStep._commit(false);
        } catch (final CommitStepException exc) {
            Messages.showErrorDialog(
                    myContentPanel,
                    exc.getMessage()
            );
            return;
        }

        myCurrentStep = getPreviousStep(myCurrentStep);
        updateStep();
    }

    protected void doNextAction() {
        // Commit data of current step
        final Step currentStep = mySteps.get(myCurrentStep);
        boolean lastStep = getCurrentStep() == getNextStep(getCurrentStep());
        LOG.assertTrue(currentStep != null);
        try {
            currentStep._commit(false);
            if (SystemInfo.isMac && lastStep) {
                doOKAction();
            }
        } catch (final CommitStepException exc) {
            Messages.showErrorDialog(
                    myContentPanel,
                    exc.getMessage()
            );
            return;
        }

        if (!SystemInfo.isMac || !lastStep) {
            myCurrentStep = getNextStep(myCurrentStep);
            updateStep();
        }
    }

    /**
     * override this to provide alternate step order
     *
     * @param step index
     * @return the next step's index
     */
    protected int getNextStep(int step) {
        final int stepCount = mySteps.size();
        if (++step >= stepCount) {
            step = stepCount - 1;
        }
        return step;
    }

    protected final int getNextStep() {
        return getNextStep(getCurrentStep());
    }

    protected T getNextStepObject() {
        int step = getNextStep();
        return mySteps.get(step);
    }

    /**
     * override this to provide alternate step order
     *
     * @param step index
     * @return the previous step's index
     */
    protected int getPreviousStep(int step) {
        if (--step < 0) {
            step = 0;
        }
        return step;
    }

    protected final int getPreviousStep() {
        return getPreviousStep(getCurrentStep());
    }

    protected void updateStep() {
        if (mySteps.size() == 0) {
            return;
        }

        final Step step = mySteps.get(myCurrentStep);
        LOG.assertTrue(step != null);
        step._init();
        myCurrentStepComponent = step.getComponent();
        LOG.assertTrue(myCurrentStepComponent != null);
        showStepComponent(myCurrentStepComponent);

        myIcon.setIcon(step.getIcon());

        if (SystemInfo.isMac && myCurrentStep == mySteps.size() - 1) {
            myFinishButton.setVisible(false);
            myNextButton.setText(IdeBundle.message("button.finish"));
            myNextButton.setVisible(true);
            myNextButton.setEnabled(myFinishButton.isEnabled());
        } else {
            myNextButton.setText(IdeBundle.message("button.wizard.next"));
            myFinishButton.setVisible(true);
            myNextButton.setEnabled(mySteps.size() == 1 || myCurrentStep < mySteps.size() - 1);
            myNextButton.setMnemonic('N');
        }

        myPreviousButton.setEnabled(myCurrentStep > 0);
    }

    protected JButton getNextButton() {
        return myNextButton;
    }

    protected JButton getPreviousButton() {
        return myPreviousButton;
    }

    protected JButton getFinishButton() {
        return myFinishButton;
    }

    public Component getCurrentStepComponent() {
        return myCurrentStepComponent;
    }

    protected void helpAction() {
        HelpManager.getInstance().invokeHelp(getHelpID());
    }

    @Override
    protected void doHelpAction() {
        HelpManager.getInstance().invokeHelp(getHelpID());
    }

    protected int getNumberOfSteps() {
        return mySteps.size();
    }

    @Nullable
    @NonNls
    protected abstract String getHelpID();

    protected boolean isCurrentStep(final T step) {
        return step != null && getCurrentStepComponent() == step.getComponent();
    }

    public EqualsAndHashCodeType getType() {
        return type;
    }
}

