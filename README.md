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
2. Determine the Implementation version of the issuetracker module:   
    1. Open /ide/modules/org-netbeans-modules-bugtracking.jar
    2. Inside the jar open META-INF/MANIFEST.MF
    3. Find the line "OpenIDE-Module-Implementation-Version" - the value after that key is the value you search for (in 7.4 this is: 201310111528)
3. Run:
    mvn -Dmantisintegration.nbtarget=<RELEASE> -Dmantisintegration.bugtracking.implver=<BUGTRACKINGIMPL> package
    
    You have to replace <RELEASE> with RELEASE74 (Netbeans 7.4)
    You have to replace <BUGTRACKINGIMPL> with the value determined in step 2
4. You find the installable module in: target/

Known Kombinations:

* [unsupported] 7.2: mvn -Dmantisintegration.nbtarget=RELEASE72 -Dmantisintegration.bugtracking.implver=201301311716 package
* [unsupported] 7.3: mvn -Dmantisintegration.nbtarget=RELEASE73 -Dmantisintegration.bugtracking.implver=201302132200 package
* [unsupported] 7.3.1: mvn -Dmantisintegration.nbtarget=RELEASE73 -Dmantisintegration.bugtracking.implver=201306052037 package
* [unsupported] 7.3.1: mvn -Dmantisintegration.nbtarget=RELEASE73 -Dmantisintegration.bugtracking.implver=201306052037 package
* 7.4: mvn -Dmantisintegration.nbtarget=RELEASE74 -Dmantisintegration.bugtracking.implver=201310111528 package