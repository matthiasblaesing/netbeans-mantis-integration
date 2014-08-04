
package eu.doppel_helix.netbeans.mantisintegration.query;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;

public class MantisQueryProvider implements QueryProvider<MantisQuery, MantisIssue> {
    private static final Logger LOG = Logger.getLogger(MantisQueryProvider.class.getName());

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
        } catch (ServiceException | RemoteException | RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to refresh buglist", ex);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIssueContainer(MantisQuery q, IssueContainer<MantisIssue> ic) {
        q.setIssueContainer(ic);
    }
    
}
