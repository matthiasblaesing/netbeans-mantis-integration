
package eu.doppel_helix.netbeans.mantisintegration;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssueProvider;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisScheduleProvider;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisStatusProvider;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQueryProvider;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepository;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepositoryProvider;
import java.awt.Color;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;

public class Mantis {
    private static Mantis instance;
    private HashMap<BigInteger, Color> statusColorMap;
    
    private MantisRepositoryProvider mrp;
    private MantisQueryProvider mqp;
    private MantisIssueProvider mip; 
    private MantisScheduleProvider msp;
    private MantisStatusProvider statusProvider;
    private BugtrackingSupport<MantisRepository, MantisQuery, MantisIssue> bf;
    
    private Mantis() {}
    
    public static synchronized Mantis getInstance() {
        if(instance == null) {
            instance = new Mantis();
        }
        return instance;
    }
    
    public MantisIssueProvider getIssueProvider() {
        if(mip == null) {
            mip = new MantisIssueProvider();
        }
        return mip;
    }
    
    public MantisQueryProvider getQueryProvider() {
        if(mqp == null) {
            mqp = new MantisQueryProvider();
        }
        return mqp;
    }
    
    public MantisRepositoryProvider getRepositoryProvider() {
        if(mrp == null) {
            mrp = new MantisRepositoryProvider();
        }
        return mrp;
    }
    
    public MantisScheduleProvider getScheduleProvider() {
        if(msp == null) {
            msp = new MantisScheduleProvider();
        }
        return msp;
    }

    public MantisStatusProvider getStatusProvider() {
        if(statusProvider == null) {
            statusProvider = new MantisStatusProvider();
        }
        return statusProvider;
    }
    
    public BugtrackingSupport<MantisRepository, MantisQuery, MantisIssue> getBugtrackingSupport() {
        if (bf == null) {
            bf = new BugtrackingSupport<>(
                    getRepositoryProvider(),
                    getQueryProvider(),
                    getIssueProvider()
            );
        }
        return bf;
    }
    
    public Map<BigInteger, Color> getStatusColorMap() {
        if(statusColorMap == null) {
            // Taken from default config
            statusColorMap = new HashMap<>();
            statusColorMap.put(new BigInteger("10"), Color.decode("#FFA0A0")); // new -> red
            statusColorMap.put(new BigInteger("20"), Color.decode("#FF50A8")); // feedback -> purple
            statusColorMap.put(new BigInteger("30"), Color.decode("#FFD850")); // acknowledged - > orange
            statusColorMap.put(new BigInteger("40"), Color.decode("#FFFFB0")); // confirmed -> yellow
            statusColorMap.put(new BigInteger("50"), Color.decode("#C8C8FF")); // assigned -> blue
            statusColorMap.put(new BigInteger("80"), Color.decode("#CCEEDD")); // resolved -> bluish-green
            statusColorMap.put(new BigInteger("90"), Color.decode("#E8E8E8")); // closed -> light gray
            
        }
        return statusColorMap;
    }
}
