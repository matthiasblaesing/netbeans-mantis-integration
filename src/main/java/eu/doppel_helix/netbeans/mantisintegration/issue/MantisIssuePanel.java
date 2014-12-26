/*
 * Copyright 2014 Matthias Bläsing <mblaesing@doppel-helix.eu>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.doppel_helix.netbeans.mantisintegration.issue;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.swing.AccountDataListCellRenderer;
import eu.doppel_helix.netbeans.mantisintegration.swing.BusyPanel;
import eu.doppel_helix.netbeans.mantisintegration.swing.DelegatingBaseLineJPanel;
import eu.doppel_helix.netbeans.mantisintegration.swing.DispatchingListener;
import eu.doppel_helix.netbeans.mantisintegration.swing.FullSizeLayout;
import eu.doppel_helix.netbeans.mantisintegration.swing.ObjectRefListCellRenderer;
import eu.doppel_helix.netbeans.mantisintegration.swing.PriorityListCellRenderer;
import eu.doppel_helix.netbeans.mantisintegration.swing.ProjectListCellRenderer;
import eu.doppel_helix.netbeans.mantisintegration.swing.StringNullSaveListCellRenderer;
import eu.doppel_helix.netbeans.mantisintegration.swing.TimeFormatterFactory;
import eu.doppel_helix.netbeans.mantisintegration.swing.VerticalScrollPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;

public class MantisIssuePanel extends javax.swing.JLayeredPane {

    private final Map<BigInteger, Color> colorMap = Mantis.getInstance().getStatusColorMap();
    JComponent waitPanel;

    private final static int CUSTOM_ROW_START = 8;
    private final static int CUSTOM_ROW_END = 17;
    private final List<CustomFieldComponent> customFields = new ArrayList<>();
    
    public MantisIssuePanel() {        
        initComponents();
        scrollablePane.getVerticalScrollBar().setUnitIncrement(20);
        waitPanel = new BusyPanel();
        Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        descriptionScrollPane.setCursor(textCursor);
        descriptionScrollPane.addMouseListener(new DispatchingListener(descriptionEditorPane));
        stepsToReproduceScrollPane.setCursor(textCursor);
        stepsToReproduceScrollPane.addMouseListener(new DispatchingListener(stepsToReproduceEditorPane));
        additionalInformationScrollPane.setCursor(textCursor);
        additionalInformationScrollPane.addMouseListener(new DispatchingListener(additionalInformationEditorPane));
        addNoteScrollPane.setCursor(textCursor);
        addNoteScrollPane.addMouseListener(new DispatchingListener(addNoteEditorPane));
        this.setLayout(new FullSizeLayout());
        this.add(waitPanel, JLayeredPane.MODAL_LAYER);
    }

    public void clearCustomFields() {
        for (CustomFieldComponent cfc: customFields) {
            innerPanel.remove(cfc);
            innerPanel.remove(cfc.getLabel());
        }
        customFields.clear();
    }
    
    public void addCustomField(CustomFieldComponent cfc) {
        int row = CUSTOM_ROW_START + customFields.size();
        if(row > CUSTOM_ROW_END) {
            throw new IllegalStateException("Maximum custom field count reached");
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.weightx = 0;
        
        innerPanel.add(cfc.getLabel(), gbc);
        
        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 7;
        
        innerPanel.add(cfc, gbc);
        customFields.add(cfc);
    }
    
    public CustomFieldComponent getCustomFieldById(BigInteger id) {
        for(CustomFieldComponent cfc: customFields) {
            if(cfc.getCustomFieldDefinitionData().getField().getId().equals(id)) {
                return cfc;
            }
        }
        return null;
    }
    
    public List<CustomFieldComponent> getCustomFields() {
        return Collections.unmodifiableList(customFields);
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

        scrollablePane = new javax.swing.JScrollPane();
        innerPanel = new VerticalScrollPane();
        headerPanel = new javax.swing.JPanel();
        issueHeader = new javax.swing.JLabel();
        headerButtonsPanel = new javax.swing.JPanel();
        refreshLinkButton = new org.jdesktop.swingx.JXHyperlink();
        seperatorLabel = new javax.swing.JLabel();
        openIssueWebbrowserLinkButton = new org.jdesktop.swingx.JXHyperlink();
        subheaderPanel = new javax.swing.JPanel();
        createdLabel = new javax.swing.JLabel();
        createdValueLabel = new javax.swing.JLabel();
        updatedLabel = new javax.swing.JLabel();
        updatedValueLabel = new javax.swing.JLabel();
        reporterLabel = new javax.swing.JLabel();
        reporterValueLabel = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();
        summaryLabel = new javax.swing.JLabel();
        summaryTextField = new javax.swing.JTextField();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionEditorPane = new eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane();
        stepsToReproduceScrollPane = new javax.swing.JScrollPane();
        stepsToReproduceEditorPane = new eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane();
        additionalInformationScrollPane = new javax.swing.JScrollPane();
        additionalInformationEditorPane = new eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane();
        severityLabel = new javax.swing.JLabel();
        assignedToLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        categoryComboBox = new javax.swing.JComboBox();
        severityComboBox = new javax.swing.JComboBox();
        reproducibilityComboBox = new javax.swing.JComboBox();
        viewStatusLabel = new javax.swing.JLabel();
        viewStatusComboBox = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        priorityComboBox = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionComboBox = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();
        additionalInformationLabel = new javax.swing.JLabel();
        reproducibilityLabel = new javax.swing.JLabel();
        assignedToComboBox = new javax.swing.JComboBox();
        categoryLabel = new javax.swing.JLabel();
        stepsToReproduceLabel = new javax.swing.JLabel();
        projectionLabel = new javax.swing.JLabel();
        projectionComboBox = new javax.swing.JComboBox();
        etaLabel = new javax.swing.JLabel();
        etaComboBox = new javax.swing.JComboBox();
        osLabel = new javax.swing.JLabel();
        osVersionLabel = new javax.swing.JLabel();
        platformLabel = new javax.swing.JLabel();
        buildLabel = new javax.swing.JLabel();
        buildTextField = new javax.swing.JTextField();
        platformTextField = new javax.swing.JTextField();
        osTextField = new javax.swing.JTextField();
        osVersionTextField = new javax.swing.JTextField();
        relationsLabel = new javax.swing.JLabel();
        relationsPanel = new DelegatingBaseLineJPanel();
        tagsLabel = new javax.swing.JLabel();
        tagsPanel = new DelegatingBaseLineJPanel();
        attachmentLabel = new javax.swing.JLabel();
        attachmentPanel = new eu.doppel_helix.netbeans.mantisintegration.swing.DelegatingBaseLineJPanel();
        buttonPanel1 = new javax.swing.JPanel();
        addIssueButton = new javax.swing.JButton();
        updateIssueButton = new javax.swing.JButton();
        notesOuterPanel = new javax.swing.JPanel();
        notesPanel = new javax.swing.JPanel();
        addNotesPanel = new javax.swing.JPanel();
        addNoteLabel = new javax.swing.JLabel();
        buttonPanel2 = new javax.swing.JPanel();
        addNoteViewStateComboBox = new javax.swing.JComboBox();
        addNoteButton = new javax.swing.JButton();
        addNoteScrollPane = new javax.swing.JScrollPane();
        addNoteEditorPane = new eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane();
        timetrackLabel = new javax.swing.JLabel();
        timetrackInput = new javax.swing.JFormattedTextField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        targetVersionLabel = new javax.swing.JLabel();
        targetVersionComboBox = new javax.swing.JComboBox();
        versionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox();
        fixVersionLabel = new javax.swing.JLabel();
        fixVersionComboBox = new javax.swing.JComboBox();

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        scrollablePane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollablePane.setOpaque(false);

        innerPanel.setBackground(new java.awt.Color(255, 255, 255));
        innerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        innerPanel.setAlignmentY(0.0F);
        innerPanel.setLayout(new java.awt.GridBagLayout());

        headerPanel.setOpaque(false);
        headerPanel.setLayout(new java.awt.BorderLayout());
        headerPanel.setLayout(new CustomLayout());

        issueHeader.setFont(issueHeader.getFont().deriveFont(issueHeader.getFont().getStyle() | java.awt.Font.BOLD, AffineTransform.getScaleInstance(1.7, 1.7)));
        org.openide.awt.Mnemonics.setLocalizedText(issueHeader, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.issueHeader.text")); // NOI18N
        headerPanel.add(issueHeader, java.awt.BorderLayout.CENTER);

        headerButtonsPanel.setOpaque(false);

        refreshLinkButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        org.openide.awt.Mnemonics.setLocalizedText(refreshLinkButton, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.refreshLinkButton.text")); // NOI18N
        refreshLinkButton.setActionCommand(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.refreshLinkButton.actionCommand")); // NOI18N
        refreshLinkButton.setFont(refreshLinkButton.getFont().deriveFont(refreshLinkButton.getFont().getStyle() & ~java.awt.Font.BOLD));
        headerButtonsPanel.add(refreshLinkButton);

        seperatorLabel.setBackground(new java.awt.Color(0, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(seperatorLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.seperatorLabel.text")); // NOI18N
        seperatorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        seperatorLabel.setMaximumSize(new java.awt.Dimension(1, 15));
        seperatorLabel.setMinimumSize(new java.awt.Dimension(1, 15));
        seperatorLabel.setPreferredSize(new java.awt.Dimension(1, 15));
        headerButtonsPanel.add(seperatorLabel);

        openIssueWebbrowserLinkButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        org.openide.awt.Mnemonics.setLocalizedText(openIssueWebbrowserLinkButton, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.openIssueWebbrowserLinkButton.text")); // NOI18N
        openIssueWebbrowserLinkButton.setActionCommand(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.openIssueWebbrowserLinkButton.actionCommand")); // NOI18N
        openIssueWebbrowserLinkButton.setFont(openIssueWebbrowserLinkButton.getFont().deriveFont(openIssueWebbrowserLinkButton.getFont().getStyle() & ~java.awt.Font.BOLD));
        headerButtonsPanel.add(openIssueWebbrowserLinkButton);

        headerPanel.add(headerButtonsPanel, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(headerPanel, gridBagConstraints);

        subheaderPanel.setOpaque(false);

        createdLabel.setFont(createdLabel.getFont().deriveFont(createdLabel.getFont().getSize()-2f));
        createdLabel.setForeground(new java.awt.Color(128, 128, 128));
        org.openide.awt.Mnemonics.setLocalizedText(createdLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.createdLabel.text")); // NOI18N
        subheaderPanel.add(createdLabel);

        createdValueLabel.setFont(createdValueLabel.getFont().deriveFont(createdValueLabel.getFont().getSize()-2f));
        createdValueLabel.setForeground(new java.awt.Color(22, 75, 123));
        org.openide.awt.Mnemonics.setLocalizedText(createdValueLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.createdValueLabel.text")); // NOI18N
        subheaderPanel.add(createdValueLabel);

        updatedLabel.setFont(updatedLabel.getFont().deriveFont(updatedLabel.getFont().getSize()-2f));
        updatedLabel.setForeground(new java.awt.Color(128, 128, 128));
        org.openide.awt.Mnemonics.setLocalizedText(updatedLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.updatedLabel.text")); // NOI18N
        subheaderPanel.add(updatedLabel);

        updatedValueLabel.setFont(updatedValueLabel.getFont().deriveFont(updatedValueLabel.getFont().getSize()-2f));
        updatedValueLabel.setForeground(new java.awt.Color(22, 75, 123));
        org.openide.awt.Mnemonics.setLocalizedText(updatedValueLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.updatedValueLabel.text")); // NOI18N
        subheaderPanel.add(updatedValueLabel);

        reporterLabel.setFont(reporterLabel.getFont().deriveFont(reporterLabel.getFont().getSize()-2f));
        reporterLabel.setForeground(new java.awt.Color(128, 128, 128));
        org.openide.awt.Mnemonics.setLocalizedText(reporterLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.reporterLabel.text")); // NOI18N
        subheaderPanel.add(reporterLabel);

        reporterValueLabel.setFont(reporterValueLabel.getFont().deriveFont(reporterValueLabel.getFont().getSize()-2f));
        reporterValueLabel.setForeground(new java.awt.Color(22, 75, 123));
        org.openide.awt.Mnemonics.setLocalizedText(reporterValueLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.reporterValueLabel.text")); // NOI18N
        subheaderPanel.add(reporterValueLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(subheaderPanel, gridBagConstraints);

        projectComboBox.setActionCommand(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.projectComboBox.actionCommand")); // NOI18N
        projectComboBox.setPrototypeDisplayValue("XXXXXXXX");
        projectComboBox.setRenderer(new ProjectListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(projectComboBox, gridBagConstraints);

        summaryLabel.setFont(summaryLabel.getFont().deriveFont(summaryLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.summaryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(summaryLabel, gridBagConstraints);

        summaryTextField.setText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.summaryTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(summaryTextField, gridBagConstraints);

        descriptionScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        descriptionScrollPane.setMinimumSize(new java.awt.Dimension(10, 100));
        descriptionScrollPane.setPreferredSize(new java.awt.Dimension(10, 100));

        descriptionEditorPane.setMinimumSize(new java.awt.Dimension(6, 95));
        descriptionEditorPane.setScrollableTracksViewportWidth(true);
        descriptionScrollPane.setViewportView(descriptionEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(descriptionScrollPane, gridBagConstraints);
        descriptionScrollPane.getViewport().setBackground(descriptionScrollPane.getBackground());

        stepsToReproduceScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        stepsToReproduceScrollPane.setMinimumSize(new java.awt.Dimension(10, 75));
        stepsToReproduceScrollPane.setPreferredSize(new java.awt.Dimension(10, 75));

        stepsToReproduceEditorPane.setMinimumSize(new java.awt.Dimension(6, 70));
        stepsToReproduceEditorPane.setScrollableTracksViewportWidth(true);
        stepsToReproduceScrollPane.setViewportView(stepsToReproduceEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(stepsToReproduceScrollPane, gridBagConstraints);
        stepsToReproduceScrollPane.getViewport().setBackground(stepsToReproduceScrollPane.getBackground());

        additionalInformationScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        additionalInformationScrollPane.setMinimumSize(new java.awt.Dimension(10, 75));
        additionalInformationScrollPane.setPreferredSize(new java.awt.Dimension(10, 75));

        additionalInformationEditorPane.setMinimumSize(new java.awt.Dimension(6, 70));
        additionalInformationEditorPane.setScrollableTracksViewportWidth(true);
        additionalInformationScrollPane.setViewportView(additionalInformationEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(additionalInformationScrollPane, gridBagConstraints);
        additionalInformationScrollPane.getViewport().setBackground(additionalInformationScrollPane.getBackground());

        severityLabel.setFont(severityLabel.getFont().deriveFont(severityLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.severityLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(severityLabel, gridBagConstraints);

        assignedToLabel.setFont(assignedToLabel.getFont().deriveFont(assignedToLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(assignedToLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.assignedToLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(assignedToLabel, gridBagConstraints);

        projectLabel.setFont(projectLabel.getFont().deriveFont(projectLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.projectLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(projectLabel, gridBagConstraints);

        categoryComboBox.setPrototypeDisplayValue("XXXXXXXX");
        categoryComboBox.setRenderer(new StringNullSaveListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(categoryComboBox, gridBagConstraints);

        severityComboBox.setPrototypeDisplayValue("XXXXXXXX");
        severityComboBox.setRenderer(new ObjectRefListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(severityComboBox, gridBagConstraints);

        reproducibilityComboBox.setPrototypeDisplayValue("XXXXXXXX");
        reproducibilityComboBox.setRenderer(new ObjectRefListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(reproducibilityComboBox, gridBagConstraints);

        viewStatusLabel.setFont(viewStatusLabel.getFont().deriveFont(viewStatusLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(viewStatusLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.viewStatusLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(viewStatusLabel, gridBagConstraints);

        viewStatusComboBox.setPrototypeDisplayValue("XXXXXXXX");
        viewStatusComboBox.setRenderer(new ObjectRefListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(viewStatusComboBox, gridBagConstraints);

        priorityLabel.setFont(priorityLabel.getFont().deriveFont(priorityLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.priorityLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(priorityLabel, gridBagConstraints);

        priorityComboBox.setPrototypeDisplayValue("XXXXXXXX");
        priorityComboBox.setRenderer(new PriorityListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(priorityComboBox, gridBagConstraints);

        resolutionLabel.setFont(resolutionLabel.getFont().deriveFont(resolutionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.resolutionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(resolutionLabel, gridBagConstraints);

        resolutionComboBox.setPrototypeDisplayValue("XXXXXXXX");
        resolutionComboBox.setRenderer(new ObjectRefListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(resolutionComboBox, gridBagConstraints);

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.statusLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(statusLabel, gridBagConstraints);

        statusComboBox.setPrototypeDisplayValue("XXXXXXXX");
        statusComboBox.setRenderer(new eu.doppel_helix.netbeans.mantisintegration.swing.StatusListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(statusComboBox, gridBagConstraints);
        statusComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object value = statusComboBox.getSelectedItem();

                Color color = null;

                if(value instanceof ObjectRef) {
                    ObjectRef or = (ObjectRef)value;
                    BigInteger level = or.getId();
                    color = colorMap.get(level);
                } else if (value == null) {
                    value = " ";
                }

                if(color == null) {
                    color = Color.WHITE;
                }

                statusComboBox.setBackground(color);
            }
        });

        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(descriptionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.descriptionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(descriptionLabel, gridBagConstraints);

        additionalInformationLabel.setFont(additionalInformationLabel.getFont().deriveFont(additionalInformationLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(additionalInformationLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.additionalInformationLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(additionalInformationLabel, gridBagConstraints);

        reproducibilityLabel.setFont(reproducibilityLabel.getFont().deriveFont(reproducibilityLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(reproducibilityLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.reproducibilityLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(reproducibilityLabel, gridBagConstraints);

        assignedToComboBox.setPrototypeDisplayValue("XXXXXXXX");
        assignedToComboBox.setRenderer(new AccountDataListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(assignedToComboBox, gridBagConstraints);

        categoryLabel.setFont(categoryLabel.getFont().deriveFont(categoryLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.categoryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(categoryLabel, gridBagConstraints);

        stepsToReproduceLabel.setFont(stepsToReproduceLabel.getFont().deriveFont(stepsToReproduceLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(stepsToReproduceLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.stepsToReproduceLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(stepsToReproduceLabel, gridBagConstraints);

        projectionLabel.setFont(projectionLabel.getFont().deriveFont(projectionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(projectionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.projectionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(projectionLabel, gridBagConstraints);

        projectionComboBox.setPrototypeDisplayValue("XXXXXXXX");
        projectionComboBox.setRenderer(new ObjectRefListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(projectionComboBox, gridBagConstraints);

        etaLabel.setFont(etaLabel.getFont().deriveFont(etaLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(etaLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.etaLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(etaLabel, gridBagConstraints);

        etaComboBox.setPrototypeDisplayValue("XXXXXXXX");
        etaComboBox.setRenderer(new ObjectRefListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(etaComboBox, gridBagConstraints);

        osLabel.setFont(osLabel.getFont().deriveFont(osLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(osLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.osLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(osLabel, gridBagConstraints);

        osVersionLabel.setFont(osVersionLabel.getFont().deriveFont(osVersionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(osVersionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.osVersionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(osVersionLabel, gridBagConstraints);

        platformLabel.setFont(platformLabel.getFont().deriveFont(platformLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(platformLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.platformLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(platformLabel, gridBagConstraints);

        buildLabel.setFont(buildLabel.getFont().deriveFont(buildLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(buildLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.buildLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(buildLabel, gridBagConstraints);

        buildTextField.setColumns(20);
        buildTextField.setText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.buildTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(buildTextField, gridBagConstraints);

        platformTextField.setColumns(20);
        platformTextField.setText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.platformTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(platformTextField, gridBagConstraints);

        osTextField.setColumns(20);
        osTextField.setText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.osTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(osTextField, gridBagConstraints);

        osVersionTextField.setColumns(20);
        osVersionTextField.setText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.osVersionTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(osVersionTextField, gridBagConstraints);

        relationsLabel.setFont(relationsLabel.getFont().deriveFont(relationsLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(relationsLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.relationsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(relationsLabel, gridBagConstraints);

        relationsPanel.setOpaque(false);
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0);
        flowLayout1.setAlignOnBaseline(true);
        relationsPanel.setLayout(flowLayout1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(relationsPanel, gridBagConstraints);

        tagsLabel.setFont(tagsLabel.getFont().deriveFont(tagsLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(tagsLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.tagsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(tagsLabel, gridBagConstraints);

        tagsPanel.setOpaque(false);
        java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0);
        flowLayout2.setAlignOnBaseline(true);
        tagsPanel.setLayout(flowLayout2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(tagsPanel, gridBagConstraints);

        attachmentLabel.setFont(attachmentLabel.getFont().deriveFont(attachmentLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(attachmentLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.attachmentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(attachmentLabel, gridBagConstraints);

        attachmentPanel.setOpaque(false);
        attachmentPanel.setLayout(new javax.swing.BoxLayout(attachmentPanel, javax.swing.BoxLayout.PAGE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(attachmentPanel, gridBagConstraints);

        buttonPanel1.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(addIssueButton, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.addIssueButton.text")); // NOI18N
        addIssueButton.setActionCommand(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.addIssueButton.actionCommand")); // NOI18N
        buttonPanel1.add(addIssueButton);

        org.openide.awt.Mnemonics.setLocalizedText(updateIssueButton, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.updateIssueButton.text")); // NOI18N
        updateIssueButton.setActionCommand(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.updateIssueButton.actionCommand")); // NOI18N
        buttonPanel1.add(updateIssueButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        innerPanel.add(buttonPanel1, gridBagConstraints);

        notesOuterPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        notesOuterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.notesOuterPanel.border.title"))); // NOI18N
        notesOuterPanel.setLayout(new javax.swing.BoxLayout(notesOuterPanel, javax.swing.BoxLayout.PAGE_AXIS));

        notesPanel.setBackground(new java.awt.Color(255, 255, 255));
        notesPanel.setOpaque(false);
        notesPanel.setLayout(new javax.swing.BoxLayout(notesPanel, javax.swing.BoxLayout.PAGE_AXIS));
        notesOuterPanel.add(notesPanel);

        addNotesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        addNotesPanel.setAlignmentX(0.0F);
        addNotesPanel.setOpaque(false);
        addNotesPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addNoteLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.addNoteLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addNotesPanel.add(addNoteLabel, gridBagConstraints);

        buttonPanel2.setOpaque(false);

        addNoteViewStateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addNoteViewStateComboBox.setRenderer(new ObjectRefListCellRenderer());
        buttonPanel2.add(addNoteViewStateComboBox);

        org.openide.awt.Mnemonics.setLocalizedText(addNoteButton, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.addNoteButton.text")); // NOI18N
        addNoteButton.setActionCommand(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.addNoteButton.actionCommand")); // NOI18N
        buttonPanel2.add(addNoteButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addNotesPanel.add(buttonPanel2, gridBagConstraints);

        addNoteScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        addNoteScrollPane.setMinimumSize(new java.awt.Dimension(10, 75));
        addNoteScrollPane.setPreferredSize(new java.awt.Dimension(10, 75));

        addNoteEditorPane.setMinimumSize(new java.awt.Dimension(6, 70));
        addNoteEditorPane.setScrollableTracksViewportWidth(true);
        addNoteScrollPane.setViewportView(addNoteEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        addNotesPanel.add(addNoteScrollPane, gridBagConstraints);
        addNoteScrollPane.getViewport().setBackground(addNoteScrollPane.getBackground());

        org.openide.awt.Mnemonics.setLocalizedText(timetrackLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.timetrackLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        addNotesPanel.add(timetrackLabel, gridBagConstraints);

        timetrackInput.setFormatterFactory(new TimeFormatterFactory());
        timetrackInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        timetrackInput.setText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.timetrackInput.text")); // NOI18N
        timetrackInput.setMinimumSize(new java.awt.Dimension(4, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        addNotesPanel.add(timetrackInput, gridBagConstraints);

        notesOuterPanel.add(addNotesPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(notesOuterPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        innerPanel.add(filler1, gridBagConstraints);

        targetVersionLabel.setFont(targetVersionLabel.getFont().deriveFont(targetVersionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(targetVersionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.targetVersionLabel.text")); // NOI18N
        targetVersionLabel.setToolTipText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.targetVersionLabel.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(targetVersionLabel, gridBagConstraints);

        targetVersionComboBox.setPrototypeDisplayValue("XXXXXXXX");
        targetVersionComboBox.setRenderer(new StringNullSaveListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(targetVersionComboBox, gridBagConstraints);

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.versionLabel.text")); // NOI18N
        versionLabel.setToolTipText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.versionLabel.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(versionLabel, gridBagConstraints);

        versionComboBox.setPrototypeDisplayValue("XXXXXXXX");
        versionComboBox.setRenderer(new StringNullSaveListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(versionComboBox, gridBagConstraints);

        fixVersionLabel.setFont(fixVersionLabel.getFont().deriveFont(fixVersionLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(fixVersionLabel, org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.fixVersionLabel.text")); // NOI18N
        fixVersionLabel.setToolTipText(org.openide.util.NbBundle.getMessage(MantisIssuePanel.class, "MantisIssuePanel.fixVersionLabel.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(fixVersionLabel, gridBagConstraints);

        fixVersionComboBox.setPrototypeDisplayValue("XXXXXXXX");
        fixVersionComboBox.setRenderer(new StringNullSaveListCellRenderer());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        innerPanel.add(fixVersionComboBox, gridBagConstraints);

        scrollablePane.setViewportView(innerPanel);

        add(scrollablePane);
        scrollablePane.setBounds(0, 0, 1314, 765);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton addIssueButton;
    javax.swing.JButton addNoteButton;
    eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane addNoteEditorPane;
    javax.swing.JLabel addNoteLabel;
    javax.swing.JScrollPane addNoteScrollPane;
    javax.swing.JComboBox addNoteViewStateComboBox;
    javax.swing.JPanel addNotesPanel;
    eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane additionalInformationEditorPane;
    javax.swing.JLabel additionalInformationLabel;
    javax.swing.JScrollPane additionalInformationScrollPane;
    javax.swing.JComboBox assignedToComboBox;
    javax.swing.JLabel assignedToLabel;
    javax.swing.JLabel attachmentLabel;
    eu.doppel_helix.netbeans.mantisintegration.swing.DelegatingBaseLineJPanel attachmentPanel;
    javax.swing.JLabel buildLabel;
    javax.swing.JTextField buildTextField;
    javax.swing.JPanel buttonPanel1;
    javax.swing.JPanel buttonPanel2;
    javax.swing.JComboBox categoryComboBox;
    javax.swing.JLabel categoryLabel;
    javax.swing.JLabel createdLabel;
    javax.swing.JLabel createdValueLabel;
    eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane descriptionEditorPane;
    javax.swing.JLabel descriptionLabel;
    javax.swing.JScrollPane descriptionScrollPane;
    javax.swing.JComboBox etaComboBox;
    javax.swing.JLabel etaLabel;
    javax.swing.Box.Filler filler1;
    javax.swing.JComboBox fixVersionComboBox;
    javax.swing.JLabel fixVersionLabel;
    javax.swing.JPanel headerButtonsPanel;
    javax.swing.JPanel headerPanel;
    javax.swing.JPanel innerPanel;
    javax.swing.JLabel issueHeader;
    javax.swing.JPanel notesOuterPanel;
    javax.swing.JPanel notesPanel;
    org.jdesktop.swingx.JXHyperlink openIssueWebbrowserLinkButton;
    javax.swing.JLabel osLabel;
    javax.swing.JTextField osTextField;
    javax.swing.JLabel osVersionLabel;
    javax.swing.JTextField osVersionTextField;
    javax.swing.JLabel platformLabel;
    javax.swing.JTextField platformTextField;
    javax.swing.JComboBox priorityComboBox;
    javax.swing.JLabel priorityLabel;
    javax.swing.JComboBox projectComboBox;
    javax.swing.JLabel projectLabel;
    javax.swing.JComboBox projectionComboBox;
    javax.swing.JLabel projectionLabel;
    org.jdesktop.swingx.JXHyperlink refreshLinkButton;
    javax.swing.JLabel relationsLabel;
    javax.swing.JPanel relationsPanel;
    javax.swing.JLabel reporterLabel;
    javax.swing.JLabel reporterValueLabel;
    javax.swing.JComboBox reproducibilityComboBox;
    javax.swing.JLabel reproducibilityLabel;
    javax.swing.JComboBox resolutionComboBox;
    javax.swing.JLabel resolutionLabel;
    javax.swing.JScrollPane scrollablePane;
    javax.swing.JLabel seperatorLabel;
    javax.swing.JComboBox severityComboBox;
    javax.swing.JLabel severityLabel;
    javax.swing.JComboBox statusComboBox;
    javax.swing.JLabel statusLabel;
    eu.doppel_helix.netbeans.mantisintegration.swing.DirectionalEditorPane stepsToReproduceEditorPane;
    javax.swing.JLabel stepsToReproduceLabel;
    javax.swing.JScrollPane stepsToReproduceScrollPane;
    javax.swing.JPanel subheaderPanel;
    javax.swing.JLabel summaryLabel;
    javax.swing.JTextField summaryTextField;
    javax.swing.JLabel tagsLabel;
    javax.swing.JPanel tagsPanel;
    javax.swing.JComboBox targetVersionComboBox;
    javax.swing.JLabel targetVersionLabel;
    javax.swing.JFormattedTextField timetrackInput;
    javax.swing.JLabel timetrackLabel;
    javax.swing.JButton updateIssueButton;
    javax.swing.JLabel updatedLabel;
    javax.swing.JLabel updatedValueLabel;
    javax.swing.JComboBox versionComboBox;
    javax.swing.JLabel versionLabel;
    javax.swing.JComboBox viewStatusComboBox;
    javax.swing.JLabel viewStatusLabel;
    // End of variables declaration//GEN-END:variables

    private class CustomLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return headerButtonsPanel.getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return headerButtonsPanel.getPreferredSize();
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Dimension d = headerButtonsPanel.getMinimumSize();
                int x = (int) (parent.getWidth() - d.getWidth());
                issueHeader.setBounds(0, 0, x, (int) issueHeader.getPreferredSize().getHeight());
                headerButtonsPanel.setBounds(x, 0, (int) d.getWidth(), (int) d.getHeight());
            }
        }
    }
}
