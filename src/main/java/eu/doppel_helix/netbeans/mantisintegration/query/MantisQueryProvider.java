
package eu.doppel_helix.netbeans.mantisintegration.query;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class MantisQueryProvider extends QueryProvider<MantisQuery, MantisIssue> {

    @Override
    public String getDisplayName(MantisQuery q) {
        return q.getName();
    }

    @Override
    public String getTooltip(MantisQuery q) {
        String tooltip = q.getName();
        if(tooltip == null) {
            tooltip = "";
        }
        return tooltip;
    }

    @Override
    public QueryController getController(MantisQuery q) {
        return q.getController();
    }

    @Override
    public boolean isSaved(MantisQuery q) {
        return q.isSaved();
    }

    @Override
    public void remove(MantisQuery q) {
        q.remove();
    }

    @Override
    public Collection<MantisIssue> getIssues(MantisQuery q) {
        try {
            return q.getIssues();
        } catch (Exception ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                    "Failed to retrieve issues");
            DialogDisplayer.getDefault().notifyLater(nd);
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public boolean contains(MantisQuery q, String id) {
        return q.contains(id);
    }

    @Override
    public void refresh(MantisQuery query) {
        try {
            query.refresh();
        } catch (Exception ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                    "Failed to refresh buglist");
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    @Override
    public void removePropertyChangeListener(MantisQuery q, PropertyChangeListener listener) {
        q.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(MantisQuery q, PropertyChangeListener listener) {
        q.addPropertyChangeListener(listener);
    }
    
}
