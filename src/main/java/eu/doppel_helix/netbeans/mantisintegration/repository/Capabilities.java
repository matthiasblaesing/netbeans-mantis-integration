
package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.MantisConnectPortType;
import eu.doppel_helix.netbeans.mantisintegration.data.Permission;
import eu.doppel_helix.netbeans.mantisintegration.data.Version;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisFault;

public class Capabilities {
    private static final Version tagVersion = new Version("1.2.9");
    private static final Logger logger = Logger.getLogger(Capabilities.class.getName());
    private final MantisRepository mr;
    private final HashMap<BigInteger,Boolean> projectUpdater = new HashMap<>();
    private boolean userIsUpdaterChecked = false;
    private Boolean userIsUpdater = false;
    private final HashMap<BigInteger,Permission> projectTimetracker = new HashMap<>();
    private boolean trackTimeChecked = false;
    private boolean trackTime = false;
    private Permission trackTimeGlobal = null;
    private boolean trackTimeGlobalChecked = false;

    public Capabilities(MantisRepository mr) {
        this.mr = mr;
    }
    
    public boolean canUpdate(MantisIssue mi) {
        // While updating MantisIssue could be not update and project not yet assigned
        if(mi == null || mi.getProject() == null) {
            return false;
        }
        
        BigInteger projectId = mi.getProject().getId();
        
        // Try project specific variant first (will fail on large resultsets)
        
        // Check cached value -- BEWARE! NULL _is_ a valid user => unknown
        if(projectUpdater.containsKey(projectId) && projectUpdater.get(projectId) != null) {
            return projectUpdater.get(projectId);
        }
        
        // 40 is the access level for "updater"
        final BigInteger requiredAccesslevel = BigInteger.valueOf(40);
        
        // Only try this once => takes potentially extremely long
        if (! projectUpdater.containsKey(projectId)) {
            try {
                AccountData[] validUsers = mr.getClient().mc_project_get_users(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectId,
                        requiredAccesslevel);
                for (AccountData ac : validUsers) {
                    if (mr.getInfo().getUsername().equalsIgnoreCase(ac.getName())) {
                        projectUpdater.put(projectId, true);
                        return true;
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
    
    public Permission getTrackTime(MantisIssue mi) throws RemoteException {
        // While updating MantisIssue could be not update and project not yet assigned
        // Fallback to WRITE permission
        if(mi == null || mi.getProject() == null) {
            return Permission.WRITE;
        }
        
        BigInteger projectId = mi.getProject().getId();

        // Try project specific variant first (will fail on large resultsets)

        // Check cached value -- BEWARE! NULL _is_ a valid user => unknown
        if (projectTimetracker.containsKey(projectId) && projectTimetracker.get(projectId) != null) {
            return projectTimetracker.get(projectId);
        }

        if (this.trackTimeChecked && (!trackTime)) {
            return Permission.NONE;
        }

        trackTime = false;


        MantisConnectPortType mcpt;
        try {
            mcpt = mr.getClient();
        } catch (ServiceException ex) {
            logger.log(Level.WARNING, "Failed to access bugtracker to retrieve timetracking info");
            return Permission.NONE;
        }

        // The following assumes, that a failure means the backing system
        // does not support time tracking -- the AxisFault indicates
        // a problem on the logical layer, not somewhere beneath
        try {
            String enabled = mcpt.mc_config_get_string(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()),
                    "time_tracking_enabled");
            if("1".equals(enabled)) {
                trackTime = true;
            } else {
                trackTime = false;
            }
            trackTimeChecked = true;
        } catch (AxisFault af) {
            trackTimeChecked = true;
            return Permission.NONE;
        }

        if (! trackTime) {
            return Permission.NONE;
        }

        BigInteger viewThreshold = BigInteger.valueOf(55); // Default: Developer
        BigInteger editThreshold = BigInteger.valueOf(55); // Default: Developer

        try {
            String viewThresholdString = mcpt.mc_config_get_string(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()),
                    "time_tracking_view_threshold");
            String editThresholdString = mcpt.mc_config_get_string(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()),
                    "time_tracking_edit_threshold");
            viewThreshold = new BigInteger(viewThresholdString);
            editThreshold = new BigInteger(editThresholdString);
        } catch (Exception ex) {
        }

        // Only try this once => takes potentially extremely long
        if (! projectTimetracker.containsKey(projectId)) {
            try {
                // Check for EDIT access
                AccountData[] validUsers = mr.getClient().mc_project_get_users(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectId,
                        editThreshold);
                for (AccountData ac : validUsers) {
                    if (mr.getInfo().getUsername().equalsIgnoreCase(ac.getName())) {
                        projectTimetracker.put(projectId, Permission.WRITE);
                        return Permission.WRITE;
                    }
                }
                
                // Check for READ access
                validUsers = mr.getClient().mc_project_get_users(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectId,
                        viewThreshold);
                for (AccountData ac : validUsers) {
                    if (mr.getInfo().getUsername().equalsIgnoreCase(ac.getName())) {
                        projectTimetracker.put(projectId, Permission.READ);
                        return Permission.READ;
                    }
                }
                
                projectTimetracker.put(projectId, Permission.NONE);
                
                return Permission.NONE;
            } catch (Exception ex) {
                logger.log(Level.INFO, MessageFormat.format(
                        "Failed to retrieve updaters for project {0}",
                        projectId), ex);
                // Prevent multiple tries to retrieve users
                projectUpdater.put(projectId, null);
            }
        }

        // Try this only once ...
        if (! trackTimeGlobalChecked) {
            trackTimeGlobalChecked = true;

            try {
                BigInteger userAccessLevel = mr.getAccount().getAccess_level();
                if (userAccessLevel.compareTo(editThreshold) >= 0) {
                    trackTimeGlobal = Permission.WRITE;
                } else if (userAccessLevel.compareTo(viewThreshold) >= 0) {
                    trackTimeGlobal = Permission.READ;
                } else {
                    trackTimeGlobal = Permission.NONE;
                }
                return trackTimeGlobal;
            } catch (ServiceException | RemoteException ex) {
                logger.log(Level.INFO, "Failed to retrieve accesslevel for user", ex);
                userIsUpdater = null;
            }
        } else if (trackTimeGlobal != null) {
            return trackTimeGlobal;
        }

        // If time tracking is enabled and we can't fetch project specific
        // settings, we fall back to write access...
        return Permission.WRITE;
    }
}
