
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.RelationshipData;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.xml.rpc.ServiceException;
import org.jdesktop.swingx.JXHyperlink;

public class RelationshipDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(RelationshipDisplay.class.getName());
    
    private final String COMMAND_DELETE = "deleteString";
    private final String COMMAND_OPENISSUE = "openIssue";
    private final RelationshipData rd;
    private final MantisIssue issue;
    private final JXHyperlink bugButton = new JXHyperlink();
    private final JXHyperlink deleteButton = new JXHyperlink();
    private final JLabel trailingLabel = new JLabel();
    private final JLabel middleLabel = new JLabel();

    public RelationshipDisplay(MantisIssue issue, RelationshipData rd) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.rd = rd;
        this.issue = issue;
        bugButton.setText(String.format(
                "%s %d",
                rd.getType().getName(),
                rd.getTarget_id()));
        bugButton.addActionListener(this);
        bugButton.setActionCommand(COMMAND_OPENISSUE);
        middleLabel.setText(" (");
        trailingLabel.setText(")");
        this.add(bugButton);
        this.add(middleLabel);
        this.add(deleteButton);
        deleteButton.setText("delete");
        deleteButton.addActionListener(this);
        deleteButton.setActionCommand(COMMAND_DELETE);
        this.add(trailingLabel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (COMMAND_DELETE.equals(e.getActionCommand())) {
            issue.getMantisRepository().getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        issue.removeRelationship(rd);
                    } catch (ServiceException | RemoteException | RuntimeException ex) {
                        issue.getMantisRepository()
                                .getExceptionHandler()
                                .handleException(LOG, "Failed to remove relationship from issue", ex);
                    }
                }
            });
        } else if (COMMAND_OPENISSUE.equals(e.getActionCommand())) {
            issue.getMantisRepository().getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        MantisIssue mi = issue.getMantisRepository().
                                getIssues(false, rd.getTarget_id()).get(0);

                        Mantis.getInstance().getBugtrackingSupport().openIssue(
                                issue.getMantisRepository(),
                                mi);
                    } catch (ServiceException | RemoteException | RuntimeException ex) {
                        issue.getMantisRepository()
                                .getExceptionHandler()
                                .handleException(LOG, "Failed to open issue", ex);
                    }
                }
            });
        }
    }
}
