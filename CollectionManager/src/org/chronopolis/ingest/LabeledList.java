/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import org.apache.pivot.collections.ArrayList;

/**
 * host list that only concerns itself with a subset of partitions
 * @author toaster
 */
public class LabeledList<T, V> extends ArrayList<T> implements Comparable {

    private V label;
    private boolean loaded = false;

    public LabeledList(V label) {
        this.label = label;
        setComparator(new LabeledList.Comparator());
    }

    public LabeledList(V label, java.util.List<T> list) {
        for (T item : list)
        {
            add(item);
        }
        this.label = label;
        setComparator(new LabeledList.Comparator());
    }

    public static class Comparator implements java.util.Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return LabeledList.compare(o1, o2);
        }
    }

    private static int compare(Object o1, Object o2) {

        if (o1 instanceof Comparable) {
            Comparable c = (Comparable) o1;
            return c.compareTo(o2);

        } else if (o2 instanceof Comparable) {
            Comparable c = (Comparable) o2;
            return -1 * c.compareTo(o1);
        } else {
            return o1.toString().compareTo(o2.toString());
        }

    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof LabeledList) {
            return compare(label, ((LabeledList) o).label);
        } else {
            return -1;
        }
    }

    public V getLabel() {
        return label;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return label + " [" +getLength() + "]";
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof LabeledList && ((LabeledList) o).label.equals(label) && super.equals(o));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.label != null ? this.label.hashCode() : 0) + super.hashCode();
        return hash;
    }
}
