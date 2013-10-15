
package eu.doppel_helix.netbeans.mantisintegration.query;

import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssueNode;
import java.awt.Color;
import java.awt.Component;
import java.math.BigInteger;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class MantisQueryTableCellRenderer extends DefaultTableCellRenderer {
    private final TableCellRenderer superRenderer;
    private final Map<BigInteger,Color> colorMap = Mantis.getInstance().getStatusColorMap();
    

    public MantisQueryTableCellRenderer(TableCellRenderer superRenderer) {
        this.superRenderer = superRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel renderer = (JLabel) superRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(value instanceof MantisIssueNode.StatusProperty) {
            MantisIssueNode.StatusProperty sp = (MantisIssueNode.StatusProperty) value;
            Color color = colorMap.get(sp.getValue().getId());
            if(color != null) {
                renderer.setBackground(color);
            }
        }
        return renderer;
    }
}
