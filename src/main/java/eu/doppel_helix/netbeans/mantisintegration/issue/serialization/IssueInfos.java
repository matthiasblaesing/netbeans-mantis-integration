package eu.doppel_helix.netbeans.mantisintegration.issue.serialization;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IssueInfos {
    private final Set<IssueInfo> issueInfoBuffer = new HashSet<>();
    private final Map<BigInteger,IssueInfo> issueInfos = new HashMap<>();
    
    @XmlElement
    private Collection<IssueInfo> getIssueInfo() {
        return issueInfoBuffer;
    }
    
    private void beforeMarshal(final Marshaller marshaller) {
        issueInfoBuffer.addAll(issueInfos.values());
    }

    private void afterMarshal(final Marshaller marshaller) {
        issueInfoBuffer.clear();
    }

    private void beforeUnmarshal(final Unmarshaller unmarshaller,
            final Object parent) {
        issueInfoBuffer.clear();
    }

    private void afterUnmarshal(final Unmarshaller unmarshaller,
            final Object parent) {
        issueInfos.clear();
        for (IssueInfo i : issueInfoBuffer) {
            this.issueInfos.put(i.getId(), i);
        }
    }
    
    public IssueInfo getIssueInfo(BigInteger id) {
        return this.issueInfos.get(id);
    }
   
    public boolean putIssueInfo(IssueInfo i) {
        boolean isEmpty = i.isEmpty();
        if(isEmpty) {
            if(issueInfos.get(i.getId()) != null) {
                issueInfos.remove(i.getId());
                return true;
            }
            return false;
        }
        IssueInfo oldInfo = this.issueInfos.put(i.getId(), i);
        return (oldInfo == null || (! oldInfo.equals(i)));
    }
}
