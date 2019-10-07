
package eu.doppel_helix.netbeans.mantisintegration.issue;

import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;

public class MantisStatusProvider implements IssueStatusProvider<MantisRepository, MantisIssue> {

    @Override
    public Status getStatus(MantisIssue i) {
        return i.getReadStatus();
    }

    @Override
    public void setSeenIncoming(MantisIssue i, boolean seen) {
        i.setRead(seen);
    }

    @Override
    public Collection<MantisIssue> getUnsubmittedIssues(MantisRepository r) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void discardOutgoing(MantisIssue i) {
    }

    @Override
    public boolean submit(MantisIssue i) {
        return false;
    }

    @Override
    public void removePropertyChangeListener(MantisIssue i, PropertyChangeListener listener) {
        i.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(MantisIssue i, PropertyChangeListener listener) {
        i.addPropertyChangeListener(listener);
    }

}
