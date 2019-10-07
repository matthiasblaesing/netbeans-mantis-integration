/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.doppel_helix.netbeans.mantisintegration.axis;

import org.apache.axis.utils.StringUtils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import org.apache.axis.components.net.SecureSocketFactory;

/**
 * This is inspired by the JSSESocketFactory from the axis package
 */
public class NbSecureSocketFactory extends NbBaseSocketFactory implements SecureSocketFactory {

    private static final Logger LOG = Logger.getLogger(NbSocketFactory.class.getName());

    /**
     * Field sslFactory
     */
    protected SSLSocketFactory sslFactory = null;

    public NbSecureSocketFactory(Hashtable attributes) {
        super(attributes);
    }

    /**
     * Initialize the SSLSocketFactory
     *
     * @throws IOException
     */
    protected void initFactory() throws IOException {
        sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
        int timeout = 0;

        if (attributes != null) {
            String value = (String) attributes.get(CONNECT_TIMEOUT);
            timeout = (value != null) ? Integer.parseInt(value) : 0;
        }

        if (sslFactory == null) {
            initFactory();
        }

        if (port == -1) {
            port = 443;
        }

        // Construct pseudo URL to determine proxy
        URI pseudoUri = new URI("https", null, host, port, null, null, null);

        List<Proxy> proxies = ProxySelector.getDefault().select(pseudoUri);

        Socket s = null;

        // If a direct connetion is possible - use it
        if (s == null) {
            for (Proxy p : proxies) {
                try {
                    Socket baseSocket = null;
                    if (null != p.type()) {
                        switch (p.type()) {
                            case DIRECT:
                                baseSocket = new Socket();
                                baseSocket.connect(new InetSocketAddress(host, port), timeout);
                                break;
                            case SOCKS:
                                baseSocket = new Socket(p);
                                baseSocket.connect(new InetSocketAddress(host, port), timeout);
                                break;
                            case HTTP:
                                baseSocket = new Socket();
                                baseSocket.connect(p.address());
                                // The tunnel handshake method (condensed and made reflexive)
                                OutputStream tunnelOutputStream = baseSocket.getOutputStream();
                                PrintWriter out = new PrintWriter(
                                    new BufferedWriter(new OutputStreamWriter(tunnelOutputStream)));
                                out.print("CONNECT " + host + ":" + port
                                    + " HTTP/1.0\r\n"
                                    + "User-Agent: AxisClient");
                                String authorization = getAuthorizationHeader();
                                if (!authorization.isEmpty()) {
                                    out.write("\n");
                                    out.write(authorization);
                                }
                                out.print("\nContent-Length: 0");
                                out.print("\nPragma: no-cache");
                                out.print("\r\n\r\n");
                                out.flush();
                                InputStream tunnelInputStream = baseSocket.getInputStream();
                                String replyStr = "";
                                // Make sure to read all the response from the proxy to prevent SSL negotiation failure
                                // Response message terminated by two sequential newlines
                                int newlinesSeen = 0;
                                boolean headerDone = false;
                                /*
                                 * Done on first newline
                                 */
                                while (newlinesSeen < 2) {
                                    int i = tunnelInputStream.read();

                                    if (i < 0) {
                                        throw new IOException("Unexpected EOF from proxy");
                                    }
                                    if (i == '\n') {
                                        headerDone = true;
                                        ++newlinesSeen;
                                    } else if (i != '\r') {
                                        newlinesSeen = 0;
                                        if (!headerDone) {
                                            replyStr += String.valueOf((char) i);
                                        }
                                    }
                                }
                                if (!(StringUtils.startsWithIgnoreWhitespaces("HTTP/1.0 200", replyStr)
                                    || StringUtils.startsWithIgnoreWhitespaces("HTTP/1.1 200", replyStr))) {
                                    throw new IOException();
                                }
                                break;
                            default:
                        }
                    }
                    if (baseSocket != null) {
                        // End of condensed reflective tunnel handshake method
                        s = sslFactory.createSocket(baseSocket, host, port, true);
                        ((SSLSocket) s).startHandshake();
                        break;
                    }
                } catch (IOException | IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "Failed proxy connection", ex);
                }
            }
        }

        return s;
    }
}
