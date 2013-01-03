
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.RelationshipData;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.util.LinkButton;

public class RelationshipDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private final String COMMAND_DELETE = "deleteString";
    private RelationshipData rd;
    private MantisIssue issue;
    private JLabel leadingLabel = new JLabel();
    private LinkButton deleteButton = new LinkButton("delete");
    private JLabel trailingLabel = new JLabel();

    public RelationshipDisplay(MantisIssue issue, RelationshipData rd) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.rd = rd;
        this.issue = issue;
        leadingLabel.setText(String.format(
                "%s %d (",
                rd.getType().getName(),
                rd.getTarget_id()));
        trailingLabel.setText(")");
        this.add(leadingLabel);
        this.add(deleteButton);
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
                    issue.removeRelationship(rd);
                }
            });
        }
    }
}
