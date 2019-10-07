package eu.doppel_helix.netbeans.mantisintegration.issue;

import biz.futureware.mantisconnect.TagData;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingWorker;
import javax.xml.rpc.ServiceException;

public class AddTagDialog extends javax.swing.JDialog implements ActionListener {
    private final static Logger logger = Logger.getLogger(AddTagDialog.class.getName());
    private final MantisIssue issue;

    @SuppressWarnings("LeakingThisInConstructor")
    public AddTagDialog(java.awt.Frame parent, final MantisIssue issue) {
        super(parent, true);
        setLocationByPlatform(true);
        initComponents();
        this.issue = issue;
        final DefaultComboBoxModel<String> types = new DefaultComboBoxModel<>();
        new SwingWorker<List<TagData>,Object>() {
            @Override
            protected List<TagData> doInBackground() throws Exception {
                return issue.getMantisRepository().getMasterData().getTags();
            }

            @Override
            protected void done() {
                try {
                    for (TagData tag : get()) {
                        types.addElement(tag.getName());
                    }
                } catch (ExecutionException ex) {
                    // Log on Level Info => don't force display, as situation is not fatal
                    // though it will surely get fatal ...
                    logger.log(Level.INFO, "Failed to retrieve taglist", ex.getCause());
                } catch (InterruptedException ex) {
                    logger.log(Level.INFO, "Failed to retrieve taglist", ex);
                }
                tagsComboBox.setEnabled(true);
            }

        }.execute();
        tagsComboBox.setModel(types);
        tagsComboBox.setSelectedItem("");
        tagsComboBox.setEnabled(false);
        closeButton.setActionCommand("cancel");
        closeButton.addActionListener(this);
        okButton.addActionListener(this);
        okButton.setActionCommand("ok");
        okButton.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("cancel".equals(e.getActionCommand())) {
            AddTagDialog.this.dispose();
        } else if ("ok".equals(e.getActionCommand())) {
            if (checkValidity()) {
                final MantisRepository mr = issue.getMantisRepository();
                final List<String> tags = new ArrayList<>();
                for (String tag : ((String) tagsComboBox.getSelectedItem()).split(",")) {
                    if (!tag.trim().isEmpty()) {
                        tags.add(tag.trim());
                    }
                }
                mr.getRequestProcessor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            issue.addTag(tags.toArray(new String[0]));
                        } catch (ServiceException | RemoteException | RuntimeException ex) {
                            mr.getExceptionHandler()
                                    .handleException(logger, "Failed to add tag to issue", ex);
                        }
                    }
                });
            }
            AddTagDialog.this.dispose();
        }
    }

    protected boolean checkValidity() {
        return (! ((String) tagsComboBox.getSelectedItem()).trim().isEmpty());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tagsLabel = new javax.swing.JLabel();
        tagsComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(AddTagDialog.class, "AddTagDialog.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(325, 125));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(tagsLabel, org.openide.util.NbBundle.getMessage(AddTagDialog.class, "AddTagDialog.tagsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(tagsLabel, gridBagConstraints);

        tagsComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(tagsComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(AddTagDialog.class, "AddTagDialog.closeButton.text")); // NOI18N
        jPanel1.add(closeButton);

        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(AddTagDialog.class, "AddTagDialog.okButton.text")); // NOI18N
        jPanel1.add(okButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(filler1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox tagsComboBox;
    private javax.swing.JLabel tagsLabel;
    // End of variables declaration//GEN-END:variables
}
