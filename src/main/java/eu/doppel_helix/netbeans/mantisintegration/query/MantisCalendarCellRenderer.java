
package eu.doppel_helix.netbeans.mantisintegration.query;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MantisCalendarCellRenderer extends DefaultTableCellRenderer {
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(value instanceof Calendar) {
            value = df.format(((Calendar)value).getTime());
        } else if (value instanceof Date) {
            value = df.format(((Date)value));
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
