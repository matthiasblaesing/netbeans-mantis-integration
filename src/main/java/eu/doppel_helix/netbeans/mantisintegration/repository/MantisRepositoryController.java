
package eu.doppel_helix.netbeans.mantisintegration.repository;

import eu.doppel_helix.netbeans.mantisintegration.MantisConnector;
import eu.doppel_helix.netbeans.mantisintegration.data.Version;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;

public class MantisRepositoryController implements RepositoryController, DocumentListener, ChangeListener, ActionListener {
    private final static Logger logger = Logger.getLogger(MantisRepositoryController.class.getName());
    private final static String COMMAND_CHECKCONNECTION = "checkConnection";
    private final static Color errorColor = new Color( 205, 0, 0);
    private final static Color goodColor = new Color( 7, 155, 0);
    private final MantisRepository repository;
    private MantisRepositoryPanel panel;
    private final List<String> errorMessages = new ArrayList<>();
    private final ChangeSupport cs = new ChangeSupport(this);
    private boolean checking = false;
    
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
            panel.httpAuthEnabled.addActionListener(this);
            panel.httpUserField.getDocument().addDocumentListener(this);
            panel.httpPwdField.getDocument().addDocumentListener(this);
            cs.addChangeListener(this);
            panel.checkButton.addActionListener(this);
            panel.checkButton.setActionCommand(COMMAND_CHECKCONNECTION);
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
            URI.create(url).toURL();
        } catch (IllegalArgumentException | MalformedURLException ex) {
            errorMessages.add("Invalid URL provided");
        }
        
        if(username.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessages.add("Username or password empty");
        }
        
        return errorMessages.isEmpty() && (! checking);
    }

    @Override
    public void populate() {
        RepositoryInfo ri = repository.getInfo();
        if(panel != null && ri != null) {
            panel.nameTextField.setText(ri.getDisplayName());
            panel.urlTextField.setText(ri.getUrl());
            panel.usernameTextField.setText(ri.getUsername());
            panel.passwordTextField.setText(new String(ri.getPassword()));
            if(ri.getValue(MantisRepository.PROP_SCHEDULE_DATE_FIELD) != null) {
                panel.scheduleDateFieldCustom.setSelected(true);
                panel.scheduleDateFieldCustomName.setText(ri.getValue(MantisRepository.PROP_SCHEDULE_DATE_FIELD));
            } else {
                panel.scheduleDateFieldBuiltIn.setSelected(true);
                panel.scheduleDateFieldCustomName.setText("");
            }
            if(ri.getValue(MantisRepository.PROP_SCHEDULE_LENGTH_FIELD) != null) {
                panel.scheduleLengthFieldCustom.setSelected(true);
                panel.scheduleLengthFieldCustomName.setText(ri.getValue(MantisRepository.PROP_SCHEDULE_LENGTH_FIELD));
            } else {
                panel.scheduleLengthFieldBuiltIn.setSelected(true);
                panel.scheduleLengthFieldCustomName.setText("");
            }
            if(ri.getHttpUsername() != null && (! ri.getHttpUsername().isEmpty())
                    && ri.getHttpPassword() != null && ri.getHttpPassword().length > 0) {
                panel.httpAuthEnabled.setSelected(true);
            } else {
                panel.httpAuthEnabled.setSelected(false);
            }
            panel.httpPwdField.setEnabled((! checking) && panel.httpAuthEnabled.isSelected());
            panel.httpUserField.setEnabled((! checking) && panel.httpAuthEnabled.isSelected());
            panel.httpUserField.setText(ri.getHttpUsername());
            panel.httpPwdField.setText(new String(ri.getHttpPassword()));
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

    public boolean isChecking() {
        return checking;
    }

    public void setChecking(boolean checking) {
        this.checking = checking;
        cs.fireChange();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                panel.checkButton.setEnabled(! checking);
                panel.nameTextField.setEnabled(! checking);
                panel.urlTextField.setEnabled(! checking);
                panel.usernameTextField.setEnabled(! checking);
                panel.passwordTextField.setEnabled(! checking);
                panel.httpAuthEnabled.setEnabled(! checking);
                panel.httpPwdField.setEnabled((! checking) && panel.httpAuthEnabled.isSelected());
                panel.httpUserField.setEnabled((! checking) && panel.httpAuthEnabled.isSelected());
            }
        });
        if(isValid()) {
            panel.checkButton.setEnabled(true);
        } else {
            panel.checkButton.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (COMMAND_CHECKCONNECTION.equals(e.getActionCommand())) {
            setChecking(true);

            final String httpUsername;
            final String httpPassword;

            if (panel.httpAuthEnabled.isSelected()) {
                httpUsername = panel.httpUserField.getText();
                httpPassword = panel.httpPwdField.getText();
            } else {
                httpUsername = "";
                httpPassword = "";
            }
            
            new SwingWorker<Object, Object>() {
                private String result = "";
                private Color resultColor = null;

                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        Version v = MantisRepository.checkConnection(
                                panel.urlTextField.getText(),
                                panel.usernameTextField.getText(),
                                panel.passwordTextField.getText(),
                                httpUsername,
                                httpPassword
                        );
                        result = "Successfully connected (version: " + v.getVersionString() + ")";
                        resultColor = goodColor;
                    } catch (ServiceException ex) {
                        logger.log(Level.INFO, "", ex);
                        result = "Failed create client - check URL";
                        resultColor = errorColor;
                    } catch (RemoteException ex) {
                        logger.log(Level.INFO, "", ex);
                        result = "Failed request - check username/password";
                        resultColor = errorColor;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    panel.checkResult.setText(result);
                    panel.checkResult.setForeground(resultColor);
                    setChecking(false);
                }
            }.execute();
        } else if ("httpAuth".equals(e.getActionCommand())) {
            if(! panel.httpAuthEnabled.isSelected()) {
                panel.httpUserField.setText("");
                panel.httpPwdField.setText("");
            }
            cs.fireChange();
        }
    }

    @Override
    public void cancelChanges() {
        this.populate();
    }

    @Override
    public void applyChanges() {
        String name = panel.nameTextField.getText();
        String url = panel.urlTextField.getText();
        String username = panel.usernameTextField.getText();
        String password = panel.passwordTextField.getText();
        RepositoryInfo ri = repository.getInfo();
        String id = null;
        if( ri != null && ri.getID() != null) {
            id = ri.getID();
        }
          
        if(id == null) {
            id = MantisConnector.ID + System.currentTimeMillis();
        }
        
        String httpUsername = "";
        String httpPassword = "";

        if(panel.httpAuthEnabled.isSelected()) {
            httpUsername = panel.httpUserField.getText();
            httpPassword = panel.httpPwdField.getText();
        }
        
        ri = new RepositoryInfo(id, 
                MantisConnector.ID, 
                url, 
                name, 
                "", 
                username, 
                httpUsername,
                password.toCharArray(), 
                httpPassword.toCharArray());
        
        if (panel.scheduleDateFieldCustom.isSelected()) {
            ri.putValue(MantisRepository.PROP_SCHEDULE_DATE_FIELD, 
                    panel.scheduleDateFieldCustomName.getText());
        }
        if (panel.scheduleLengthFieldCustom.isSelected()) {
           ri.putValue(MantisRepository.PROP_SCHEDULE_LENGTH_FIELD, 
                    panel.scheduleLengthFieldCustomName.getText());
        }

        repository.setInfo(ri);
    }
    
}
