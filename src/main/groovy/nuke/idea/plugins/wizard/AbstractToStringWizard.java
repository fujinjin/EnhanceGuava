package nuke.idea.plugins.wizard;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ide.wizard.StepListener;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractToStringWizard<T extends Step> extends DialogWrapper {

    private JButton myFinishButton;
    private JButton myCancelButton;
    private JButton myHelpButton;
    private JPanel myContentPanel;
    private TallImageComponent myIcon;
    private final Map<Component, String> myComponentToIdMap = new HashMap<Component, String>();
    private final StepListener myStepListener = new StepListener() {
        public void stateChanged() {
            updateStep();
        }
    };

    public AbstractToStringWizard(final String title, final Component dialogParent) {
        super(dialogParent, true);
        initWizard(title);
    }

    public AbstractToStringWizard(final String title, final Project project) {
        super(project, true);
        initWizard(title);
    }

    private void initWizard(final String title) {
        setTitle(title);
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


        buttonPanel.add(myFinishButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(myCancelButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        if (ApplicationInfo.contextHelpAvailable()) {
            buttonPanel.add(myHelpButton);
        }
        myFinishButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        // Commit data of current step and perform OK action
                        doOKAction();
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
        JPanel buttonsPanel = new JPanel();
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        return panel;
    }

    public void addStep(@NotNull final T step) {

        if (step instanceof StepAdapter) {
            ((StepAdapter) step).registerStepListener(myStepListener);
        }
        // card layout is used
        final Component component = step.getComponent();
        if (component != null) {
            addStepComponent(component);
        }
        myFinishButton.setText(IdeBundle.message("button.finish"));
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

    protected void updateStep() {

        myFinishButton.setVisible(true);
    }

    protected JButton getFinishButton() {
        return myFinishButton;
    }

    protected void helpAction() {
        HelpManager.getInstance().invokeHelp(getHelpID());
    }

    @Override
    protected void doHelpAction() {
        HelpManager.getInstance().invokeHelp(getHelpID());
    }

    @Nullable
    @NonNls
    protected abstract String getHelpID();

}


