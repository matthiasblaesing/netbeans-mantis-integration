package eu.doppel_helix.netbeans.mantisintegration.issue;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.AttachmentData;
import biz.futureware.mantisconnect.CustomFieldValueForIssueData;
import biz.futureware.mantisconnect.IssueData;
import biz.futureware.mantisconnect.IssueNoteData;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.RelationshipData;
import eu.doppel_helix.netbeans.mantisintegration.data.Permission;
import eu.doppel_helix.netbeans.mantisintegration.issue.serialization.IssueInfo;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import eu.doppel_helix.netbeans.mantisintegration.util.SafeAutocloseable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.Mutex;

public class MantisIssue {
    private final static Logger logger = Logger.getLogger(MantisIssue.class.getName());
    private final MantisRepository mr;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final IssueData issueData = new IssueData();
    private MantisIssueController mic;
    private Integer busy = 0;
    private Permission timetracking = Permission.NONE;
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

    public MantisIssue(MantisRepository mr) {
        this.mr = mr;
    }

    // Convenience methods
    public boolean isBusy() {
        return busy != 0;
    }

    private void setBusy(boolean busyBool) {
        boolean oldBusy = isBusy();
        synchronized(this) {
            if (busyBool) {
                busy++;
            } else {
                busy--;
            }
            if (busy < 0) {
                throw new IllegalStateException("Inbalanced busy/nonbusy");
            }
        }
        firePropertyChange("busy", oldBusy, isBusy());
    }

    public void setIdFromString(String id) {
        String oldValue = getIdAsString();
        if (id == null) {
            setId(null);
        } else {
            setId(new BigInteger(id));
        }
        firePropertyChange("idFromString", oldValue, getIdAsString());
    }

    public String getIdAsString() {
        BigInteger result = this.issueData.getId();
        if (result == null) {
            return null;
        } else {
            return result.toString();
        }
    }

    public String getDisplayValue() {
        if (getId() == null) {
            return "New Issue";
        } else {
            return "#" + getIdAsString() + " - " + getSummary();
        }
    }

    public List<String> getSubtasks() {
        List<String> result = new ArrayList<>();
        for (RelationshipData rd : getRelationships()) {
            if (rd.getType().getId().intValue() == 2) {
                result.add(rd.getTarget_id().toString());
            }
        }
        return result;
    }

    public boolean refresh() throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            boolean result = mr.updateIssueFromRepository(MantisIssue.this);
            timetracking = mr.getCapabilities().getTrackTime(this);
            if (result) {
                firePropertyChange(IssueProvider.EVENT_ISSUE_DATA_CHANGED, null, null);
            }
            return result;
        }
    }

    public void updateFromIssueData(final IssueData id) {
        issueData.setId(id.getId());
        issueData.setView_state(id.getView_state());
        issueData.setLast_updated(id.getLast_updated());
        issueData.setProject(id.getProject());
        issueData.setCategory(id.getCategory());
        issueData.setPriority(id.getPriority());
        issueData.setSeverity(id.getSeverity());
        issueData.setStatus(id.getStatus());
        issueData.setReporter(id.getReporter());
        issueData.setSummary(id.getSummary());
        issueData.setVersion(id.getVersion());
        issueData.setBuild(id.getBuild());
        issueData.setPlatform(id.getPlatform());
        issueData.setOs(id.getOs());
        issueData.setOs_build(id.getOs_build());
        issueData.setReproducibility(id.getReproducibility());
        issueData.setDate_submitted(id.getDate_submitted());
        issueData.setSponsorship_total(id.getSponsorship_total());
        issueData.setHandler(id.getHandler());
        issueData.setProjection(id.getProjection());
        issueData.setEta(id.getEta());
        issueData.setResolution(id.getResolution());
        issueData.setFixed_in_version(id.getFixed_in_version());
        issueData.setDescription(id.getDescription());
        issueData.setSteps_to_reproduce(id.getSteps_to_reproduce());
        issueData.setAdditional_information(id.getAdditional_information());
        issueData.setAttachments(id.getAttachments());
        issueData.setRelationships(id.getRelationships());
        issueData.setNotes(id.getNotes());
        issueData.setCustom_fields(id.getCustom_fields());
        issueData.setTags(id.getTags());
        issueData.setTarget_version(id.getTarget_version());
        issueData.setVersion(id.getVersion());
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                firePropertyChange(IssueProvider.EVENT_ISSUE_DATA_CHANGED, null, null);
            }
        });
    }

    public MantisIssueController getController() {
        if (mic == null) {
            mic = new MantisIssueController(this);
        }
        return mic;
    }

    public boolean isFinished() {
        // @todo: Respect settings of repository
        // Current implemention: isFinished = Status.ID >= 80
        return BigInteger.valueOf(80).compareTo(issueData.getStatus().getId()) <= 0;
    }

    public void attachFile(File file, String description) throws ServiceException, RemoteException, IOException {
        try (SafeAutocloseable ac = busy()) {
            mr.addFile(this, file, description);
        }
    }

    public void addComment(String comment, boolean closeAsFixed) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.addComment(this, comment, null, null);
            if(closeAsFixed) {
                if(mr.getCommitStatus() != null) {
                    this.setStatus(mr.getCommitStatus());
                }
                if(mr.getCommitResolution() != null) {
                    this.setResolution(mr.getCommitResolution());
                }
                mr.updateIssue(this, getIssueData());
            }
        }
    }

    public MantisRepository getMantisRepository() {
        return mr;
    }

    public IssueData getIssueData() {
        return this.issueData;
    }

    public void removeRelationship(RelationshipData rd) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.removeRelationship(this, rd);
        }
    }

    public void removeFile(AttachmentData ad) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.removeFile(this, ad);
        }
    }

    public byte[] getFile(AttachmentData ad) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            return mr.getFile(this, ad);
        }
    }

    public void addFile(File f, String comment) throws ServiceException, RemoteException, IOException {
        try (SafeAutocloseable ac = busy()) {
            mr.addFile(this, f, comment);
        }
    }

    public void addComment(String comment, ObjectRef viewState, BigInteger timetracking) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.addComment(this, comment, viewState, timetracking);
        }
    }

    public void addRelationship(ObjectRef type, BigInteger target) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.addRelationship(this, type, target);
        }
    }

    public void addTag(String... tagString) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.addTag(this, tagString);
        }
    }

    public void removeTag(ObjectRef... tag) throws ServiceException, RemoteException {
        try (SafeAutocloseable ac = busy()) {
            mr.removeTag(this, tag);
        }
    }

    public int getNoteCount() {
        if (issueData.getNotes() == null) {
            return 0;
        } else {
            return issueData.getNotes().length;
        }
    }

    /**
     * Check whether update of issue data and tags is possible
     */
    public boolean canUpdate() {
        // @todo: Implement a better strategy to check
        return mr.getCapabilities().canUpdate(this);
    }

    public Permission getTimetracking() {
        return timetracking;
    }

    // Property change support
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(final String property, final Object oldValue, final Object newValue) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                pcs.firePropertyChange(property, oldValue, newValue);
            }
        });
    }

    // Delegation to IssueData including property change support
    public BigInteger getId() {
        return issueData.getId();
    }

    public void setId(BigInteger id) {
        BigInteger oldValue = issueData.getId();
        String oldDisplayValue = getDisplayValue();
        issueData.setId(id);
        firePropertyChange("id", oldValue, id);
        firePropertyChange("displayValue", oldDisplayValue, getDisplayValue());
    }

    public ObjectRef getView_state() {
        return issueData.getView_state();
    }

    public void setView_state(ObjectRef view_state) {
        ObjectRef oldValue = issueData.getView_state();
        issueData.setView_state(view_state);
        firePropertyChange("view_state", oldValue, view_state);
    }

    public Calendar getLast_updated() {
        return issueData.getLast_updated();
    }

    public void setLast_updated(Calendar last_updated) {
        Calendar oldValue = issueData.getLast_updated();
        issueData.setLast_updated(last_updated);
        firePropertyChange("last_updated", oldValue, last_updated);
    }

    public ObjectRef getProject() {
        return issueData.getProject();
    }

    public void setProject(ObjectRef project) {
        ObjectRef oldValue = issueData.getProject();
        issueData.setProject(project);
        firePropertyChange("project", oldValue, project);
    }

    public String getCategory() {
        return issueData.getCategory();
    }

    public void setCategory(String category) {
        String oldValue = issueData.getCategory();
        issueData.setCategory(category);
        firePropertyChange("category", oldValue, category);
    }

    public ObjectRef getPriority() {
        return issueData.getPriority();
    }

    public void setPriority(ObjectRef priority) {
        ObjectRef oldValue = issueData.getPriority();
        issueData.setPriority(priority);
        firePropertyChange("priority", oldValue, priority);
    }

    public ObjectRef getSeverity() {
        return issueData.getSeverity();
    }

    public void setSeverity(ObjectRef severity) {
        ObjectRef oldValue = issueData.getSeverity();
        issueData.setSeverity(severity);
        firePropertyChange("severity", oldValue, severity);
    }

    public ObjectRef getStatus() {
        return issueData.getStatus();
    }

    public void setStatus(ObjectRef status) {
        ObjectRef oldValue = issueData.getStatus();
        issueData.setStatus(status);
        firePropertyChange("status", oldValue, status);
    }

    public AccountData getReporter() {
        return issueData.getReporter();
    }

    public void setReporter(AccountData reporter) {
        AccountData oldValue = issueData.getReporter();
        issueData.setReporter(reporter);
        firePropertyChange("reporter", oldValue, reporter);
    }

    public String getSummary() {
        return issueData.getSummary();
    }

    public void setSummary(String summary) {
        String oldValue = issueData.getSummary();
        String oldDisplayValue = getDisplayValue();
        issueData.setSummary(summary);
        firePropertyChange("summary", oldValue, summary);
        firePropertyChange("displayValue", oldDisplayValue, getDisplayValue());
    }

    public String getVersion() {
        return issueData.getVersion();
    }

    public void setVersion(String version) {
        String oldValue = issueData.getVersion();
        issueData.setVersion(version);
        firePropertyChange("version", oldValue, version);
    }

    public String getBuild() {
        return issueData.getBuild();
    }

    public void setBuild(String build) {
        String oldValue = issueData.getBuild();
        issueData.setBuild(build);
        firePropertyChange("build", oldValue, build);
    }

    public String getPlatform() {
        return issueData.getPlatform();
    }

    public void setPlatform(String platform) {
        String oldValue = issueData.getPlatform();
        issueData.setPlatform(platform);
        firePropertyChange("platform", oldValue, platform);
    }

    public String getOs() {
        return issueData.getOs();
    }

    public void setOs(String os) {
        String oldValue = issueData.getOs();
        issueData.setOs(os);
        firePropertyChange("os", oldValue, os);
    }

    public String getOs_build() {
        return issueData.getOs_build();
    }

    public void setOs_build(String os_build) {
        String oldValue = issueData.getOs_build();
        issueData.setOs_build(os_build);
        firePropertyChange("os_build", oldValue, os_build);
    }

    public ObjectRef getReproducibility() {
        return issueData.getReproducibility();
    }

    public void setReproducibility(ObjectRef reproducibility) {
        ObjectRef oldValue = issueData.getReproducibility();
        issueData.setReproducibility(reproducibility);
        firePropertyChange("reproducibility", oldValue, reproducibility);
    }

    public Calendar getDate_submitted() {
        return issueData.getDate_submitted();
    }

    public void setDate_submitted(Calendar date_submitted) {
        Calendar oldValue = issueData.getDate_submitted();
        issueData.setDate_submitted(date_submitted);
        firePropertyChange("date_submitted", oldValue, date_submitted);
    }

    public BigInteger getSponsorship_total() {
        return issueData.getSponsorship_total();
    }

    public void setSponsorship_total(BigInteger sponsorship_total) {
        BigInteger oldValue = issueData.getSponsorship_total();
        issueData.setSponsorship_total(sponsorship_total);
        firePropertyChange("sponsorship_total", oldValue, sponsorship_total);
    }

    public AccountData getHandler() {
        return issueData.getHandler();
    }

    public void setHandler(AccountData handler) {
        AccountData oldValue = issueData.getHandler();
        issueData.setHandler(handler);
        firePropertyChange("handler", oldValue, handler);
    }

    public ObjectRef getProjection() {
        return issueData.getProjection();
    }

    public void setProjection(ObjectRef projection) {
        ObjectRef oldValue = issueData.getProjection();
        issueData.setProjection(projection);
        firePropertyChange("projection", oldValue, projection);
    }

    public ObjectRef getEta() {
        return issueData.getEta();
    }

    public void setEta(ObjectRef eta) {
        ObjectRef oldValue = issueData.getEta();
        issueData.setEta(eta);
        firePropertyChange("eta", oldValue, eta);
    }

    public ObjectRef getResolution() {
        return issueData.getResolution();
    }

    public void setResolution(ObjectRef resolution) {
        ObjectRef oldValue = issueData.getResolution();
        issueData.setResolution(resolution);
        firePropertyChange("resolution", oldValue, resolution);
    }

    public String getFixed_in_version() {
        return issueData.getFixed_in_version();
    }

    public void setFixed_in_version(String fixed_in_version) {
        String oldValue = issueData.getFixed_in_version();
        issueData.setFixed_in_version(fixed_in_version);
        firePropertyChange("fixed_in_version", oldValue, fixed_in_version);
    }

    public String getDescription() {
        return issueData.getDescription();
    }

    public void setDescription(String description) {
        String oldValue = issueData.getDescription();
        issueData.setDescription(description);
        firePropertyChange("description", oldValue, description);
    }

    public String getSteps_to_reproduce() {
        return issueData.getSteps_to_reproduce();
    }

    public void setSteps_to_reproduce(String steps_to_reproduce) {
        String oldValue = issueData.getSteps_to_reproduce();
        issueData.setSteps_to_reproduce(steps_to_reproduce);
        firePropertyChange("steps_to_reproduce", oldValue, steps_to_reproduce);
    }

    public String getAdditional_information() {
        return issueData.getAdditional_information();
    }

    public void setAdditional_information(String additional_information) {
        String oldValue = issueData.getAdditional_information();
        issueData.setAdditional_information(additional_information);
        firePropertyChange("additional_information", oldValue, additional_information);
    }

    public AttachmentData[] getAttachments() {
        return issueData.getAttachments();
    }

    public void setAttachments(AttachmentData[] attachments) {
        AttachmentData[] oldValue = issueData.getAttachments();
        issueData.setAttachments(attachments);
        firePropertyChange("attachments", oldValue, attachments);
    }

    public RelationshipData[] getRelationships() {
        return issueData.getRelationships();
    }

    public void setRelationships(RelationshipData[] relationships) {
        RelationshipData[] oldValue = issueData.getRelationships();
        issueData.setRelationships(relationships);
        firePropertyChange("relationships", oldValue, relationships);
    }

    public IssueNoteData[] getNotes() {
        return issueData.getNotes();
    }

    public void setNotes(IssueNoteData[] notes) {
        IssueNoteData[] oldValue = issueData.getNotes();
        issueData.setNotes(notes);
        firePropertyChange("notes", oldValue, notes);
    }

    public CustomFieldValueForIssueData[] getCustom_fields() {
        CustomFieldValueForIssueData[] value = issueData.getCustom_fields();
        if(value == null) {
            value = new CustomFieldValueForIssueData[0];
        }
        return value;
    }

    public void setCustom_fields(CustomFieldValueForIssueData[] custom_fields) {
        if(custom_fields == null) {
            custom_fields = new CustomFieldValueForIssueData[0];
        }
        CustomFieldValueForIssueData[] oldValue = issueData.getCustom_fields();
        issueData.setCustom_fields(custom_fields);
        firePropertyChange("custom_fields", oldValue, custom_fields);
    }

    public String getTarget_version() {
        return issueData.getTarget_version();
    }

    public void setTarget_version(String version) {
        String oldValue = issueData.getTarget_version();
        issueData.setTarget_version(version);
        firePropertyChange("target_version", oldValue, version);
    }

    public ObjectRef[] getTags() {
        return issueData.getTags();
    }

    public void setTags(ObjectRef[] tags) {
        ObjectRef[] oldValue = issueData.getTags();
        issueData.setTags(tags);
        firePropertyChange("tags", oldValue, tags);
    }

    public IssueStatusProvider.Status getReadStatus() {
        Date readDate = getReadDate();
        if (readDate == null) {
            return IssueStatusProvider.Status.INCOMING_NEW;
        } else if (getLast_updated().getTime().after(readDate)) {
            return IssueStatusProvider.Status.INCOMING_MODIFIED;
        } else {
            return IssueStatusProvider.Status.SEEN;
        }
    }

    public Date getReadDate() {
        IssueInfo ii = getMantisRepository().getIssueInfosHandler().getIssueInfo(getId());
        if (ii == null) {
            return null;
        } else {
            return ii.getReadState();
        }
    }

    public void setRead(boolean read) {
        IssueInfo ii = getMantisRepository().getIssueInfosHandler().getIssueInfo(getId());
        if (ii == null) {
            ii = new IssueInfo(getId());
        }
        if (read) {
            ii.setReadState(new Date());
        } else {
            ii.setReadState(null);
        }
        getMantisRepository().getIssueInfosHandler().putIssueInfo(ii);
        firePropertyChange(IssueStatusProvider.EVENT_STATUS_CHANGED, null, null);
    }
}
