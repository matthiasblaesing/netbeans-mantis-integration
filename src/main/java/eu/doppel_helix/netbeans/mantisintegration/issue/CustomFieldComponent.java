package eu.doppel_helix.netbeans.mantisintegration.issue;

import biz.futureware.mantisconnect.CustomFieldDefinitionData;
import biz.futureware.mantisconnect.UserData;
import com.clutch.dates.StringToTime;
import eu.doppel_helix.netbeans.mantisintegration.swing.DelegatingBaseLineJPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.WrapLayout;

public abstract class CustomFieldComponent extends DelegatingBaseLineJPanel implements ActionListener {

    private enum Type {
        DisplayOnly, // Added value
        String,
        Numeric,
        Float,
        Enumeration,
        Email,
        Checkbox,
        List,
        MultiselectionList,
        Date,
        Radio
    }

    public static CustomFieldComponent create(CustomFieldDefinitionData cfdd, UserData ud) {
        // typeId is zero based - but more values are introduced (-1 for DisplayOnly)
        Type type = Type.values()[cfdd.getType().intValue() + 1];
        if (ud != null
                && ud.getAccess_level().compareTo(cfdd.getAccess_level_rw()) < 0) {
            return new CustomFieldComponentDisplay(cfdd, type);
        } else {
            switch (type) {
                case Date:
                    return new CustomFieldComponentDate(cfdd);
                case String:
                case Email:
                    return new CustomFieldComponentLine(cfdd);
                case List:
                    return new CustomFieldComponentList(cfdd, false);
                case MultiselectionList:
                    return new CustomFieldComponentList(cfdd, true);
                case Checkbox:
                    return new CustomFieldComponentCheckbox(cfdd);
                case Radio:
                    return new CustomFieldComponentRadio(cfdd);
                case Numeric:
                    return new CustomFieldComponentNumeric(cfdd, false);
                case Float:
                    return new CustomFieldComponentNumeric(cfdd, true);
                case Enumeration:
                    return new CustomFieldComponentEnum(cfdd);
                default:
                    return new CustomFieldComponentDisplay(cfdd, type);
            }
        }
    }

    private final CustomFieldDefinitionData cfdd;
    private JLabel label;
    protected JPopupMenu popup = new JPopupMenu();

    private CustomFieldComponent(CustomFieldDefinitionData cfdd) {
        this.cfdd = cfdd;
        setOpaque(false);
        
        JMenuItem mi = new JMenuItem("Reset");
        mi.addActionListener(this);
        mi.setActionCommand("reset");
        popup.add(mi);
        
        this.setComponentPopupMenu(popup);
    }

    abstract public String getValue();

    abstract public void setValue(String value);

    public CustomFieldDefinitionData getCustomFieldDefinitionData() {
        return cfdd;
    }
    
    public void setDefaultValue() {
        setValue(cfdd.getDefault_value());
    }

    public JLabel getLabel() {
        if (label == null) {
            label = new JLabel(cfdd.getField().getName() + ":");
        }
        return label;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "reset":
                this.setDefaultValue();
                break;
        }
    }

    private static class CustomFieldComponentDisplay extends CustomFieldComponent {

        private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        private String value;
        private JLabel outputLabel;
        private Type type;

        public CustomFieldComponentDisplay(CustomFieldDefinitionData cfdd, Type type) {
            super(cfdd);
            this.type = type;
            this.outputLabel = new JLabel();
            this.setLayout(new BorderLayout());
            this.add(outputLabel);
            this.setComponentPopupMenu(null);
        }

        @Override
        public void setValue(String value) {
            this.value = value;
            outputLabel.setText(this.toDisplay(value));
        }

        @Override
        public java.lang.String getValue() {
            return value;
        }
        
        private String toDisplay(String value) {
            if (value == null) {
                return " ";
            }
            switch (type) {
                case Checkbox:
                case MultiselectionList:
                    return value.replace("|", ", ");
                case Date:
                    synchronized (dateFormat) {
                        Date d = new Date(Integer.valueOf(value) * 1000l);
                        return dateFormat.format(d);
                    }
                case Email:
                case Enumeration:
                case Float:
                case Numeric:
                case Radio:
                case String:
                case List:
                default:
                    return value;
            }
        }
    }

    private static class CustomFieldComponentDate extends CustomFieldComponent {
        private final JXDatePicker datePicker = new JXDatePicker();

        public CustomFieldComponentDate(CustomFieldDefinitionData cfdd) {
            super(cfdd);
            this.setLayout(new WrapLayout(WrapLayout.LEADING));
            this.add(datePicker);
            datePicker.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            if (value != null) {
                value = value.trim();
            }
            if (value.trim().startsWith("{") && value.endsWith("}")) {
                Object date = StringToTime.date(value.substring(1, value.length()
                        - 1));
                if (date instanceof Date) {
                    datePicker.setDate((Date) date);
                } else {
                    datePicker.setDate(null);
                }
            } else {
                try {
                    Long unixtimestamp = Long.valueOf(value);
                    datePicker.setDate(new Date(unixtimestamp * 1000));
                } catch (NullPointerException | NumberFormatException ex) {
                    datePicker.setDate(null);
                }
            }
        }

        @Override
        public java.lang.String getValue() {
            if (datePicker.getDate() != null) {
                return Long.toString(datePicker.getDate().getTime() / 1000);
            } else {
                return "";
            }
        }
    }

    private static class CustomFieldComponentLine extends CustomFieldComponent {
        private final JTextField line = new JTextField();

        public CustomFieldComponentLine(CustomFieldDefinitionData cfdd) {
            super(cfdd);
            this.setLayout(new BorderLayout());
            this.add(line);
            line.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            if(value == null) {
                value = "";
            }
            line.setText(value);
        }

        @Override
        public java.lang.String getValue() {
            return line.getText();
        }
    }
    
    private static class CustomFieldComponentList extends CustomFieldComponent {
        private final JList<String> list = new JList<>();

        public CustomFieldComponentList(CustomFieldDefinitionData cfdd, boolean multiselect) {
            super(cfdd);
            this.setLayout(new BorderLayout());
            this.add(new JScrollPane(list));
            this.setMinimumSize(new Dimension(0, 75));
            DefaultListModel<String> dlm = new DefaultListModel<>();
            String[] possibleValues = cfdd.getPossible_values().split("\\|");
            for(int i = 0; i < possibleValues.length; i++) {
                dlm.add(i, possibleValues[i]);
            }
            list.setModel(dlm);
            if(multiselect) {
                list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            list.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            List<String> selectedValues = Arrays.asList(value.split("\\|"));
            List<Integer> selectIndices = new ArrayList<>();
            ListModel<String> lm = list.getModel();
            for(int i = 0; i < lm.getSize(); i++) {
                if(selectedValues.contains(lm.getElementAt(i))) {
                    selectIndices.add(i);
                }
            }
            int selectedIndicesArray[] = new int[selectIndices.size()];
            for(int i = 0; i < selectIndices.size(); i++) {
                selectedIndicesArray[i] = selectIndices.get(i);
            }
            list.setSelectedIndices(selectedIndicesArray);
        }

        @Override
        public java.lang.String getValue() {
            StringBuilder result = new StringBuilder();
            for(int index: list.getSelectedIndices()) {
                if(result.length() != 0) {
                    result.append("|");
                }
                result.append(list.getModel().getElementAt(index));
            }
            return result.toString();
        }
    }
    
    private static class CustomFieldComponentCheckbox extends CustomFieldComponent {
        private final Map<String,JCheckBox> checkboxes = new HashMap<>();

        public CustomFieldComponentCheckbox(CustomFieldDefinitionData cfdd) {
            super(cfdd);
            this.setLayout(new WrapLayout(WrapLayout.LEADING));
            for(String value: cfdd.getPossible_values().split("\\|")) {
                JCheckBox checkbox = new JCheckBox(value);
                checkbox.setOpaque(false);
                checkbox.setComponentPopupMenu(popup);
                checkboxes.put(value, checkbox);
                this.add(checkbox);
            }
        }

        @Override
        public void setValue(String value) {
            List<String> selectedValues = Arrays.asList(value.split("\\|"));
            for(Entry<String,JCheckBox> entry: checkboxes.entrySet()) {
                entry.getValue().setSelected(selectedValues.contains(entry.getKey()));
            }
        }

        @Override
        public java.lang.String getValue() {
            StringBuilder result = new StringBuilder();
            for(Entry<String,JCheckBox> entry: checkboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    if (result.length() != 0) {
                        result.append("|");
                    }
                    result.append(entry.getKey());
                }
            }
            return result.toString();
        }
    }
    
    private static class CustomFieldComponentRadio extends CustomFieldComponent {
        private final Map<String,JRadioButton> radiobuttons = new HashMap<>();

        public CustomFieldComponentRadio(CustomFieldDefinitionData cfdd) {
            super(cfdd);
            this.setLayout(new WrapLayout(WrapLayout.LEADING));
            ButtonGroup buttonGroup = new ButtonGroup();
            for(String value: cfdd.getPossible_values().split("\\|")) {
                JRadioButton radiobutton = new JRadioButton(value);
                radiobutton.setOpaque(false);
                radiobutton.setComponentPopupMenu(popup);
                radiobuttons.put(value, radiobutton);
                this.add(radiobutton);
                buttonGroup.add(radiobutton);
            }
        }

        @Override
        public void setValue(String value) {
            List<String> selectedValues = Arrays.asList(value.split("\\|"));
            for(Entry<String,JRadioButton> entry: radiobuttons.entrySet()) {
                entry.getValue().setSelected(selectedValues.contains(entry.getKey()));
            }
        }

        @Override
        public java.lang.String getValue() {
            StringBuilder result = new StringBuilder();
            for(Entry<String,JRadioButton> entry: radiobuttons.entrySet()) {
                if (entry.getValue().isSelected()) {
                    if (result.length() != 0) {
                        result.append("|");
                    }
                    result.append(entry.getKey());
                }
            }
            return result.toString();
        }
    }
    
    private static class CustomFieldComponentEnum extends CustomFieldComponent {
        private final JComboBox<String> combobox = new JComboBox<>();

        public CustomFieldComponentEnum(CustomFieldDefinitionData cfdd) {
            super(cfdd);
            this.setLayout(new WrapLayout(WrapLayout.LEADING));
            DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<>();
            for(String value: cfdd.getPossible_values().split("\\|")) {
                dcbm.addElement(value);
            }
            combobox.setModel(dcbm);
            this.add(combobox);
            combobox.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            combobox.setSelectedItem(value);
        }

        @Override
        public java.lang.String getValue() {
            if(combobox.getSelectedIndex() != -1) {
                return (String) combobox.getSelectedItem();
            } else {
                return "";
            }
        }
    }

    private static class CustomFieldComponentNumeric extends CustomFieldComponent implements FocusListener {
        private final JTextField line = new JTextField();
        private final boolean floating;
        private String oldValue = null;

        public CustomFieldComponentNumeric(CustomFieldDefinitionData cfdd, boolean floating) {
            super(cfdd);
            this.setLayout(new BorderLayout());
            this.add(line);
            this.floating = floating;
            line.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            if(value == null) {
                value = "";
            }
            line.setText(value);
        }

        @Override
        public java.lang.String getValue() {
            return line.getText();
        }

        @Override
        public void focusGained(FocusEvent e) {
            oldValue = line.getText();
        }

        @Override
        public void focusLost(FocusEvent e) {
            String newValue = line.getText();
            if("".equals(newValue)) {
                return;
            }
            try {
                if (floating) {
                    Double.parseDouble(newValue);
                } else {
                    Integer.parseInt(newValue);
                }
            } catch (NumberFormatException ne) {
                line.setText(oldValue);
            }
        }
    }
}
