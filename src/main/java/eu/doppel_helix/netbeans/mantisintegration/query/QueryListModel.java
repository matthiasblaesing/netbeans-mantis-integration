
package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.openide.util.WeakListeners;

public class QueryListModel extends AbstractTableModel implements PropertyChangeListener {
    
    private List<MantisIssue> issues = new ArrayList<>();
    private final Map<BigInteger,PropertyChangeListener> listener = new HashMap<>();

    public void setIssues(Collection<MantisIssue> issues) {
        assert SwingUtilities.isEventDispatchThread();
        for(MantisIssue mi: this.issues) {
            PropertyChangeListener pcl = listener.get(mi.getId());
            if(pcl != null) {
                mi.removePropertyChangeListener(pcl);
            }
        }
        this.issues = new ArrayList<>(issues);
        for (MantisIssue mi : this.issues) {
            PropertyChangeListener pcl = WeakListeners.propertyChange(this, mi);
            mi.addPropertyChangeListener(pcl);
        }
        fireTableDataChanged();
    }
    
    public MantisIssue getIssue(int pos) {
        return issues.get(pos);
    }
    
    @Override
    public int getRowCount() {
        return issues.size();
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return BigInteger.class;
            case 1:
                return Integer.class;
            case 2: 
                return String.class;
            case 3:
                return ObjectRef.class;
            case 4:
                return ObjectRef.class;
            case 5:
                return ObjectRef.class;
            case 6:
                return Calendar.class;
            case 7:
                return String.class;
            case 8:
                return MantisIssue.class;
            default:
                return null;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MantisIssue mi = getIssue(rowIndex);
        switch(columnIndex) {
            case 0:
                return mi.getId();
            case 1:
                return mi.getNoteCount();
            case 2: 
                return mi.getCategory();
            case 3:
                return mi.getSeverity();
            case 4:
                return mi.getPriority();
            case 5:
                return mi.getStatus();
            case 6:
                return mi.getLast_updated();
            case 7:
                return mi.getSummary();
            case 8:
                return mi;
            default:
                return null;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.fireTableDataChanged();
    }
    
}
