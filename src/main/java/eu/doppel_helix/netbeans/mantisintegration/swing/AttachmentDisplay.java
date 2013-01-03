
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.AttachmentData;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.openide.util.Exceptions;

public class AttachmentDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private static File lastDirectory;
    private final String COMMAND_DELETE = "delete";
    private final String COMMAND_DOWNLOAD = "download";
    private AttachmentData ad;
    private MantisIssue issue;
    private JLabel leadingLabel = new JLabel();
    private LinkButton deleteButton = new LinkButton("delete");
    private JLabel medianLabel = new JLabel();
    private LinkButton downloadButton = new LinkButton("download");
    private JLabel trailingLabel = new JLabel();

    public AttachmentDisplay(MantisIssue issue, AttachmentData ad) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.ad = ad;
        this.issue = issue;
        leadingLabel.setText(String.format(
                "%1$s (%2$d bytes) %3$tY-%3$tm-%3$td %3$tH:%3$tM [",
                ad.getFilename(),
                ad.getSize(),
                ad.getDate_submitted()));
        medianLabel.setText(", ");
        trailingLabel.setText("]");
        this.add(leadingLabel);
        this.add(downloadButton);
        this.add(medianLabel);
        this.add(deleteButton);
        this.add(trailingLabel);
        deleteButton.addActionListener(this);
        deleteButton.setActionCommand(COMMAND_DELETE);
        downloadButton.addActionListener(this);
        downloadButton.setActionCommand(COMMAND_DOWNLOAD);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(COMMAND_DELETE.equals(e.getActionCommand())) {
            issue.getMantisRepository().getRequestProcessor().submit(new Runnable() {
                @Override
                public void run() {
                    issue.removeFile(ad);
                }
            }); 
        } if(COMMAND_DOWNLOAD.equals(e.getActionCommand())) {
            byte[] data = issue.getFile(ad);
            JFileChooser fileChooser = new JFileChooser(lastDirectory);
            fileChooser.setDialogTitle("Save attachment");
            File preselected;
            if(lastDirectory != null && lastDirectory.canWrite()) {
                preselected = new File(lastDirectory, ad.getFilename());
            } else {
                preselected = new File(ad.getFilename());
            }
            fileChooser.setSelectedFile(preselected);
            int result = fileChooser.showSaveDialog(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                FileOutputStream fos = null;
                try {
                    lastDirectory = fileChooser.getCurrentDirectory();
                    fos = new FileOutputStream(fileChooser.getSelectedFile());
                    fos.write(data);
                    fos.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    try {
                        if(fos != null) {
                            fos.close();
                        }
                    } catch (IOException ex) {}
                }
            }
        }
        
    }
}
