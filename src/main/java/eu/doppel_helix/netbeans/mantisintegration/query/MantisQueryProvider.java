
package eu.doppel_helix.netbeans.mantisintegration.query;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;


public class MantisQueryProvider extends QueryProvider<MantisQuery, MantisIssue> {

    @Override
    public String getDisplayName(MantisQuery q) {
        return q.getName();
    }

    @Override
    public String getTooltip(MantisQuery q) {
        return q.getName();
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
        return q.getIssues();
    }

    @Override
    public boolean contains(MantisQuery q, String id) {
        return q.contains(id);
    }

    @Override
    public void refresh(MantisQuery query) {
        query.refresh();
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
