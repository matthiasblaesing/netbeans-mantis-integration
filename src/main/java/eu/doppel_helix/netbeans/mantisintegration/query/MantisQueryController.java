package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.FilterData;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.ProjectData;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.data.FlattenedProjectData;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import eu.doppel_helix.netbeans.mantisintegration.swing.ListBackedComboBoxModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

public class MantisQueryController implements ActionListener, PropertyChangeListener, QueryController {

    public final static String COMMAND_OPEN_ISSUE = "open_issue";
    public final static String COMMAND_SELECT_PROJECT1 = "selectProject1";
    public final static String COMMAND_SELECT_PROJECT2 = "selectProject2";
    public final static String COMMAND_EXECUTE_QUERY = "executeQuery";
    public final static String COMMAND_DELETE_QUERY = "deleteQuery";
    public final static String COMMAND_SAVE_QUERY = "saveQuery";
    private final static Logger logger = Logger.getLogger(
            MantisQueryController.class.getName());
    RequestProcessor rp = new RequestProcessor("MantisQueryController");
    private ListBackedComboBoxModel<FlattenedProjectData> projectModel1 = new ListBackedComboBoxModel<>(
            FlattenedProjectData.class);
    private ListBackedComboBoxModel<FilterData> filterModel1 = new ListBackedComboBoxModel<>(
            FilterData.class);
    private ListBackedComboBoxModel<AccountData> reporterModel = new ListBackedComboBoxModel<>(
            AccountData.class);
    private ListBackedComboBoxModel<AccountData> assignedToModel = new ListBackedComboBoxModel<>(
            AccountData.class);
    private ListBackedComboBoxModel<String> categoryModel = new ListBackedComboBoxModel<>(
            String.class);
    private ListBackedComboBoxModel<ObjectRef> severityModel = new ListBackedComboBoxModel<>(
            ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> resolutionModel = new ListBackedComboBoxModel<>(
            ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> statusModel = new ListBackedComboBoxModel<>(
            ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> priorityModel = new ListBackedComboBoxModel<>(
            ObjectRef.class);
    private ListBackedComboBoxModel<ObjectRef> viewstatusModel = new ListBackedComboBoxModel<>(
            ObjectRef.class);
    private MantisQueryPanel mqp;
    private MantisQuery mq;
    private MantisRepository mr;
    private final static ProjectData pseudoProject;

    static {
        pseudoProject = new ProjectData();
        pseudoProject.setAccess_min(new ObjectRef(BigInteger.ZERO, "None"));
        pseudoProject.setDescription("");
        pseudoProject.setId(BigInteger.ZERO);
        pseudoProject.setName("All");
    }

    private final SwingWorker initialize = new SwingWorker() {
        List<FlattenedProjectData> projects;
        List<AccountData> users;
        List<String> categories;
        List<ObjectRef> severities;
        List<ObjectRef> resolutions;
        List<ObjectRef> states;
        List<ObjectRef> priorities;
        List<ObjectRef> viewstates;
        private Exception exception;

        @Override
        protected Object doInBackground() throws Exception {
            try {
                projects = new ArrayList<>();
                projects.add(new FlattenedProjectData(pseudoProject, 0));
                for (ProjectData pd : mr.getProjects()) {
                    projects.addAll(FlattenedProjectData.buildList(pd));
                }
                users = new ArrayList<>(Arrays.asList(mr.getUsers(
                        BigInteger.ZERO)));
                users.add(0, null);
                categories = new ArrayList<>(Arrays.asList(
                        mr.getCategories(BigInteger.ZERO)));
                categories.add(0, null);
                severities = new ArrayList<>(Arrays.asList(
                        mr.getSeverities()));
                severities.add(0, null);
                resolutions = new ArrayList<>(Arrays.asList(
                        mr.getResolutions()));
                resolutions.add(0, null);
                states = new ArrayList<>(Arrays.asList(mr.getStates()));
                states.add(0, null);
                priorities = new ArrayList<>(Arrays.asList(
                        mr.getPriorities()));
                priorities.add(0, null);
                viewstates = new ArrayList<>(Arrays.asList(
                        mr.getViewStates()));
                viewstates.add(0, null);
            } catch (Exception ex) {
                exception = ex;
            }
            return null;
        }

        @Override
        protected void done() {
            if (exception != null) {
                NotifyDescriptor nd = new NotifyDescriptor.Exception(exception,
                        "Failed to update ");
                DialogDisplayer.getDefault().notifyLater(nd);
            } else {
                reporterModel.setBackingList(users);
                assignedToModel.setBackingList(users);
                categoryModel.setBackingList(categories);
                severityModel.setBackingList(severities);
                resolutionModel.setBackingList(resolutions);
                statusModel.setBackingList(states);
                priorityModel.setBackingList(priorities);
                viewstatusModel.setBackingList(viewstates);
                projectModel1.setBackingList(projects);

                projectModel1.setSelectedItem(new FlattenedProjectData(
                        pseudoProject, 0));
                reporterModel.setSelectedItem(mq.getReporter());
                assignedToModel.setSelectedItem(mq.getAssignedTo());
                categoryModel.setSelectedItem(mq.getCategory());
                severityModel.setSelectedItem(mq.getSeverity());
                resolutionModel.setSelectedItem(mq.getResolution());
                statusModel.setSelectedItem(mq.getStatus());
                priorityModel.setSelectedItem(mq.getPriority());
                viewstatusModel.setSelectedItem(mq.getViewStatus());
                if (mq.getProjectId() != null) {
                    FlattenedProjectData foundFpd = null;
                    for (FlattenedProjectData fpd : projects) {
                        if (fpd.getProjectData().getId().equals(
                                mq.getProjectId())) {
                            projectModel1.setSelectedItem(fpd);
                            break;
                        }
                    }
                }
                updateFilterList();

                if (mq.getServersideFilterId() != null) {
                    for (FilterData fd : filterModel1.getBackingList()) {
                        if (mq.getServersideFilterId().equals(fd.getId())) {
                            filterModel1.setSelectedItem(fd);
                            break;
                        }
                    }
                }

                if (mqp != null) {
                    mqp.lastUpdateAfterDatePicker.setDate(
                            mq.getLastUpdateAfter());
                    mqp.lastUpdateBeforeDatePicker.setDate(
                            mq.getLastUpdateBefore());
                    mqp.summaryTextField.setText(mq.getSummaryFilter());
                    if (mq.getCombination() == MantisQuery.Combination.ANY) {
                        mqp.matchTypeComboBox.setSelectedIndex(1);
                    }
                }
            }
            mq.setBusy(false);
        }
    };

    public MantisQueryController(final MantisQuery mq) {
        this.mq = mq;
        this.mr = mq.getMantisRepository();

        mq.setBusy(true);
        initialize.execute();
    }
    
    private void onSaveState() {
        if (mq.isSaved()) {
            mqp.gotoIssuePanel.setVisible(false);
            mqp.headerButtonsPanel.setVisible(true);
        } else {
            mqp.gotoIssuePanel.setVisible(true);
            mqp.headerButtonsPanel.setVisible(false);
        }
    }

    private void updateFilterList() {
        FlattenedProjectData selected = (FlattenedProjectData) projectModel1.getSelectedItem();
        if (selected != null) {
            try {
                FilterData[] filter = mr.getFilters(
                        selected.getProjectData().getId());
                if (filter != null) {
                    filterModel1.setBackingList(Arrays.asList(filter));
                    filterModel1.addElement(0, null);
                }
            } catch (Exception ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                        "Failed to retrieve filterlist");
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (COMMAND_OPEN_ISSUE.equals(e.getActionCommand())) {
            gotoIssue();
        } else if (COMMAND_SELECT_PROJECT1.equals(e.getActionCommand())) {
            updateFilterList();
        } else if (COMMAND_SAVE_QUERY.equals(e.getActionCommand())) {
            if (mq.getName() == null || mq.getName().isEmpty()) {
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                        "Name", "Save query");
                DialogDisplayer.getDefault().notify(nd);
                mq.setName((String) nd.getInputText());
            }
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    mq.setBusy(true);
                    mq.save();
                    mq.refresh();
                    Collection<MantisIssue> issues = mq.getIssues();
                    for (MantisIssue mi : issues) {
                        getComponent(QueryMode.VIEW).getQueryListModel().setIssues(issues);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    mq.setBusy(false);
                    try {
                        get();
                    } catch ( InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }.execute();
        } else if (COMMAND_DELETE_QUERY.equals(e.getActionCommand())) {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    mq.remove();
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                    } catch ( InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }.execute();
        } else if (COMMAND_EXECUTE_QUERY.equals(e.getActionCommand())) {
            if (projectModel1.getSelectedItem() != null) {
                mq.setProjectId(
                        ((FlattenedProjectData) projectModel1.getSelectedItem()).getProjectData().getId());
            } else {
                mq.setProjectId(null);
            }
            if (filterModel1.getSelectedItem() != null) {
                mq.setServersideFilterId(
                        ((FilterData) filterModel1.getSelectedItem()).getId());
            } else {
                mq.setServersideFilterId(null);
            }
            mq.setReporter((AccountData) reporterModel.getSelectedItem());
            mq.setAssignedTo((AccountData) assignedToModel.getSelectedItem());
            mq.setCategory((String) categoryModel.getSelectedItem());
            mq.setSeverity((ObjectRef) severityModel.getSelectedItem());
            mq.setResolution((ObjectRef) resolutionModel.getSelectedItem());
            mq.setStatus((ObjectRef) statusModel.getSelectedItem());
            mq.setPriority((ObjectRef) priorityModel.getSelectedItem());
            mq.setViewStatus((ObjectRef) viewstatusModel.getSelectedItem());
            mq.setLastUpdateAfter(mqp.lastUpdateAfterDatePicker.getDate());
            mq.setLastUpdateBefore(mqp.lastUpdateBeforeDatePicker.getDate());
            if (mqp.matchTypeComboBox.getSelectedIndex() == 1) {
                mq.setCombination(MantisQuery.Combination.ANY);
            } else {
                mq.setCombination(MantisQuery.Combination.ALL);
            }
            mq.setSummaryFilter(mqp.summaryTextField.getText());

            SwingWorker sw = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    mq.setBusy(true);
                    mq.refresh();
                    Collection<MantisIssue> issues = mq.getIssues();
                    getComponent(QueryMode.VIEW).getQueryListModel().setIssues(issues);
                    return null;
                }

                @Override
                protected void done() {
                    mq.setBusy(false);
                    try {
                        get();
                    } catch ( InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };

            sw.execute();
        }
    }

    private void gotoIssue() {
        getComponent(QueryMode.VIEW); // Make sure component exists

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    MantisIssue mi = mq.getMantisRepository().
                            getIssues(false, mqp.gotoIssueTextField.getText()).get(0);

                    Mantis.getInstance().getBugtrackingSupport().openIssue(
                            mq.getMantisRepository(), mi);
                } catch (Exception ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                            "Failed to open issue");
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
        };

        rp.submit(r);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("busy".equals(evt.getPropertyName()) && mqp != null) {
            mqp.waitPanel.setVisible((Boolean) evt.getNewValue());
        }
    }

    @Override
    public boolean providesMode(QueryMode mode) {
        if(mode == QueryMode.EDIT) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MantisQueryPanel getComponent(QueryMode mode) {
        if (mqp == null) {
            mqp = new MantisQueryPanel();
            mqp.projectComboBox.setModel(projectModel1);
            mqp.projectComboBox.addActionListener(this);
            mqp.projectComboBox.setActionCommand(COMMAND_SELECT_PROJECT1);
            mqp.reporterComboBox.setModel(reporterModel);
            mqp.assignedToComboBox.setModel(assignedToModel);
            mqp.categoryComboBox.setModel(categoryModel);
            mqp.severityComboBox.setModel(severityModel);
            mqp.resolutionComboBox.setModel(resolutionModel);
            mqp.statusComboBox.setModel(statusModel);
            mqp.priorityComboBox.setModel(priorityModel);
            mqp.viewStatusComboBox.setModel(viewstatusModel);
            mqp.gotoIssueButton.setActionCommand(COMMAND_OPEN_ISSUE);
            mqp.gotoIssueButton.addActionListener(this);
            mqp.gotoIssueTextField.setActionCommand(COMMAND_OPEN_ISSUE);
            mqp.gotoIssueTextField.addActionListener(this);
            mqp.filterComboBox.setModel(filterModel1);
            mqp.executeQueryButton.addActionListener(this);
            mqp.saveQueryButton.addActionListener(this);
            mqp.deleteQueryLinkButton.addActionListener(this);
            mq.addPropertyChangeListener(this);
            mqp.waitPanel.setVisible(mq.isBusy());
            updateFilterList();
            mqp.lastUpdateAfterDatePicker.setDate(mq.getLastUpdateAfter());
            mqp.lastUpdateBeforeDatePicker.setDate(mq.getLastUpdateBefore());
            mqp.summaryTextField.setText(mq.getSummaryFilter());
            if (mq.getCombination() == MantisQuery.Combination.ANY) {
                mqp.matchTypeComboBox.setSelectedIndex(1);
            }
            onSaveState();
        }
        return mqp;
    }

    @Override
    public void opened() {
    }

    @Override
    public void closed() {
    }

    @Override
    public boolean saveChanges(String name) {
        mq.save();
        return true;
    }

    @Override
    public boolean discardUnsavedChanges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isChanged() {
        return ! mq.isSaved();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        mq.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        mq.removePropertyChangeListener(l);
    }
}
