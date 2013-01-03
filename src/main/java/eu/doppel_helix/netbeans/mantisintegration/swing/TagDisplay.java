
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class TagDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private final String COMMAND_DELETE = "deleteString";
    private ObjectRef tag;
    private MantisIssue issue;
    private JLabel leadingLabel = new JLabel();
    private LinkButton deleteButton = new LinkButton("delete");
    private JLabel trailingLabel = new JLabel();

    public TagDisplay(MantisIssue issue, ObjectRef tag) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.tag = tag;
        this.issue = issue;
        leadingLabel.setText(String.format(
                "%s (",
                tag.getName()));
        trailingLabel.setText(")");
        this.add(leadingLabel);
        this.add(deleteButton);
        deleteButton.addActionListener(this);
        deleteButton.setActionCommand(COMMAND_DELETE);
        this.add(trailingLabel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(COMMAND_DELETE.equals(e.getActionCommand())) {
            issue.getMantisRepository().getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        issue.removeTag(tag);
                    } catch (Exception ex) {
                        NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                                "Failed to remove tag from issue");
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }
                }
            }); 
        }
    }
}
