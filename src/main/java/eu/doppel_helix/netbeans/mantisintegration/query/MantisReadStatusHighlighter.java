package eu.doppel_helix.netbeans.mantisintegration.query;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.swing.UIUtils;
import java.awt.Component;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;

public class MantisReadStatusHighlighter extends AbstractHighlighter {

    @Override
    protected Component doHighlight(Component cmpnt, ComponentAdapter adapter) {
        IssueStatusProvider.Status status = ((MantisIssue) adapter.getValue(8)).getReadStatus();
        switch(status) {
            case CONFLICT:
                cmpnt.setForeground(UIUtils.getTaskConflictColor());
                break;
            case INCOMING_NEW:
                cmpnt.setForeground(UIUtils.getTaskNewColor());
                break;
            case INCOMING_MODIFIED:
                cmpnt.setForeground(UIUtils.getTaskModifiedColor());
                break;
            default:
                cmpnt.setForeground(null);
        }

        return cmpnt;
    }

}
