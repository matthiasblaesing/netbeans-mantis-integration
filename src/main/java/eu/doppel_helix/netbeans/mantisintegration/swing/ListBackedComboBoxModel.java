
package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListBackedComboBoxModel<E> implements ComboBoxModel<E> {
    private final List<E> backingList = new ArrayList<>();
    private final Class<E> klass;
    Set<ListDataListener> listener = new HashSet<>();
    private E selectedItem = null;
    
    public ListBackedComboBoxModel(Class<E> klass) {
        this.klass = klass;
    }
    
    /**
     * Set the backing List for this ComboBoxModel (the list is copied)
     * 
     * @param elements 
     */
    public void setBackingList(final List<E> elements) {
        int oldLength = backingList.size();
        backingList.clear();
        if (oldLength > 0) {
            fireIntervalRemoved(0, oldLength - 1);
        }
        backingList.addAll(elements);
        if (selectedItem != null) {
            if (!backingList.contains(selectedItem)) {
                backingList.add(selectedItem);
            }
        }
        if (backingList.size() > 0) {
            fireIntervalAdded(0, backingList.size() - 1);
        }
    }
    
    public void addElement(final E element) {
        backingList.add(element);
        fireIntervalAdded(backingList.size() - 1, backingList.size() - 1);
    }
    
    public void addElement(final int position, final E element) {
        backingList.add(position, element);
        fireIntervalAdded(position, position);
    }
    
    /**
     * Retrieve an unmodifiable reference to the backing list
     * 
     * WARNING: This reference is only unmodifiable for the caller!
     * 
     * @return 
     */
    public List<E> getBackingList() {
        return Collections.unmodifiableList(backingList);
    }
    
    protected void fireIntervalAdded(int start, int end) {
        Iterator<ListDataListener> i = listener.iterator();
        ListDataEvent lde = new ListDataEvent(
                this, ListDataEvent.INTERVAL_ADDED, start, end);
        while(i.hasNext()) {
            i.next().intervalAdded(lde);
        }
    }

    protected void fireIntervalRemoved(int start, int end) {
        Iterator<ListDataListener> i = listener.iterator();
        ListDataEvent lde = new ListDataEvent(
                this, ListDataEvent.INTERVAL_REMOVED, start, end);
        while(i.hasNext()) {
            i.next().intervalAdded(lde);
        }
    }
    
    protected void fireContentsChanged(int start, int end) {
        Iterator<ListDataListener> i = listener.iterator();
        ListDataEvent lde = new ListDataEvent(
                this, ListDataEvent.CONTENTS_CHANGED, start, end);
        while (i.hasNext()) {
            i.next().contentsChanged(lde);
        }
    }
    
    @Override
    public void setSelectedItem(Object anItem) {
        if(anItem != null && (! (klass.isAssignableFrom(anItem.getClass())))) {
            throw new IllegalArgumentException("Wrong type for List");
        } else {
            selectedItem = (E) anItem;
            if(selectedItem != null) {
                if(! backingList.contains(selectedItem)) {
                    backingList.add(selectedItem);
                    fireIntervalAdded(backingList.size() - 1, backingList.size() - 1);
                }
            }
            if(! backingList.isEmpty()) {
                fireContentsChanged(0, backingList.size() - 1);
            }
        }
    }

    @Override
    public E getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return backingList.size();
    }

    @Override
    public E getElementAt(int index) {
        return backingList.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listener.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listener.remove(l);
    }
    
}
