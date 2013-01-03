
package eu.doppel_helix.netbeans.mantisintegration;

import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.NbBundle;

@BugtrackingConnector.Registration (
        id=MantisConnector.ID,
        displayName="#LBL_ConnectorName",
        tooltip="#LBL_ConnectorTooltip"
)    
public class MantisConnector extends BugtrackingConnector {
    public static final String ID = "eu.doppel_helix.netbeans.mantisintegration";

    public static String getConnectorName() {
        return NbBundle.getMessage(MantisConnector.class, "LBL_ConnectorName");           // NOI18N
    }
    
    public MantisConnector() {}
    
    @Override
    public Repository createRepository(RepositoryInfo info) {
        MantisRepository mr = new MantisRepository(info);
        return Mantis.getInstance().getBugtrackingFactory().createRepository(
                mr,
                Mantis.getInstance().getRepositoryProvider(),
                Mantis.getInstance().getQueryProvider(),
                Mantis.getInstance().getIssueProvider());
    }
    
    @Override
    public Repository createRepository() {
        MantisRepository mr = new MantisRepository();
        Mantis m = Mantis.getInstance();
        return m.getBugtrackingFactory().createRepository(
                mr,
                m.getRepositoryProvider(),
               m.getQueryProvider(),
                m.getIssueProvider());
    }
}
