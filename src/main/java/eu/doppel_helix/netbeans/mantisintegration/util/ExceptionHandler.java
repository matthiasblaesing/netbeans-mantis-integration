package eu.doppel_helix.netbeans.mantisintegration.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.apache.axis.AxisFault;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class ExceptionHandler {
    private static final QName clientFault = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client");
    
    public static void handleException(Logger logger, String message, Exception ex) {
        if (ex instanceof AxisFault) {
            AxisFault af = (AxisFault) ex;
            if (clientFault.equals(af.getFaultCode())) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        message + "\n\n" + af.getFaultString(), 
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                logger.log(Level.INFO, message, ex);
            }
        } else {
            logger.log(Level.WARNING, message, ex);
        }
    }
}
