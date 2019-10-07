
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.xml.rpc.ServiceException;
import org.jdesktop.swingx.JXHyperlink;

public class TagDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(TagDisplay.class.getName());
    private final String COMMAND_DELETE = "deleteString";
    private final ObjectRef tag;
    private final MantisIssue issue;
    private final JLabel leadingLabel = new JLabel();
    private final JXHyperlink deleteButton = new JXHyperlink();
    private final JLabel trailingLabel = new JLabel();

    @SuppressWarnings("LeakingThisInConstructor")
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
        deleteButton.setText("delete");
        deleteButton.addActionListener(this);
        deleteButton.setActionCommand(COMMAND_DELETE);
        this.add(trailingLabel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(COMMAND_DELETE.equals(e.getActionCommand())) {
            final MantisRepository mr = issue.getMantisRepository();
            mr.getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        issue.removeTag(tag);
                    } catch (RemoteException | ServiceException ex) {
                        mr.getExceptionHandler()
                                .handleException(LOG, "Failed to remove tag from issue", ex);
                    }
                }
            });
        }
    }
}
