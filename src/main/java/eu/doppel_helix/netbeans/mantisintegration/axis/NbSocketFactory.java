package eu.doppel_helix.netbeans.mantisintegration.axis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.axis.components.net.BooleanHolder;
import static org.apache.axis.components.net.DefaultSocketFactory.CONNECT_TIMEOUT;

public class NbSocketFactory extends NbBaseSocketFactory implements org.apache.axis.components.net.SocketFactory {

    private static final Logger LOG = Logger.getLogger(NbSocketFactory.class.getName());

    public NbSocketFactory(Hashtable attributes) {
        super(attributes);
    }

    @Override
    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
        int timeout = 0;

        if (attributes != null) {
            String value = (String) attributes.get(CONNECT_TIMEOUT);
            timeout = (value != null) ? Integer.parseInt(value) : 0;
        }

        if (port == -1) {
            port = 80;
        }

        // Construct pseudo URL to determine proxy
        URI pseudoUri = new URI("http", null, host, port, null, null, null);

        List<Proxy> proxies = ProxySelector.getDefault().select(pseudoUri);

        Socket s = null;

        // Iterate over possible proxies in the order they are returned by the
        // selector
        if (s == null) {
            for (Proxy p : proxies) {
                try {
                    if (null != p.type()) {
                        switch (p.type()) {
                            case DIRECT:
                                s = new Socket();
                                s.connect(new InetSocketAddress(host, port), timeout);
                                break;
                            case SOCKS:
                                s = new Socket(p);
                                s.connect(new InetSocketAddress(host, port), timeout);
                                break;
                            case HTTP:
                                s = new Socket();
                                s.connect(p.address());
                                useFullURL.value = true;
                                addProxyAuthenticationIfPresent(otherHeaders);
                                break;
                            default:
                        }
                    }
                    break;
                } catch (IOException | IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "Failed proxy connection", ex);
                }
            }
        }

        return s;
    }

    private void addProxyAuthenticationIfPresent(StringBuffer otherHeaders) {
        String authorizationHeader = getAuthorizationHeader();
        if (!authorizationHeader.isEmpty()) {
            otherHeaders
                .append(authorizationHeader)
                .append("\r\n");
        }
    }

}
