
package eu.doppel_helix.netbeans.mantisintegration.issue;

import biz.futureware.mantisconnect.CustomFieldValueForIssueData;
import biz.futureware.mantisconnect.IssueData;
import eu.doppel_helix.netbeans.mantisintegration.issue.serialization.IssueInfo;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueScheduleProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.Exceptions;

public class MantisScheduleProvider implements IssueScheduleProvider<MantisIssue>{
    private static final Logger LOG = Logger.getLogger(MantisScheduleProvider.class.getName());
    
    @Override
    public void setSchedule(final MantisIssue i, IssueScheduleInfo scheduleInfo) {
        IssueInfo ii = i.getMantisRepository().getIssueInfosHandler().getIssueInfo(i.getId());
        if(ii == null) {
            ii = new IssueInfo(i.getId());
        }
        
        if(scheduleInfo != null) {
            ii.setScheduleDate(scheduleInfo.getDate());
            ii.setScheduleLength(scheduleInfo.getInterval());
        } else {
            ii.setScheduleDate(null);
            ii.setScheduleLength(0);
        }
        i.getMantisRepository().getIssueInfosHandler().putIssueInfo(ii);
        
        RepositoryInfo ri = i.getMantisRepository().getInfo();
        String dateField = ri.getValue(MantisRepository.PROP_SCHEDULE_DATE_FIELD);
        String lengthField = ri.getValue(MantisRepository.PROP_SCHEDULE_LENGTH_FIELD);
                boolean dateFieldFound = false;
        boolean lengthFieldFound = false;

        final IssueData id = i.getIssueData();
        
        CustomFieldValueForIssueData[] cfvfids = id.getCustom_fields();
        for (CustomFieldValueForIssueData cfvfid : cfvfids) {
            if (dateField != null
                    && dateField.equalsIgnoreCase(cfvfid.getField().getName())) {
                if(scheduleInfo != null && scheduleInfo.getDate() != null) {
                    cfvfid.setValue(Long.toString(scheduleInfo.getDate().getTime() / 1000));
                } else {
                    cfvfid.setValue(null);
                }
                dateFieldFound = true;
            } else if (lengthField != null
                    && lengthField.equalsIgnoreCase(cfvfid.getField().getName())) {
                if(scheduleInfo != null && scheduleInfo.getInterval() != 0) {
                    cfvfid.setValue(Integer.toString(scheduleInfo.getInterval()));
                } else {
                    cfvfid.setValue(null);
                }
                lengthFieldFound = true;
            }
        }
        
        if (dateField != null && (!dateFieldFound)) {
            LOG.log(Level.WARNING, String.format(
                    "Mantis-Custom Field for schedule date was not found: %s",
                    dateField));
        }

        if (lengthField != null && (!lengthFieldFound)) {
            LOG.log(Level.WARNING, String.format(
                    "Mantis-Custom Field for schedule length was not found: %s",
                    dateField));
        }
        
        Runnable updater = new Runnable() {
            public void run() {
                try {
                    MantisRepository mr = i.getMantisRepository();
                    mr.updateIssue(i, id);
                } catch (ServiceException |RemoteException ex) {
                    LOG.log(Level.WARNING, "Failed to update issue", ex);
                }
            }
        };
        
        if(SwingUtilities.isEventDispatchThread()) {
            i.getMantisRepository().getRequestProcessor().execute(updater);
        } else {
            updater.run();
        }
    }

    @Override
    public Date getDueDate(MantisIssue i) {
        return null;
    }

    @Override
    public IssueScheduleInfo getSchedule(MantisIssue i) {
        IssueInfo ii = i.getMantisRepository().getIssueInfosHandler().getIssueInfo(i.getId());
        
        Date scheduleDate = null;
        int scheduleLength = 0;
        
        if (ii != null && ii.getScheduleLength() != 0 && ii.getScheduleDate()
                != null) {
            scheduleDate = ii.getScheduleDate();
            scheduleLength = ii.getScheduleLength();
        }
        
        RepositoryInfo ri = i.getMantisRepository().getInfo();
        String dateField = ri.getValue(MantisRepository.PROP_SCHEDULE_DATE_FIELD);
        String lengthField = ri.getValue(MantisRepository.PROP_SCHEDULE_LENGTH_FIELD);
        boolean dateFieldFound = false;
        boolean lengthFieldFound = false;
        
        CustomFieldValueForIssueData[] cfvfids = i.getCustom_fields();
        for(CustomFieldValueForIssueData cfvfid: cfvfids) {
            if(dateField != null && dateField.equalsIgnoreCase(cfvfid.getField().getName())) {
                Date extractedDate = extractDate(cfvfid);
                if(extractedDate != null) {
                    scheduleDate = extractedDate;
                }
                dateFieldFound = true;
            } else if (lengthField != null  && lengthField.equalsIgnoreCase(cfvfid.getField().getName())) {
                int extractedScheduleLength = extractInt(cfvfid);
                if(extractedScheduleLength > 0) {
                    scheduleLength = extractedScheduleLength;
                }
                lengthFieldFound = true;
            }
        }
        
        if (dateField != null && (!dateFieldFound)) {
            LOG.log(Level.WARNING, String.format(
                    "Mantis-Custom Field for schedule date was not found: %s",
                    dateField));
        }
        
        if (lengthField != null && (!lengthFieldFound)) {
            LOG.log(Level.WARNING, String.format(
                    "Mantis-Custom Field for schedule length was not found: %s",
                    dateField));
        }
        
        if(scheduleDate == null || scheduleLength == 0) {
            return null;
        } else {
            return new IssueScheduleInfo(scheduleDate, scheduleLength);
        }
    }
   
    
    private Date extractDate(CustomFieldValueForIssueData cfvfid) {
        if (cfvfid.getValue() == null || "".equals(cfvfid.getValue())) {
            return null;
        } else {
            try {
                long secondsSinceEpoch = Long.parseLong(cfvfid.getValue());
                return new Date(secondsSinceEpoch * 1000);
            } catch (NumberFormatException ex) {
                LOG.log(Level.WARNING, String.format(
                        "Mantis-Custom Field was not parsable as a datefield: %s (ID: %d)",
                        cfvfid.getField().getName(), cfvfid.getField().getId()), ex);
                return null;
            }
        }
    }

    private Integer extractInt(CustomFieldValueForIssueData cfvfid) {
        if (cfvfid.getValue() == null || "".equals(cfvfid.getValue())) {
            return 0;
        } else {
            try {
                return Integer.parseInt(cfvfid.getValue());
            } catch (NumberFormatException ex) {
                LOG.log(Level.WARNING, String.format(
                        "Mantis-Custom Field was not parsable as an integer: %s (ID: %d)",
                        cfvfid.getField().getName(), cfvfid.getField().getId()), ex);
                return 0;
            }
        }
    }
}
