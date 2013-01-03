
package eu.doppel_helix.netbeans.mantisintegration.repository;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import java.awt.Image;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.util.Lookup;

public class MantisRepositoryProvider extends RepositoryProvider<MantisRepository,MantisQuery,MantisIssue> {

    @Override
    public RepositoryInfo getInfo(MantisRepository r) {
        return r.getInfo();
    }

    @Override
    public Image getIcon(MantisRepository r) {
        return r.getIcon();
    }

    @Override
    public MantisIssue[] getIssues(MantisRepository r, String... ids) {
        return r.getIssues(ids);
    }

    @Override
    public void remove(MantisRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(MantisRepository r) {
        return r.getController();
    }

    @Override
    public MantisQuery createQuery(MantisRepository r) {
        return r.createQuery();
    }

    @Override
    public MantisIssue createIssue(MantisRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<MantisQuery> getQueries(MantisRepository r) {
        return r.getQueries();
    }

    @Override
    public Lookup getLookup(MantisRepository r) {
        return r.getLookup();
    }

    @Override
    public Collection<MantisIssue> simpleSearch(MantisRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }
    
}
