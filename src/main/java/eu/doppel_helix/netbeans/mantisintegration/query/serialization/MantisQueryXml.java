
package eu.doppel_helix.netbeans.mantisintegration.query.serialization;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import java.math.BigInteger;
import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * V1 Version of the serialized data format
 * 
 * @author matthias
 */
@XmlRootElement(name = "mantisQuery")
public class MantisQueryXml {
    @XmlAttribute
    private static int version = 1;
    private String id = "";
    private String name = "";
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
    private MantisQuery.Combination combination;

    public MantisQueryXml() {
    }
    
    public MantisQueryXml(MantisQuery mq) {
        this.id = mq.getId();
        this.name = mq.getName();
        this.projectId = mq.getProjectId();
        this.serversideFilterId = mq.getServersideFilterId();
        this.reporter = mq.getReporter();
        this.assignedTo = mq.getAssignedTo();
        this.category = mq.getCategory();
        this.severity = mq.getSeverity();
        this.resolution = mq.getResolution();
        this.status = mq.getStatus();
        this.priority = mq.getPriority();
        this.viewStatus = mq.getViewStatus();
        this.lastUpdateAfter = mq.getLastUpdateAfter();
        this.lastUpdateBefore = mq.getLastUpdateBefore();
        this.summaryFilter = mq.getSummaryFilter();
        this.combination = mq.getCombination();
    }
    
    public void toMantisQuery(MantisQuery target) {
        target.setId(this.id);
        target.setName(this.name);
        target.setProjectId(this.projectId);
        target.setServersideFilterId(this.serversideFilterId);
        target.setReporter(this.reporter);
        target.setAssignedTo(this.assignedTo);
        target.setCategory(this.category);
        target.setSeverity(this.severity);
        target.setResolution(this.resolution);
        target.setStatus(this.status);
        target.setPriority(this.priority);
        target.setViewStatus(this.viewStatus);
        target.setLastUpdateAfter(this.lastUpdateAfter);
        target.setLastUpdateBefore(this.lastUpdateBefore);
        target.setSummaryFilter(this.summaryFilter);
        target.setCombination(this.combination);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getProjectId() {
        return projectId;
    }

    public void setProjectId(BigInteger projectId) {
        this.projectId = projectId;
    }

    public BigInteger getServersideFilterId() {
        return serversideFilterId;
    }

    public void setServersideFilterId(BigInteger serversideFilterId) {
        this.serversideFilterId = serversideFilterId;
    }

    public AccountData getReporter() {
        return reporter;
    }

    public void setReporter(AccountData reporter) {
        this.reporter = reporter;
    }

    public AccountData getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(AccountData assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ObjectRef getSeverity() {
        return severity;
    }

    public void setSeverity(ObjectRef severity) {
        this.severity = severity;
    }

    public ObjectRef getResolution() {
        return resolution;
    }

    public void setResolution(ObjectRef resolution) {
        this.resolution = resolution;
    }

    public ObjectRef getStatus() {
        return status;
    }

    public void setStatus(ObjectRef status) {
        this.status = status;
    }

    public ObjectRef getPriority() {
        return priority;
    }

    public void setPriority(ObjectRef priority) {
        this.priority = priority;
    }

    public ObjectRef getViewStatus() {
        return viewStatus;
    }

    public void setViewStatus(ObjectRef viewStatus) {
        this.viewStatus = viewStatus;
    }

    public Date getLastUpdateAfter() {
        return lastUpdateAfter;
    }

    public void setLastUpdateAfter(Date lastUpdateAfter) {
        this.lastUpdateAfter = lastUpdateAfter;
    }

    public Date getLastUpdateBefore() {
        return lastUpdateBefore;
    }

    public void setLastUpdateBefore(Date lastUpdateBefore) {
        this.lastUpdateBefore = lastUpdateBefore;
    }

    public String getSummaryFilter() {
        return summaryFilter;
    }

    public void setSummaryFilter(String summaryFilter) {
        this.summaryFilter = summaryFilter;
    }

    @XmlJavaTypeAdapter(MantisQueryCombinationAdapter.class)
    public MantisQuery.Combination getCombination() {
        return combination;
    }

    public void setCombination(MantisQuery.Combination combination) {
        this.combination = combination;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.id.hashCode();
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
        final MantisQueryXml other = (MantisQueryXml) obj;
        if ((this.id == null) ? (other.id != null) : !this.name.equals(other.id)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.projectId != other.projectId && (this.projectId == null || !this.projectId.equals(other.projectId))) {
            return false;
        }
        if (this.serversideFilterId != other.serversideFilterId && (this.serversideFilterId == null || !this.serversideFilterId.equals(other.serversideFilterId))) {
            return false;
        }
        if (this.reporter != other.reporter && (this.reporter == null || !this.reporter.equals(other.reporter))) {
            return false;
        }
        if (this.assignedTo != other.assignedTo && (this.assignedTo == null || !this.assignedTo.equals(other.assignedTo))) {
            return false;
        }
        if ((this.category == null) ? (other.category != null) : !this.category.equals(other.category)) {
            return false;
        }
        if (this.severity != other.severity && (this.severity == null || !this.severity.equals(other.severity))) {
            return false;
        }
        if (this.resolution != other.resolution && (this.resolution == null || !this.resolution.equals(other.resolution))) {
            return false;
        }
        if (this.status != other.status && (this.status == null || !this.status.equals(other.status))) {
            return false;
        }
        if (this.priority != other.priority && (this.priority == null || !this.priority.equals(other.priority))) {
            return false;
        }
        if (this.viewStatus != other.viewStatus && (this.viewStatus == null || !this.viewStatus.equals(other.viewStatus))) {
            return false;
        }
        if (this.lastUpdateAfter != other.lastUpdateAfter && (this.lastUpdateAfter == null || !this.lastUpdateAfter.equals(other.lastUpdateAfter))) {
            return false;
        }
        if (this.lastUpdateBefore != other.lastUpdateBefore && (this.lastUpdateBefore == null || !this.lastUpdateBefore.equals(other.lastUpdateBefore))) {
            return false;
        }
        if ((this.summaryFilter == null) ? (other.summaryFilter != null) : !this.summaryFilter.equals(other.summaryFilter)) {
            return false;
        }
        if (this.combination != other.combination) {
            return false;
        }
        return true;
    }    
}
