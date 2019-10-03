package eu.doppel_helix.netbeans.mantisintegration.issue;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.AttachmentData;
import biz.futureware.mantisconnect.CustomFieldDefinitionData;
import biz.futureware.mantisconnect.CustomFieldValueForIssueData;
import biz.futureware.mantisconnect.IssueData;
import biz.futureware.mantisconnect.IssueNoteData;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.ProjectData;
import biz.futureware.mantisconnect.ProjectVersionData;
import biz.futureware.mantisconnect.RelationshipData;
import biz.futureware.mantisconnect.UserData;
import eu.doppel_helix.netbeans.mantisintegration.data.FlattenedProjectData;
import eu.doppel_helix.netbeans.mantisintegration.data.Permission;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import eu.doppel_helix.netbeans.mantisintegration.swing.AttachmentDisplay;
import org.jdesktop.swingx.JXHyperlink;
import eu.doppel_helix.netbeans.mantisintegration.swing.ListBackedComboBoxModel;
import eu.doppel_helix.netbeans.mantisintegration.swing.NoteDisplay;
import eu.doppel_helix.netbeans.mantisintegration.swing.RelationshipDisplay;
import eu.doppel_helix.netbeans.mantisintegration.swing.TagDisplay;
import eu.doppel_helix.netbeans.mantisintegration.util.SafeAutocloseable;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;

public class MantisIssueController implements PropertyChangeListener, ActionListener, IssueController {

    private static final Logger logger = Logger.getLogger(MantisIssueController.class.getName());
    private static File lastDirectory;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ListBackedComboBoxModel<FlattenedProjectData> projectModel = new ListBackedComboBoxModel<>(FlattenedProjectData.class);
    private ListBackedComboBoxModel<ObjectRef> viewstatesModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> viewstatesModel2 = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> severitiesModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> reproducibilitiesModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> prioritiesModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> resolutionsModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> statesModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> etasModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<String> targetVersionModel = new ListBackedComboBoxModel<>(String.class);
    private ListBackedComboBoxModel<String> productVersionModel = new ListBackedComboBoxModel<>(String.class);
    private ListBackedComboBoxModel<String> fixVersionModel = new ListBackedComboBoxModel<>(String.class);
    private ListBackedComboBoxModel<ObjectRef> projectionsModel = new ListBackedComboBoxModel<>(ObjectRef.class);
    private ListBackedComboBoxModel<String> categoriesModel = new ListBackedComboBoxModel<>(String.class);
    private ListBackedComboBoxModel<AccountData> assignedModel = new ListBackedComboBoxModel<>(AccountData.class);
    private Map<BigInteger,String> customFieldValueBackingStore = new HashMap<>();
    private MantisIssuePanel panel;
    private final MantisIssue issue;
    private StateMonitor stateMonitor = new StateMonitor();
    private final SwingWorker updateModel = new SwingWorker() {
        List<FlattenedProjectData> projects;
        List<ObjectRef> viewStates;
        List<ObjectRef> severities;
        List<ObjectRef> reproducibilities;
        List<ObjectRef> resolutions;
        List<ObjectRef> priorities;
        List<ObjectRef> states;
        List<ObjectRef> etas;
        List<ObjectRef> projections;

        @Override
        protected Object doInBackground() throws Exception {
            try(SafeAutocloseable ac = issue.busy()) {
                MantisRepository mr = issue.getMantisRepository();
                viewStates = Arrays.asList(mr.getMasterData().getViewStates());
                
                projects = new ArrayList<>();
                projects.add(null);
                for(ProjectData pd: mr.getMasterData().getProjects()) {
                    projects.addAll(FlattenedProjectData.buildList(pd));
                }
                
                severities = Arrays.asList(mr.getMasterData().getSeverities());
                reproducibilities = Arrays.asList(mr.getMasterData().getReproducibilities());
                resolutions = Arrays.asList(mr.getMasterData().getResolutions());
                priorities = Arrays.asList(mr.getMasterData().getPriorities());
                states = new ArrayList<>(Arrays.asList(mr.getMasterData().getStates()));
                states.add(0, null);
                etas = Arrays.asList(mr.getMasterData().getEtas());
                projections = Arrays.asList(mr.getMasterData().getProjections());
                return null;
            }
        }
        
        @Override
        protected void done() {
            try {
                get();
                projectModel.setBackingList(projects);
                viewstatesModel.setBackingList(viewStates);
                viewstatesModel2.setBackingList(viewStates);
                viewstatesModel2.setSelectedItem(viewStates.get(0));
                severitiesModel.setBackingList(severities);
                reproducibilitiesModel.setBackingList(reproducibilities);
                prioritiesModel.setBackingList(priorities);
                resolutionsModel.setBackingList(resolutions);
                statesModel.setBackingList(states);
                etasModel.setBackingList(etas);
                projectionsModel.setBackingList(projections);
                updateInfo(null);
            } catch (Exception ex) {
                issue.getMantisRepository()
                        .getExceptionHandler()
                        .handleException(logger, "Failed to update", ex);
            }
        }
    };
    
    public MantisIssueController(final MantisIssue issue) {
        this.issue = issue;
        issue.addPropertyChangeListener(this);
        updateModel.execute();
    }

    @Override
    public JComponent getComponent() {
        if (panel == null) {
            panel = new MantisIssuePanel();
            panel.addNoteViewStateComboBox.setModel(viewstatesModel2);
            panel.projectComboBox.setModel(projectModel);
            panel.categoryComboBox.setModel(categoriesModel);
            panel.viewStatusComboBox.setModel(viewstatesModel);
            panel.severityComboBox.setModel(severitiesModel);
            panel.reproducibilityComboBox.setModel(reproducibilitiesModel);
            panel.priorityComboBox.setModel(prioritiesModel);
            panel.assignedToComboBox.setModel(assignedModel);
            panel.resolutionComboBox.setModel(resolutionsModel);
            panel.statusComboBox.setModel(statesModel);
            panel.projectionComboBox.setModel(projectionsModel);
            panel.etaComboBox.setModel(etasModel);
            panel.targetVersionComboBox.setModel(targetVersionModel);
            panel.fixVersionComboBox.setModel(fixVersionModel);
            panel.versionComboBox.setModel(productVersionModel);
            panel.refreshLinkButton.addActionListener(this);
            panel.openIssueWebbrowserLinkButton.addActionListener(this);
            panel.projectComboBox.addActionListener(this);
            panel.updateIssueButton.setVisible(false);
            panel.addNoteButton.addActionListener(this);
            panel.addIssueButton.addActionListener(this);
            panel.updateIssueButton.addActionListener(this);
            panel.projectComboBox.addActionListener(stateMonitor);
            panel.descriptionEditorPane.getDocument().addDocumentListener(stateMonitor);
            panel.summaryTextField.getDocument().addDocumentListener(stateMonitor);
            panel.waitPanel.setVisible(issue.isBusy());
            updateInfo(null);
        }
        return panel;
    }

    private final Runnable updateInputState = new Runnable() {
        @Override
        public void run() {
            panel.updateIssueButton.setVisible(false);
            panel.addIssueButton.setVisible(false);
            panel.notesOuterPanel.setVisible(false);
            panel.statusComboBox.setEnabled(false);
            panel.resolutionComboBox.setEnabled(false);
            panel.headerPanel.setVisible(false);
            panel.subheaderPanel.setVisible(false);
            panel.relationsLabel.setVisible(false);
            panel.relationsPanel.setVisible(false);
            panel.tagsLabel.setVisible(false);
            panel.tagsPanel.setVisible(false);
            panel.attachmentLabel.setVisible(false);
            panel.attachmentPanel.setVisible(false);
//            panel.filler1.setVisible(false);
            if (issue.getId() == null) {
                setUpdateEnabledFields(true);
                panel.addIssueButton.setVisible(true);
//                panel.filler1.setVisible(true);
            } else {
                if (issue.canUpdate()) {
                    panel.updateIssueButton.setVisible(true);
                    panel.statusComboBox.setEnabled(true);
                    panel.resolutionComboBox.setEnabled(true);
                }
                setUpdateEnabledFields(issue.canUpdate());
                panel.notesOuterPanel.setVisible(true);
                panel.headerPanel.setVisible(true);
                panel.subheaderPanel.setVisible(true);
                panel.relationsLabel.setVisible(true);
                panel.relationsPanel.setVisible(true);
                panel.tagsLabel.setVisible(true);
                panel.tagsPanel.setVisible(true);
                panel.attachmentLabel.setVisible(true);
                panel.attachmentPanel.setVisible(true);
                boolean timeTracking = issue.getTimetracking() == Permission.WRITE;
                panel.timetrackInput.setVisible(timeTracking);
                panel.timetrackLabel.setVisible(timeTracking);
            }
        }
    };
    
    private void setUpdateEnabledFields(boolean enabled) {
        panel.projectComboBox.setEnabled(enabled);
        panel.viewStatusComboBox.setEnabled(enabled);
        panel.assignedToComboBox.setEnabled(enabled);
        panel.categoryComboBox.setEnabled(enabled);
        panel.severityComboBox.setEnabled(enabled);
        panel.reproducibilityComboBox.setEnabled(enabled);
        panel.priorityComboBox.setEnabled(enabled);
        panel.projectionComboBox.setEnabled(enabled);
        panel.etaComboBox.setEnabled(enabled);
        panel.targetVersionComboBox.setEnabled(enabled);
        panel.fixVersionComboBox.setEnabled(enabled);
        panel.versionComboBox.setEnabled(enabled);
        panel.platformTextField.setEditable(enabled);
        panel.osTextField.setEditable(enabled);
        panel.osVersionTextField.setEditable(enabled);
        panel.buildTextField.setEditable(enabled);
        panel.summaryTextField.setEditable(enabled);
        panel.descriptionEditorPane.setEditable(enabled);
        panel.stepsToReproduceEditorPane.setEditable(enabled);
        panel.additionalInformationEditorPane.setEditable(enabled);
    }

    private void updateInfo(String property) {
        if (panel != null) {
            if (property == null || "id".equals(property)) {
                issue.getMantisRepository().getRequestProcessor()
                        .execute(updateInputState);
                panel.issueHeader.setText(issue.getDisplayValue());
            }
            if (property == null || "summary".equals(property)) {
                panel.issueHeader.setText(issue.getDisplayValue());
                panel.summaryTextField.setText(issue.getSummary());
            }
            if (property == null || "date_submitted".equals(property)) {
                if (issue.getDate_submitted() == null) {
                    panel.createdValueLabel.setText("-");
                } else {
                    panel.createdValueLabel.setText(dateFormat.format(issue.getDate_submitted().getTime()));
                }
            }
            if (property == null || "last_updated".equals(property)) {
                if (issue.getLast_updated() == null) {
                    panel.updatedValueLabel.setText("-");
                } else {
                    panel.updatedValueLabel.setText(dateFormat.format(issue.getLast_updated().getTime()));
                }
            }
            if (property == null || "reporter".equals(property)) {
                if (issue.getReporter() == null) {
                    panel.reporterValueLabel.setText(" - ");
                } else {
                    String account = issue.getReporter().getName();
                    String name = issue.getReporter().getReal_name();
                    if (name == null) {
                        panel.reporterValueLabel.setText(account);
                    } else {
                        panel.reporterValueLabel.setText(account + " - " + name);
                    }
                }
            }
            if (property == null || "project".equals(property)) {
                FlattenedProjectData current = null;
                if (issue.getProject() != null) {
                    BigInteger id = issue.getProject().getId();
                    for (int i = 0; i < projectModel.getSize(); i++) {
                        if ((projectModel.getElementAt(i) != null)
                                && (projectModel.getElementAt(i).getProjectData().getId().equals(id))) {
                            current = projectModel.getElementAt(i);
                            break;
                        }
                    }
                }
                panel.projectComboBox.setSelectedItem(current);
            }
            if (property == null || "category".equals(property)) {
                categoriesModel.setSelectedItem(issue.getCategory());
            }
            if (property == null || "target_version".equals(property)) {
                targetVersionModel.setSelectedItem(issue.getTarget_version());
            }
            if (property == null || "version".equals(property)) {
                productVersionModel.setSelectedItem(issue.getVersion());
            }
            if (property == null || "fixed_in_version".equals(property)) {
                fixVersionModel.setSelectedItem(issue.getFixed_in_version());
            }
            if (property == null || "view_state".equals(property)) {
                panel.viewStatusComboBox.setSelectedItem(issue.getView_state());
            }
            if (property == null || "severity".equals(property)) {
                panel.severityComboBox.setSelectedItem(issue.getSeverity());
            }
            if (property == null || "reproducibility".equals(property)) {
                panel.reproducibilityComboBox.setSelectedItem(issue.getReproducibility());
            }
            if (property == null || "priority".equals(property)) {
                panel.priorityComboBox.setSelectedItem(issue.getPriority());
            }
            if (property == null || "handler".equals(property)) {
                AccountData target = issue.getHandler();
                assignedModel.setSelectedItem(target);
            }
            if (property == null || "resolution".equals(property)) {
                panel.resolutionComboBox.setSelectedItem(issue.getResolution());
            }
            if (property == null || "status".equals(property)) {
                panel.statusComboBox.setSelectedItem(issue.getStatus());
            }
            if (property == null || "eta".equals(property)) {
                panel.etaComboBox.setSelectedItem(issue.getEta());
            }
            if (property == null || "projection".equals(property)) {
                panel.projectionComboBox.setSelectedItem(issue.getProjection());
            }
            if (property == null || "description".equals(property)) {
                DefaultCaret caret = (DefaultCaret) panel.descriptionEditorPane.getCaret();
                int oldPolicy = caret.getUpdatePolicy();
                caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
                panel.descriptionEditorPane.setText(issue.getDescription());
                caret.setUpdatePolicy(oldPolicy);
                panel.descriptionScrollPane.setPreferredSize(new Dimension(
                        10,
                        Math.max(
                            (int) panel.descriptionEditorPane.getPreferredSize().getHeight() + 10,
                            95)));
            }
            if (property == null || "additional_information".equals(property)) {
                DefaultCaret caret = (DefaultCaret) panel.additionalInformationEditorPane.getCaret();
                int oldPolicy = caret.getUpdatePolicy();
                caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
                panel.additionalInformationEditorPane.setText(issue.getAdditional_information());
                caret.setUpdatePolicy(oldPolicy);
                panel.additionalInformationScrollPane.setPreferredSize(new Dimension(
                        10,
                        Math.max(
                            (int) panel.additionalInformationEditorPane.getPreferredSize().getHeight() + 10,
                            70)));
            }
            if (property == null || "steps_to_reproduce".equals(property)) {
                DefaultCaret caret = (DefaultCaret) panel.stepsToReproduceEditorPane.getCaret();
                int oldPolicy = caret.getUpdatePolicy();
                caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
                panel.stepsToReproduceEditorPane.setText(issue.getSteps_to_reproduce());
                caret.setUpdatePolicy(oldPolicy);
                panel.stepsToReproduceScrollPane.setPreferredSize(new Dimension(
                        10,
                        Math.max(
                            (int) panel.stepsToReproduceEditorPane.getPreferredSize().getHeight() + 10,
                            70)));
            }
            if (property == null || "platform".equals(property)) {
                panel.platformTextField.setText(issue.getPlatform());
            }
            if (property == null || "os".equals(property)) {
                panel.osTextField.setText(issue.getOs());
            }
            if (property == null || "os_build".equals(property)) {
                panel.osVersionTextField.setText(issue.getOs_build());
            }
            if (property == null || "build".equals(property)) {
                panel.buildTextField.setText(issue.getBuild());
            }
            if (property == null || "relationships".equals(property)) {
                panel.relationsPanel.removeAll();
                if (issue.getRelationships() != null) {
                    for (RelationshipData rd : issue.getRelationships()) {
                        panel.relationsPanel.add(new RelationshipDisplay(issue, rd));
                    }
                }
                JXHyperlink lb = new JXHyperlink();
                lb.setText("Add");
                lb.setActionCommand("addRelationship");
                lb.addActionListener(this);
                panel.relationsPanel.add(lb);
            }
            if (property == null || "tags".equals(property)) {
                panel.tagsPanel.removeAll();
                if (issue.getTags() != null) {
                    for (ObjectRef or : issue.getTags()) {
                        panel.tagsPanel.add(new TagDisplay(issue, or));
                    }
                }
                JXHyperlink lb = new JXHyperlink();
                lb.setText("Add");
                lb.setActionCommand("addTag");
                lb.addActionListener(this);
                panel.tagsPanel.add(lb);
            }
            if (property == null || "attachments".equals(property)) {
                panel.attachmentPanel.removeAll();
                if (issue.getAttachments() != null) {
                    for (AttachmentData ad : issue.getAttachments()) {
                        AttachmentDisplay adisplay = new AttachmentDisplay(issue, ad);
                        adisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panel.attachmentPanel.add(adisplay);
                    }
                }
                JXHyperlink lb = new JXHyperlink();
                lb.setText("Add");
                lb.setActionCommand("addAttachment");
                lb.addActionListener(this);
                panel.attachmentPanel.add(lb);
            }
            if (property == null || "notes".equals(property)) {
                panel.notesPanel.removeAll();
                Dimension fixedDim = new Dimension(1, 5);
                if (issue.getNotes() != null) {
                    boolean first = true;
                    for (IssueNoteData ind : issue.getNotes()) {
                        if (!first) {
                            panel.notesPanel.add(new Box.Filler(fixedDim, fixedDim, fixedDim));
                        }
                        boolean showTimeTracking = issue.getTimetracking() == Permission.WRITE;
                        showTimeTracking |= issue.getTimetracking() == Permission.READ;
                        panel.notesPanel.add(new NoteDisplay(ind, showTimeTracking));
                        if (first) {
                            first = false;
                        }
                    }
                }
            }
            if (property == null || "custom_fields".equals(property)) {
                if (issue.getCustom_fields() != null) {
                    for (CustomFieldValueForIssueData cfvfi : issue.getCustom_fields()) {
                        CustomFieldComponent cfc = panel.getCustomFieldById(cfvfi.getField().getId());
                        if (cfc != null) {
                            cfc.setValue(cfvfi.getValue());
                        }
                    }
                }
            }
            panel.timetrackInput.setValue(BigInteger.ZERO);
            panel.revalidate();
            panel.repaint();
        }
    }

    private IssueData getUpdateData() {
        IssueData updateData = new IssueData();
        updateData.setId(issue.getId());
        updateData.setAdditional_information(panel.additionalInformationEditorPane.getText());
        // Skip attachments
        updateData.setBuild(panel.buildTextField.getText());
        updateData.setCategory((String) categoriesModel.getSelectedItem());
        // Skip updating submitdate
        updateData.setDescription(panel.descriptionEditorPane.getText());
        updateData.setEta((ObjectRef) panel.etaComboBox.getSelectedItem());
        updateData.setHandler((AccountData) assignedModel.getSelectedItem());
        // Skip notes
        // Skip tags
        updateData.setOs(panel.osTextField.getText());
        updateData.setOs_build(panel.osVersionTextField.getText());
        updateData.setPlatform(panel.platformTextField.getText());
        updateData.setPriority((ObjectRef) panel.priorityComboBox.getSelectedItem());
        FlattenedProjectData pd = (FlattenedProjectData) panel.projectComboBox.getSelectedItem();
        if (pd == null) {
            updateData.setProject(null);
        } else {
            ObjectRef project = new ObjectRef(pd.getProjectData().getId(), 
                    pd.getProjectData().getName());
            updateData.setProject(project);
        }
        updateData.setProjection((ObjectRef) panel.projectionComboBox.getSelectedItem());
        // Skip relationships
        updateData.setReproducibility((ObjectRef) panel.reproducibilityComboBox.getSelectedItem());
        updateData.setResolution((ObjectRef) panel.resolutionComboBox.getSelectedItem());
        updateData.setSeverity((ObjectRef) panel.severityComboBox.getSelectedItem());
        updateData.setStatus((ObjectRef) panel.statusComboBox.getSelectedItem());
        updateData.setSteps_to_reproduce(panel.stepsToReproduceEditorPane.getText());
        updateData.setSummary(panel.summaryTextField.getText());
        updateData.setView_state((ObjectRef) panel.viewStatusComboBox.getSelectedItem());
        String targetVersion = (String) targetVersionModel.getSelectedItem();
        String version = (String)productVersionModel.getSelectedItem();
        String fixedInVersion = (String)fixVersionModel.getSelectedItem();
        updateData.setTarget_version(targetVersion == null ? "" : targetVersion);
        updateData.setVersion(version == null ? "" : version);
        updateData.setFixed_in_version(fixedInVersion == null ? "" : fixedInVersion);
        updateData.setSponsorship_total(issue.getSponsorship_total());
        // Should reporter/submitdate/last be updateable?
        updateData.setReporter(issue.getReporter());
        updateData.setDate_submitted(issue.getDate_submitted());
        updateData.setLast_updated(issue.getLast_updated());
        if(pd != null) {
            try {
                List<ObjectRef> fields = new ArrayList<>();
                for (CustomFieldDefinitionData cfdd : issue.getMantisRepository().getMasterData().getCustomFieldDefinitions(pd.getProjectData().getId())) {
                    fields.add(cfdd.getField());
                }
                CustomFieldValueForIssueData[] customFieldData = new CustomFieldValueForIssueData[fields.size()];
                for(int i = 0; i < customFieldData.length; i++) {
                    ObjectRef field = fields.get(i);
                    try {
                        customFieldData[i] = new CustomFieldValueForIssueData(
                                field,
                                panel.getCustomFieldById(field.getId()).getValue()
                        );
                    } catch (NullPointerException ex) {
                    }
                }
                updateData.setCustom_fields(customFieldData);
            } catch (Exception ex) {
                issue.getMantisRepository()
                        .getExceptionHandler()
                        .handleException(logger, "Failed to get custom field definitions", ex);
            }
        }
        return updateData;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MantisIssueController.class.getName());
    }

    public boolean isValid() {
        boolean result = true;
        result &= panel.projectComboBox.getSelectedItem() != null;
        result &= (!panel.descriptionEditorPane.getText().trim().isEmpty());
        result &= (!panel.summaryTextField.getText().trim().isEmpty());
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if ("busy".equals(propertyName) && panel != null) {
            panel.waitPanel.setVisible((Boolean) evt.getNewValue());
        } else {
            if (IssueProvider.EVENT_ISSUE_DATA_CHANGED.equals(propertyName)) {
                propertyName = null;
                customFieldValueBackingStore.clear();
            }
            updateInfo(propertyName);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final MantisRepository mr = issue.getMantisRepository();
        if ("addIssue".equals(e.getActionCommand())
                || "updateIssue".equals(e.getActionCommand())) {
            mr.getRequestProcessor().submit(new Runnable() {
                public void run() {
                    saveChanges();
                }
            });
        } else if ("addNote".equals(e.getActionCommand())) {
            final ObjectRef viewState = (ObjectRef) panel.addNoteViewStateComboBox.getSelectedItem();
            final String comment = panel.addNoteEditorPane.getText();
            final BigInteger timetracking = (BigInteger) panel.timetrackInput.getValue();
            panel.addNoteViewStateComboBox.setSelectedIndex(0);
            panel.addNoteEditorPane.setText("");
            panel.timetrackInput.setValue(BigInteger.ZERO);
            mr.getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        issue.addComment(comment, viewState, timetracking);
                    } catch (Exception ex) {
                        mr
                                .getExceptionHandler()
                                .handleException(logger, "Failed to comment to issue", ex);
                    }
                }
            });
        } else if ("selectProject".equals(e.getActionCommand())) {
            final FlattenedProjectData fpd = (FlattenedProjectData) panel.projectComboBox.getSelectedItem();
            mr.getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    try (SafeAutocloseable saf = issue.busy()) {
                        final List<String> categories = new ArrayList<>();
                        final List<AccountData> users = new ArrayList<>();
                        final List<String> versions = new ArrayList<>();
                        final CustomFieldDefinitionData[][] customFields = new CustomFieldDefinitionData[1][];
                        if (fpd != null) {
                            BigInteger projectId = fpd.getProjectData().getId();
                            categories.add(null);
                            categories.addAll(Arrays.asList(mr.getMasterData().getCategories(projectId)));
                            users.add(null);
                            users.addAll(Arrays.asList(mr.getMasterData().getUsers(projectId)));
                            versions.add(null);
                            for (ProjectVersionData vdata : mr.getMasterData().getVersions(projectId)) {
                                versions.add(vdata.getName());
                            }
                            customFields[0] = mr.getMasterData().getCustomFieldDefinitions(projectId);
                        } else {
                            customFields[0] = new CustomFieldDefinitionData[0];
                        }
                        
                        final UserData[] ud = new UserData[1];
                        try {
                            ud[0] = mr.getAccount();
                        } catch (ServiceException | RemoteException ex) {
                        };
                        
                        Mutex.EVENT.writeAccess(new Mutex.Action<Void>() {
                            public Void run() {
                                categoriesModel.setBackingList(categories);
                                assignedModel.setBackingList(users);
                                targetVersionModel.setBackingList(versions);
                                productVersionModel.setBackingList(versions);
                                fixVersionModel.setBackingList(versions);

                                for (CustomFieldComponent cfc : panel.getCustomFields()) {
                                    customFieldValueBackingStore.put(
                                            cfc.getCustomFieldDefinitionData().getField().getId(),
                                            cfc.getValue());
                                }
                                Map<BigInteger,String> issueData = new HashMap<>();
                                for(CustomFieldValueForIssueData cfvfid: issue.getCustom_fields()) {
                                    issueData.put(cfvfid.getField().getId(), cfvfid.getValue());
                                }
                                panel.clearCustomFields();
                                for (CustomFieldDefinitionData cfdd : customFields[0]) {
                                    CustomFieldComponent cfc = CustomFieldComponent.create(cfdd, ud[0]);
                                    BigInteger id = cfc.getCustomFieldDefinitionData().getField().getId();
                                    if (customFieldValueBackingStore.containsKey(id)) {
                                        cfc.setValue(customFieldValueBackingStore.get(id));
                                    } else if (issue.getId() == null
                                            || BigInteger.ZERO.equals(issue.getId())) {
                                        cfc.setDefaultValue();
                                    } else {
                                        cfc.setValue(issueData.get(id));
                                    }
                                    panel.addCustomField(cfc);
                                }
                                return null;
                            }
                        });
                    } catch (Exception ex) {
                        mr
                                .getExceptionHandler()
                                .handleException(logger, "Failed to create/add issue", ex);
                    }
                }
            });
        } else if ("refreshIssue".equals(e.getActionCommand())) {
            if (issue.getId() != null) {
                mr.getRequestProcessor().submit(new Runnable() {
                    public void run() {
                        try {
                            issue.refresh();
                        } catch (Exception ex) {
                            mr
                                    .getExceptionHandler()
                                    .handleException(logger, "Failed to refresh issue", ex);
                        }
                    }
                });
            }
        } else if ("openIssueWebbrowser".equals(e.getActionCommand())) {
            URI uri = mr.getIssueUrl(issue);
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if ("addRelationship".equals(e.getActionCommand())) {
            new AddRelationshipDialog((JFrame) WindowManager.getDefault().getMainWindow(), issue).setVisible(true);
        } else if ("addTag".equals(e.getActionCommand())) {
            new AddTagDialog((JFrame) WindowManager.getDefault().getMainWindow(), issue).setVisible(true);
        } else if ("addAttachment".equals(e.getActionCommand())) {
            final JFileChooser fileChooser = new JFileChooser(lastDirectory);
            fileChooser.setDialogTitle("Add attachment");
            int result = fileChooser.showOpenDialog(getComponent());
            if (result == JFileChooser.APPROVE_OPTION) {
                lastDirectory = fileChooser.getCurrentDirectory();
                mr.getRequestProcessor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            issue.addFile(fileChooser.getSelectedFile(), null);
                        } catch (Exception ex) {
                            mr
                                    .getExceptionHandler()
                                    .handleException(logger, "Failed to add file to issue", ex);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void opened() {
    }

    @Override
    public void closed() {
    }

    @Override
    public boolean saveChanges() {
        try(SafeAutocloseable ac = issue.busy()) {
            if (isValid()) {
                if (issue.getId() == null) {
                    MantisRepository mr = issue.getMantisRepository();
                    mr.addIssue(issue, getUpdateData());
                } else {
                    MantisRepository mr = issue.getMantisRepository();
                    mr.updateIssue(issue, getUpdateData());
                }
            }
        } catch (Exception ex) {
            issue.getMantisRepository()
                    .getExceptionHandler()
                    .handleException(logger, "Failed to create/add issue", ex);
        }
        return true;
    }

    @Override
    public boolean discardUnsavedChanges() {
        updateInfo(null);
        return true;
    }

    @Override
    public boolean isChanged() {
        // @todo: Impelement change detection
        return false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        issue.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        issue.removePropertyChangeListener(l);
    }

    private class StateMonitor implements DocumentListener, ActionListener {

        private void updateButtonState() {
            if (isValid()) {
                panel.addIssueButton.setEnabled(true);
                panel.updateIssueButton.setEnabled(true);
            } else {
                panel.addIssueButton.setEnabled(false);
                panel.updateIssueButton.setEnabled(false);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateButtonState();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateButtonState();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateButtonState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateButtonState();
        }
    }
}
