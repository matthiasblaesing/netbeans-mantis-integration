package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.FilterData;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.ProjectData;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.data.FlattenedProjectData;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisStatusProvider;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import eu.doppel_helix.netbeans.mantisintegration.swing.ListBackedComboBoxModel;
import eu.doppel_helix.netbeans.mantisintegration.util.SafeAutocloseable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
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

    private class IssueTableIssueOpener implements MouseListener, KeyListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            int mouseRow = mqp.issueTable.rowAtPoint(e.getPoint());
            if ((mouseRow != -1) && (!mqp.issueTable.isRowSelected(mouseRow))) {
                mqp.issueTable.setRowSelectionInterval(mouseRow, mouseRow);
            }
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                int viewRow = mqp.issueTable.getSelectedRow();
                if (viewRow == -1) {
                    return;
                }
                int modelRow = mqp.issueTable.convertRowIndexToModel(viewRow);
                MantisIssue mi = mqp.getQueryListModel().getIssue(modelRow);
                Mantis.getInstance().getBugtrackingSupport().openIssue(
                        mi.getMantisRepository(),
                        mi);
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                final MantisIssue issue;
                int viewRow = mqp.issueTable.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = mqp.issueTable.convertRowIndexToModel(viewRow);
                    issue = ((QueryListModel) mqp.issueTable.getModel()).getIssue(modelRow);
                } else {
                    issue = null;
                }

                final MantisStatusProvider statusProvider = Mantis.getInstance().getStatusProvider();

                JPopupMenu menu = new JPopupMenu();
                JMenuItem openItem = new JMenuItem(NbBundle.getMessage(MantisQueryController.class, "MantisQueryPanel.menuOpenIssue"));
                openItem.setEnabled(issue != null);
                openItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mantis.getInstance().getBugtrackingSupport().openIssue(
                                issue.getMantisRepository(),
                                issue);
                    }
                });
                JMenuItem markAsRead = new JMenuItem(NbBundle.getMessage(MantisQueryController.class, "MantisQueryPanel.menuMarkAsRead"));
                markAsRead.setEnabled(statusProvider.getStatus(issue) == IssueStatusProvider.Status.INCOMING_MODIFIED
                                    || statusProvider.getStatus(issue) == IssueStatusProvider.Status.INCOMING_NEW);
                markAsRead.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        statusProvider.setSeenIncoming(issue, true);
                    }
                });
                JMenuItem markAsUnRead = new JMenuItem(NbBundle.getMessage(MantisQueryController.class, "MantisQueryPanel.menuMarkAsUnRead"));
                markAsUnRead.setEnabled(statusProvider.getStatus(issue) == IssueStatusProvider.Status.SEEN);
                markAsUnRead.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        statusProvider.setSeenIncoming(issue, false);
                    }
                });
                menu.add(openItem);
                menu.add(markAsRead);
                menu.add(markAsUnRead);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                int viewRow = mqp.issueTable.getSelectedRow();
                if (viewRow == -1) {
                    return;
                }
                int modelRow = mqp.issueTable.convertRowIndexToModel(viewRow);
                MantisIssue mi = mqp.getQueryListModel().getIssue(modelRow);
                Mantis.getInstance().getBugtrackingSupport().openIssue(
                        mi.getMantisRepository(),
                        mi);
                e.consume();
            }
        }
    }

    IssueTableIssueOpener issueTableIssueOpener = new IssueTableIssueOpener();

    static {
        pseudoProject = new ProjectData();
        pseudoProject.setAccess_min(new ObjectRef(BigInteger.ZERO, "None"));
        pseudoProject.setDescription("");
        pseudoProject.setId(BigInteger.ZERO);
        pseudoProject.setName("All");
    }

    public MantisQueryController(final MantisQuery mq) {
        this.mq = mq;
        this.mr = mq.getMantisRepository();

        Runnable initializer = new Runnable() {
            @Override
            public void run() {
                try (SafeAutocloseable ac = mq.busy()) {
                    final FilterData[] filter = mr.getMasterData().getFilters(mq.getProjectId());

                    final List<FlattenedProjectData> projects = new ArrayList<>();
                    projects.add(new FlattenedProjectData(pseudoProject, 0));
                    for (ProjectData pd : mr.getMasterData().getProjects()) {
                        projects.addAll(FlattenedProjectData.buildList(pd));
                    }
                    final List<AccountData> users = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getUsers(BigInteger.ZERO)));
                    users.add(0, null);
                    final List<String> categories = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getCategories(BigInteger.ZERO)));
                    categories.add(0, null);
                    final List<ObjectRef> severities = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getSeverities()));
                    severities.add(0, null);
                    final List<ObjectRef> resolutions = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getResolutions()));
                    resolutions.add(0, null);
                    final List<ObjectRef> states = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getStates()));
                    states.add(0, null);
                    final List<ObjectRef> priorities = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getPriorities()));
                    priorities.add(0, null);
                    final List<ObjectRef> viewstates = new ArrayList<>(
                            Arrays.asList(mr.getMasterData().getViewStates()));
                    viewstates.add(0, null);

                    Mutex.EVENT.writeAccess(new Mutex.Action<Void>() {

                        @Override
                        public Void run() {
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

                            if (filter != null) {
                                filterModel1.setBackingList(Arrays.asList(filter));
                                filterModel1.addElement(0, null);
                            }

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
                            return null;
                        }
                    });

                } catch (Exception ex) {
                    mq.getMantisRepository()
                            .getExceptionHandler()
                            .handleException(logger, "Failed to update", ex);
                }
            }
        };

        this.mr.getRequestProcessor().execute(initializer);
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

    private final Runnable updateProjectDependendLists = new Runnable() {
        // @todo: also update the other lists that are project dependend
        @Override
        public void run() {
            try (SafeAutocloseable saf = mq.busy()) {
                FlattenedProjectData selected = Mutex.EVENT.writeAccess(
                        new Mutex.Action<FlattenedProjectData>() {
                            @Override
                            public FlattenedProjectData run() {
                                return (FlattenedProjectData) projectModel1.getSelectedItem();
                            }
                        });

                final FilterData[][] filter = new FilterData[1][];

                if (selected != null) {
                    filter[0] = mr.getMasterData().getFilters(
                            selected.getProjectData().getId());
                }

                Mutex.EVENT.writeAccess(new Mutex.Action<Void>() {
                    @Override
                    public Void run() {
                        if (filter != null) {
                            filterModel1.setBackingList(Arrays.asList(filter[0]));
                            filterModel1.addElement(0, null);
                        } else {
                            filterModel1.setBackingList(Collections.EMPTY_LIST);
                        }
                        return null;
                    }
                });

            } catch (Exception ex) {
                mq.getMantisRepository()
                        .getExceptionHandler()
                        .handleException(logger, "Failed to retrieve filterlist", ex);
            }
        }
    };

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (null != e.getActionCommand()) {
            switch (e.getActionCommand()) {
                case COMMAND_OPEN_ISSUE:
                    gotoIssue();
                    break;
                case COMMAND_SELECT_PROJECT1:
                    mr.getRequestProcessor().execute(updateProjectDependendLists);
                    break;
                case COMMAND_SAVE_QUERY:
                    if (mq.getName() == null || mq.getName().isEmpty()) {
                        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                            "Name", "Save query");
                        DialogDisplayer.getDefault().notify(nd);
                        mq.setName((String) nd.getInputText());
                    }
                    new SwingWorker<Collection<MantisIssue>, Object>() {
                        @Override
                        protected Collection<MantisIssue> doInBackground() throws Exception {
                            try (SafeAutocloseable ac = mq.busy()) {
                                mq.save();
                                mq.refresh();
                                return mq.getIssues();
                            }
                        }

                        @Override
                        protected void done() {
                            try {
                                getComponent(QueryMode.VIEW).getQueryListModel().setIssues(get());
                            } catch (InterruptedException | ExecutionException ex) {
                                logger.log(Level.WARNING, "Failed to save query", ex);
                            }
                        }
                    }.execute();
                    break;
                case COMMAND_DELETE_QUERY:
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
                            } catch (InterruptedException | ExecutionException ex) {
                                logger.log(Level.WARNING, "Failed to delete query", ex);
                            }
                        }
                    }.execute();
                    break;
                case COMMAND_EXECUTE_QUERY:
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
                    SwingWorker sw = new SwingWorker<Collection<MantisIssue>, Object>() {
                        @Override
                        protected Collection<MantisIssue> doInBackground() throws Exception {
                            try (SafeAutocloseable ac = mq.busy()) {
                                mq.refresh();
                                return mq.getIssues();
                            }
                        }

                        @Override
                        protected void done() {
                            try {
                                getComponent(QueryMode.VIEW).getQueryListModel().setIssues(get());
                            } catch (InterruptedException | ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    };
                    sw.execute();
                    break;
                default:
            }
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
                } catch (ServiceException | RemoteException | RuntimeException ex) {
                    mq.getMantisRepository()
                            .getExceptionHandler()
                            .handleException(logger, "Failed to open issue", ex);
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
        return mode == QueryMode.EDIT;
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
            mqp.lastUpdateAfterDatePicker.setDate(mq.getLastUpdateAfter());
            mqp.lastUpdateBeforeDatePicker.setDate(mq.getLastUpdateBefore());
            mqp.summaryTextField.setText(mq.getSummaryFilter());
            if (mq.getCombination() == MantisQuery.Combination.ANY) {
                mqp.matchTypeComboBox.setSelectedIndex(1);
            }
            mqp.issueTable.addMouseListener(issueTableIssueOpener);
            mqp.issueTable.addKeyListener(issueTableIssueOpener);
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
        return !mq.isSaved();
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
