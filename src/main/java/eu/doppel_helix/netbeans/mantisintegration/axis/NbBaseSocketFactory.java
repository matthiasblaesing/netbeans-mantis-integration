package eu.doppel_helix.netbeans.mantisintegration.axis;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import org.apache.axis.encoding.Base64;
import org.apache.axis.transport.http.HTTPConstants;
import org.openide.util.NbPreferences;

abstract class NbBaseSocketFactory implements org.apache.axis.components.net.SocketFactory {
    protected Hashtable attributes;

    public NbBaseSocketFactory(Hashtable attributes) {
        this.attributes = attributes;
    }

    protected String getAuthorizationHeader() {
        StringBuilder buffer = new StringBuilder();
        // Accessing the preferences directly is ugly as hell, but can't be
        // worked around, as the Authenticator returns nut values if authentication
        // is disabled!
        Preferences p = NbPreferences.root().node("/org/netbeans/core");
        if (p.getBoolean("useProxyAuthentication", false)) {
            PasswordAuthentication pa = Authenticator.requestPasswordAuthentication(
                null, null, -1, "HTTP", "", "http", null, Authenticator.RequestorType.PROXY);
            if (pa != null) {
                buffer
                    .append(HTTPConstants.HEADER_PROXY_AUTHORIZATION)
                    .append(": Basic ")
                    .append(Base64.encode(String.format("%s:%s",
                        pa.getUserName(),
                        new String(pa.getPassword())
                    ).getBytes()));
            }
        }
        return buffer.toString();
    }

}
