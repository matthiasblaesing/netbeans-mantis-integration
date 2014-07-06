
package eu.doppel_helix.netbeans.mantisintegration.issue;

import eu.doppel_helix.netbeans.mantisintegration.issue.serialization.IssueInfo;
import java.util.Date;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueScheduleProvider;

public class MantisScheduleProvider implements IssueScheduleProvider<MantisIssue>{

    @Override
    public void setSchedule(MantisIssue i, IssueScheduleInfo scheduleInfo) {
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
    }

    @Override
    public Date getDueDate(MantisIssue i) {
        return null;
    }

    @Override
    public IssueScheduleInfo getSchedule(MantisIssue i) {
        IssueInfo ii = i.getMantisRepository().getIssueInfosHandler().getIssueInfo(i.getId());
        if(ii == null || ii.getScheduleLength() == 0 || ii.getScheduleDate() == null) {
            return null;
        }
        return new IssueScheduleInfo(ii.getScheduleDate(), ii.getScheduleLength());
    }
   
    
}
