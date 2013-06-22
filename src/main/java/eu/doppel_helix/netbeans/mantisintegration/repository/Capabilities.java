
package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.AccountData;
import eu.doppel_helix.netbeans.mantisintegration.data.Version;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;

public class Capabilities {
    private static final Version tagVersion = new Version("1.2.9");
    private static final Logger logger = Logger.getLogger(Capabilities.class.getName());
    private MantisRepository mr;
    private HashMap<BigInteger,Boolean> projectUpdater = new HashMap<BigInteger, Boolean>();
    private boolean userIsUpdaterChecked = false;
    private Boolean userIsUpdater = false;

    public Capabilities(MantisRepository mr) {
        this.mr = mr;
    }
    
    public boolean canUpdate(MantisIssue mi) {
        BigInteger projectId = mi.getProject().getId();
        
        // Try project specific variant first (will fail on large resultsets)
        
        // Check cached value -- BEWARE! NULL _is_ a valid user => unknown
        if(projectUpdater.containsKey(projectId) && projectUpdater.get(projectId) != null) {
            return projectUpdater.get(projectId);
        }
        
        // 40 is the access level for "updater"
        final BigInteger requiredAccesslevel = BigInteger.valueOf(40);
        
        // Only try this once => takes potentially extremely long
        if (projectUpdater.containsKey(projectId)) {
            try {
                AccountData[] validUsers = mr.getClient().mc_project_get_users(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectId,
                        requiredAccesslevel);
                for (AccountData ac : validUsers) {
                    if (mr.getInfo().getUsername().equals(ac.getName())) {
                        projectUpdater.put(projectId, true);
                    }
                }
                projectUpdater.put(projectId, false);
            } catch (Exception ex) {
                logger.log(Level.INFO, MessageFormat.format(
                        "Failed to retrieve updaters for project {0}",
                        projectId), ex);
                // Prevent multiple tries to retrieve users
                projectUpdater.put(projectId, null);
            }
        }
        
        
        
        // Fallback to user role
        if(userIsUpdaterChecked && userIsUpdater != null) {
            return userIsUpdater;
        }
        
        // Try this only once ...
        if(! userIsUpdaterChecked) {
            userIsUpdaterChecked = true;
            
            BigInteger userAccessLevel;
            try {
                userAccessLevel = mr.getAccount().getAccess_level();
                if (userAccessLevel.compareTo(requiredAccesslevel) >= 0) {
                    userIsUpdater = true;
                } else {
                    userIsUpdater = false;
                }
                return userIsUpdater;
            } catch (Exception ex) {
                logger.log(Level.INFO, "Failed to retrieve accesslevel for user", ex);
                userIsUpdater = null;
            }
        }
            
        // *arg* -> we can't know so asume the developer (our targetgroup)
        // knows what he is doing and allowed
        return true;
    }
    
    
    public boolean isTagSupport() throws ServiceException, RemoteException {
        return mr.getVersion().compareTo(tagVersion) >= 0;
    }
}
