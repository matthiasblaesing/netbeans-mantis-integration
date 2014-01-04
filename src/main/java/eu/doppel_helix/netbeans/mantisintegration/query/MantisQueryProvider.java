
package eu.doppel_helix.netbeans.mantisintegration.query;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class MantisQueryProvider implements QueryProvider<MantisQuery, MantisIssue> {

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
    public void remove(MantisQuery q) {
        q.remove();
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
    public boolean canRemove(MantisQuery q) {
        return true;
    }

    @Override
    public boolean canRename(MantisQuery q) {
        return false;
    }

    @Override
    public void rename(MantisQuery q, String newName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIssueContainer(MantisQuery q, IssueContainer<MantisIssue> ic) {
        q.setIssueContainer(ic);
    }
    
}
