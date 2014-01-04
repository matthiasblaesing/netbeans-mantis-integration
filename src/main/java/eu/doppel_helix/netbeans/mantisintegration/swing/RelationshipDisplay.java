
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.RelationshipData;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.MantisConnector;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import org.jdesktop.swingx.JXHyperlink;
import org.netbeans.modules.bugtracking.api.Repository;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class RelationshipDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private final String COMMAND_DELETE = "deleteString";
    private final String COMMAND_OPENISSUE = "openIssue";
    private RelationshipData rd;
    private MantisIssue issue;
    private JXHyperlink bugButton = new JXHyperlink();
    private JXHyperlink deleteButton = new JXHyperlink();
    private JLabel trailingLabel = new JLabel();
    private JLabel middleLabel = new JLabel();

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
                    } catch (Exception ex) {
                        NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                                "Failed to remove relationship from issue");
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                }
            });
        } else if (COMMAND_OPENISSUE.equals(e.getActionCommand())) {
            issue.getMantisRepository().getRequestProcessor().submit(new Runnable() {
                public void run() {
                    try {
                        MantisIssue mi = issue.getMantisRepository().
                                getIssues(false, rd.getTarget_id()).get(0);

                        Mantis.getInstance().getBugtrackingSupport().openIssue(
                                issue.getMantisRepository(),
                                mi);
                    } catch (Exception ex) {
                        NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                                "Failed to open issue");
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                }
            });
        }
    }
}
