
package eu.doppel_helix.netbeans.mantisintegration.issue;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;

public class MantisIssueProvider extends IssueProvider<MantisIssue> {

    @Override
    public String getDisplayName(MantisIssue data) {
        return data.getDisplayValue();
    }

    @Override
    public String getTooltip(MantisIssue data) {
        return getDisplayName(data);
    }

    @Override
    public String getID(MantisIssue data) {
        return data.getIdAsString();
    }

    @Override
    public String[] getSubtasks(MantisIssue data) {
        return data.getSubtasks();
    }

    @Override
    public String getSummary(MantisIssue data) {
        return data.getSummary();
    }

    @Override
    public boolean isNew(MantisIssue data) {
        return data.getId() == null;
    }

    @Override
    public boolean isFinished(MantisIssue data) {
        return data.isFinished();
    }

    @Override
    public boolean refresh(MantisIssue data) {
        return data.refresh();
    }

    @Override
    public void addComment(MantisIssue data, String comment, boolean closeAsFixed) {
        data.addComment(comment, closeAsFixed);
    }

    @Override
    public void attachPatch(MantisIssue data, File file, String description) {
        data.attachPatch(file, description);
    }

    @Override
    public BugtrackingController getController(MantisIssue data) {
        return data.getController();
    }

    @Override
    public void removePropertyChangeListener(MantisIssue data, PropertyChangeListener listener) {
        data.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(MantisIssue data, PropertyChangeListener listener) {
        data.addPropertyChangeListener(listener);
    }
    
}
