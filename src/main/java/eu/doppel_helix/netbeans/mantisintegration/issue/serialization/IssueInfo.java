package eu.doppel_helix.netbeans.mantisintegration.issue.serialization;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;

public class IssueInfo implements Cloneable {

    private BigInteger id;
    private Date scheduleDate;
    private int scheduleLength;
    private Date readState;

    public IssueInfo() {
    }

    public IssueInfo(BigInteger id) {
        this.id = id;
    }

    @XmlSchemaType(name = "date")
    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public int getScheduleLength() {
        return scheduleLength;
    }

    public void setScheduleLength(int scheduleLength) {
        this.scheduleLength = scheduleLength;
    }

    public Date getReadState() {
        return readState;
    }

    public void setReadState(Date readState) {
        this.readState = readState;
    }
    
    @XmlAttribute
    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    @XmlTransient
    public boolean isEmpty() {
        return scheduleLength == 0 && scheduleDate == null && readState == null;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.id);
        hash = 19 * hash + Objects.hashCode(this.scheduleDate);
        hash = 19 * hash + this.scheduleLength;
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
        final IssueInfo other = (IssueInfo) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.scheduleDate, other.scheduleDate)) {
            return false;
        }
        if (this.scheduleLength != other.scheduleLength) {
            return false;
        }
        return true;
    }
    
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
