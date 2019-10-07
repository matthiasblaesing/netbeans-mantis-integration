package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.FilterSearchData;
import biz.futureware.mantisconnect.IssueHeaderData;
import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import eu.doppel_helix.netbeans.mantisintegration.util.SafeAutocloseable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;

public class MantisQuery {
    public enum Combination {
        ALL,
        ANY
    }
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final MantisRepository mr;
    private QueryProvider.IssueContainer<MantisIssue> issueContainer;
    private MantisQueryController mqc;
    private String id = UUID.randomUUID().toString();
    private String name = null;
    private BigInteger projectId;
    private BigInteger serversideFilterId;
    private AccountData reporter;
    private AccountData assignedTo;
    private String category;
    private ObjectRef severity;
    private ObjectRef resolution;
    private ObjectRef status;
    private ObjectRef priority;
    private ObjectRef viewStatus;
    private Date lastUpdateAfter;
    private Date lastUpdateBefore;
    private String summaryFilter;
    private Combination combination = Combination.ALL;
    private final Set<String> matchingIds = new HashSet<>();
    private Integer busy = 0;
    private boolean saved = false;
    private final SafeAutocloseable busyHelper = new SafeAutocloseable() {
        @Override
        public void close() {
            setBusy(false);
        }
    };

    public SafeAutocloseable busy() {
        setBusy(true);
        return busyHelper;
    }

    public MantisQuery(MantisRepository mr) {
        this.mr = mr;
    }

    MantisRepository getMantisRepository() {
        return mr;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public void save() {
        mr.saveQuery(this, true);
        setSaved(true);
    }

    public void remove() {
        matchingIds.clear();
        mr.deleteQuery(id);
    }

    public Collection<MantisIssue> getIssues() throws ServiceException, RemoteException {
        return mr.getIssues(true, matchingIds.toArray(new String[matchingIds.size()]));
    }

    /**
     * DO NOT USE THIS! It is only here for serialization!
     *
     * @return
     */
    public Set<String> getMatchingIds() {
        return matchingIds;
    }

    public boolean contains(String id) {
        return matchingIds.contains(id);
    }

    public void refresh() throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            if (issueContainer != null) {
                issueContainer.refreshingStarted();
            }

            matchingIds.clear();
            for (MantisIssue mi : mr.findIssues(this)) {
                matchingIds.add(mi.getIdAsString());
            }

            if(isSaved()) {
                mr.saveQuery(this, false);
            }

            // Assumption: this is called off the EDT and should do the heavy
            // lifting, while getIssues is called on the EDT and needs to be
            // lightweight
            List<MantisIssue> mis = mr.getIssues(
                    false, matchingIds.toArray(new String[matchingIds.size()]));

            if (issueContainer != null) {
                issueContainer.clear();
                issueContainer.add(mis.toArray(new MantisIssue[mis.size()]));
                issueContainer.refreshingFinished();
            }
        }
    }

    public QueryController getController() {
        if (mqc == null) {
            mqc = new MantisQueryController(this);
        }
        return mqc;
    }

    private void firePropertyChanged(final String property, final Object oldValue, final Object newValue) {
        pcs.firePropertyChange(property, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public synchronized boolean isBusy() {
        return busy != 0;
    }

    private synchronized void setBusy(boolean busyBool) {
        boolean oldBusy = isBusy();
        if (busyBool) {
            busy++;
        } else {
            busy--;
        }
        if (busy < 0) {
            throw new IllegalStateException("Inbalanced busy/nonbusy");
        }
        firePropertyChanged("busy", oldBusy, isBusy());
    }

    public BigInteger getProjectId() {
        return projectId;
    }

    public void setProjectId(BigInteger filterProjectId) {
        BigInteger oldId = this.projectId;
        this.projectId = filterProjectId;
        pcs.firePropertyChange("projectId", oldId, this.projectId);
    }

    public BigInteger getServersideFilterId() {
        return serversideFilterId;
    }

    public void setServersideFilterId(BigInteger filterId) {
        BigInteger oldId = this.serversideFilterId;
        this.serversideFilterId = filterId;
        pcs.firePropertyChange("filterId", oldId, this.serversideFilterId);
    }

    public AccountData getReporter() {
        return reporter;
    }

    public void setReporter(AccountData reporter) {
        AccountData oldValue = this.reporter;
        this.reporter = reporter;
        pcs.firePropertyChange("reporter", oldValue, reporter);
    }

    public AccountData getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(AccountData assignedTo) {
        AccountData oldValue = this.assignedTo;
        this.assignedTo = assignedTo;
        pcs.firePropertyChange("assignedTo", oldValue, assignedTo);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        String oldValue = this.category;
        this.category = category;
        pcs.firePropertyChange("category", oldValue, category);
    }

    public ObjectRef getSeverity() {
        return severity;
    }

    public void setSeverity(ObjectRef severity) {
        ObjectRef oldValue = this.severity;
        this.severity = severity;
        pcs.firePropertyChange("severity", oldValue, severity);
    }

    public ObjectRef getResolution() {
        return resolution;
    }

    public void setResolution(ObjectRef resolution) {
        ObjectRef oldValue = this.resolution;
        this.resolution = resolution;
        pcs.firePropertyChange("resolution", oldValue, resolution);
    }

    public ObjectRef getStatus() {
        return status;
    }

    public void setStatus(ObjectRef status) {
        ObjectRef oldValue = this.status;
        this.status = status;
        pcs.firePropertyChange("status", oldValue, status);
    }

    public ObjectRef getPriority() {
        return priority;
    }

    public void setPriority(ObjectRef priority) {
        ObjectRef oldValue = this.priority;
        this.priority = priority;
        pcs.firePropertyChange("priority", oldValue, priority);
    }

    public ObjectRef getViewStatus() {
        return viewStatus;
    }

    public void setViewStatus(ObjectRef viewStatus) {
        ObjectRef oldValue = this.viewStatus;
        this.viewStatus = viewStatus;
        pcs.firePropertyChange("viewStatus", oldValue, viewStatus);
    }

    public Date getLastUpdateAfter() {
        return lastUpdateAfter;
    }

    public void setLastUpdateAfter(Date lastUpdateAfter) {
        Date oldValue = this.lastUpdateAfter;
        this.lastUpdateAfter = lastUpdateAfter;
        pcs.firePropertyChange("lastUpdateAfter", oldValue, lastUpdateAfter);
    }

    public Date getLastUpdateBefore() {
        return lastUpdateBefore;
    }

    public void setLastUpdateBefore(Date lastUpdateBefore) {
        Date oldValue = this.lastUpdateBefore;
        this.lastUpdateBefore = lastUpdateBefore;
        pcs.firePropertyChange("lastUpdateBefore", oldValue, lastUpdateBefore);
    }

    public String getSummaryFilter() {
        return summaryFilter;
    }

    public void setSummaryFilter(String summaryFilter) {
        String oldValue = this.summaryFilter;
        this.summaryFilter = summaryFilter;
        pcs.firePropertyChange("summaryFilter", oldValue, summaryFilter);
    }

    public Combination getCombination() {
        return combination;
    }

    public void setCombination(Combination combination) {
        if(combination == null) {
            combination = Combination.ALL;
        }
        Combination oldValue = this.combination;
        this.combination = combination;
        pcs.firePropertyChange("combination", oldValue, combination);
    }

    public QueryProvider.IssueContainer<MantisIssue> getIssueContainer() {
        return issueContainer;
    }

    public void setIssueContainer(QueryProvider.IssueContainer<MantisIssue> issueContainer) {
        this.issueContainer = issueContainer;
    }

    public boolean matchesFilter(IssueHeaderData id) {
        int matches = 0;
        int checks = 0;

        if(getReporter() != null) {
            checks++;
            if(getReporter().getId().equals(id.getReporter())) {
                matches++;
            }
        }
        if(getAssignedTo() != null) {
            checks++;
            if(id.getHandler() != null && getAssignedTo().getId().equals(id.getHandler())) {
                matches++;
            }
        }
        if(getCategory() != null) {
            checks++;
            if(getCategory().equals(id.getCategory())) {
                matches++;
            }
        }
        if(getSeverity() != null) {
            checks++;
            if(getSeverity().getId().equals(id.getSeverity())) {
                matches++;
            }
        }
        if(getResolution() != null) {
            checks++;
            if(getResolution().getId().equals(id.getResolution())) {
                matches++;
            }
        }
        if(getStatus() != null) {
            checks++;
            if(getStatus().getId().equals(id.getStatus())) {
                matches++;
            }
        }
        if(getPriority() != null) {
            checks++;
            if(getPriority().getId().equals(id.getPriority())) {
                matches++;
            }
        }
        if(getViewStatus() != null) {
            checks++;
            if(getViewStatus().getId().equals(id.getView_state())) {
                matches++;
            }
        }
        if(getProjectId() != null && (! getProjectId().equals(BigInteger.ZERO)) ) {
            checks++;
            if(getProjectId().equals(id.getProject())) {
                matches++;
            }
        }
        if(getLastUpdateAfter() != null) {
            checks++;
            if(getLastUpdateAfter().before(id.getLast_updated().getTime())) {
                matches++;
            }
        }
        if(getLastUpdateBefore() != null) {
            checks++;
            if(getLastUpdateBefore().after(id.getLast_updated().getTime())) {
                matches++;
            }
        }
        if(getSummaryFilter() != null && (! getSummaryFilter().isEmpty())) {
            checks++;
            Pattern p = null;
            try {
                p = Pattern.compile(getSummaryFilter());
            } catch (PatternSyntaxException ex) {}
            if(id.getSummary().contains(getSummaryFilter()) || (p != null && p.matcher(id.getSummary()).find())) {
                matches++;
            }
        }

        switch (combination) {
            default:
            case ALL:
                return matches == checks;
            case ANY:
                return matches > 0;
        }
    }

    /**
     * Build a prefilter to do pre-filtering on the server side
     *
     * @return
     */
    public FilterSearchData getAsServerFilter() {
        FilterSearchData fsd = new FilterSearchData();

        if (this.getCombination() == Combination.ALL) {
            if (getReporter() != null) {
                fsd.setReporter_id(new BigInteger[]{getReporter().getId()});
            }
            if (getAssignedTo() != null) {
                fsd.setHandler_id(new BigInteger[]{getAssignedTo().getId()});
            }
            if (getCategory() != null) {
                fsd.setCategory(new String[]{getCategory()});
            }
            if (getSeverity() != null) {
                fsd.setSeverity_id(new BigInteger[]{getSeverity().getId()});
            }
            if (getResolution() != null) {
                fsd.setResolution_id(new BigInteger[]{getResolution().getId()});
            }
            if (getStatus() != null) {
                fsd.setStatus_id(new BigInteger[]{getStatus().getId()});
            }
            if (getPriority() != null) {
                fsd.setPriority_id(new BigInteger[]{getPriority().getId()});
            }
            if (getViewStatus() != null) {
                fsd.setView_state_id(new BigInteger[]{getViewStatus().getId()});
            }
            if (getProjectId() != null) {
                fsd.setProject_id(new BigInteger[]{getProjectId()});
            }
        } else {
            if (getProjectId() != null && getProjectId().compareTo(BigInteger.ZERO) != 0) {
                fsd.setProject_id(new BigInteger[]{getProjectId()});
            }
        }
        return fsd;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MantisQuery other = (MantisQuery) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
