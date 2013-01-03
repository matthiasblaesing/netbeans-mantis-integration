
package eu.doppel_helix.netbeans.mantisintegration.repository;

import eu.doppel_helix.netbeans.mantisintegration.MantisConnector;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class MantisRepositoryController implements RepositoryController, DocumentListener {
    private final MantisRepository repository;
    private MantisRepositoryPanel panel;
    private final List<String> errorMessages = new ArrayList<String>();
    private ChangeSupport cs = new ChangeSupport(this);
    
    public MantisRepositoryController(MantisRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public JComponent getComponent() {
        if(panel == null) {
            panel = new MantisRepositoryPanel();
            panel.nameTextField.getDocument().addDocumentListener(this);
            panel.urlTextField.getDocument().addDocumentListener(this);
            panel.usernameTextField.getDocument().addDocumentListener(this);
            panel.passwordTextField.getDocument().addDocumentListener(this);
            populate();
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public boolean isValid() {
        errorMessages.clear();
        String name = panel.nameTextField.getText();
        String url = panel.urlTextField.getText();
        String username = panel.usernameTextField.getText();
        String password = panel.passwordTextField.getText();
        
        if(name.isEmpty()) {
            errorMessages.add("No name specified");
        }
        
        try {
            new URI(url);
            URI.create(url).toURL();
        } catch (Exception ex) {
            errorMessages.add("Invalid URL provided");
        }
        
        if(username.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessages.add("Username or password empty");
        }
        
        return errorMessages.size() == 0;
    }

    @Override
    public void populate() {
        RepositoryInfo ri = repository.getInfo();
        if(panel != null && ri != null) {
            panel.nameTextField.setText(ri.getDisplayName());
            panel.urlTextField.setText(ri.getUrl());
            panel.usernameTextField.setText(ri.getUsername());
            panel.passwordTextField.setText(new String(ri.getPassword()));
        }
    }

    @Override
    public String getErrorMessage() {
        StringBuilder message = new StringBuilder();
        for(String m: errorMessages) {
            if(message.length() != 0) {
                message.append(", ");
            }
            message.append(m);
        }
        return message.toString();
    }

    @Override
    public void applyChanges() throws IOException {
        String name = panel.nameTextField.getText();
        String url = panel.urlTextField.getText();
        String username = panel.usernameTextField.getText();
        String password = panel.passwordTextField.getText();
        RepositoryInfo ri = repository.getInfo();
        String id = null;
        if( ri != null && ri.getId() != null) {
            id = ri.getId();
        }
          
        if(id == null) {
            id = MantisConnector.ID + System.currentTimeMillis();
        }
        ri = new RepositoryInfo(id, MantisConnector.ID, url, name, "", username, "", password.toCharArray(), "".toCharArray());
        repository.setInfo(ri);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        cs.fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        cs.fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        cs.fireChange();
    }
    
}
