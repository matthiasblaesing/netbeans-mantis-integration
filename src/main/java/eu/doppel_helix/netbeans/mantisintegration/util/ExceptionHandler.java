package eu.doppel_helix.netbeans.mantisintegration.util;

import biz.futureware.mantisconnect.CustomFieldDefinitionData;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.axis.AxisFault;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class ExceptionHandler {
    private static final QName clientFault = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client");
    private static final Pattern customFieldMsgPattern = Pattern.compile("(Invalid custom field value for field )id \\s*(\\d+)\\s*\\.");
    private final MantisRepository repository;
    
    public ExceptionHandler(MantisRepository repository) {
        this.repository = repository;
    }
    
    public void handleException(Logger logger, String message, Exception ex) {
        if (ex instanceof AxisFault) {
            AxisFault af = (AxisFault) ex;
            if (clientFault.equals(af.getFaultCode())) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        formatUserMessage(message, af.getFaultString()), 
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                logger.log(Level.INFO, message, ex);
            }
        } else {
            logger.log(Level.WARNING, message, ex);
        }
    }
    
    private String formatUserMessage(String message, String fault) {
        if(fault == null) {
            fault = "";
        }
        Matcher customFieldMatcher = customFieldMsgPattern.matcher(fault);
        if(customFieldMatcher.matches()) {
            String intro = customFieldMatcher.group(1);
            String id = customFieldMatcher.group(2);
            CustomFieldDefinitionData cfd = repository.getCustomFieldDefinition(
                    new BigInteger(id));
            if(cfd != null) {
                fault = String.format("%s '%s (ID: %s)'",
                        intro, cfd.getField().getName(), id);
            } else {
                fault = String.format("%s (ID: %s)", intro, id);
            }
        }
        return message + "\n\n" + fault;
    }
}
