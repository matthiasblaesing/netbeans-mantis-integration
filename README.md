This netbeans plugin integrates the mantis bugtracker into the netbeans IDE.

After installation a new issue tracker is available inside netbeans.

#User information
##Relevant config options in mantisBT (server side):

- source_control_notes_view_status (default private, integer value 10 -> public)
- source_control_set_resolution_to (integer value 80->resolved, 90->closed)
- source_control_set_status_to (20->fixed)

##SSL-Handling

If your installation uses a regular SSL certificate, which is signed by a 
certificate authority (CA), that is part of the normal java certificate storage,
that is directly supported. Just enter the URL and with a https prefix.

If you use a self-signed certificate, there are two options:

1. Add your certificate to the java truststore:

        keytool -import -file <servercertfile> -alias <servername> -keystore "%JAVA_HOME%\lib\security\cacerts"

2. Make sure you use a recent version of MantisIntegration (0.5 and newer) and
install the "SSL Certificate Exception" module, that is part of nb-ldap-explorer.
The module can be found at: [http://code.google.com/p/nb-ldap-explorer/downloads/list](http://code.google.com/p/nb-ldap-explorer/downloads/list)
and is installable stand-alone. Since version 0.6 the "SSL Certificate Exception"
module is distributed together with the 7.4 modules inside the zip downloadable
from the netbeans plugin center.

#Developer information
##BUILD Instructions:

1. Checkout the source code
2. build with maven (mvn package)