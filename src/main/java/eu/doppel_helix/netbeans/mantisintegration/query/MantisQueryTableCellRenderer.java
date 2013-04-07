
package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;

public class MantisQueryTableCellRenderer extends DefaultTableCellRenderer {
    private TableCellRenderer superRenderer;
    private Map<BigInteger,Color> colorMap = Mantis.getInstance().getStatusColorMap();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public MantisQueryTableCellRenderer(TableCellRenderer superRenderer) {
        this.superRenderer = superRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object originalValue = value;
        Color overrideColor = Color.WHITE;
        if(value instanceof IssueNode.IssueProperty) {
            try {
		if (((IssueNode.IssueProperty) originalValue).getName().startsWith("mantis.issue.")) {
		    value = ((IssueNode.IssueProperty) value).getValue();
                    if ("mantis.issue.status".equals(((IssueNode.IssueProperty) originalValue).getName())) {
                        if (value instanceof ObjectRef) {
                            ObjectRef or = (ObjectRef) value;
                            BigInteger level = or.getId();
                            if(colorMap.get(level) != null) {
                                overrideColor = colorMap.get(level);
                            }
                        }
                    }
                }
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(value instanceof BigInteger) {
            value = ((BigInteger) value).longValue();
        } else if (value instanceof ObjectRef) {
            value = ((ObjectRef) value).getName();
        } else if (value instanceof Date) {
            value = df.format((Date) value);
        } else if (value instanceof Calendar) {
            value = df.format(((Calendar) value).getTime());
        }
        
        Component c = null;
	if(value instanceof IssueNode.IssueProperty) {
		c = superRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	} else {
		c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
        
        if(overrideColor != null) {
        if(c instanceof JComponent) {
            ((JComponent)c).setOpaque(true);
        }
        c.setBackground(overrideColor);
        }
        
        return c;
    }
}
