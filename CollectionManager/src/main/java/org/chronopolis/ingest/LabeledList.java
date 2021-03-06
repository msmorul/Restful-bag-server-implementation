/*
 * Copyright (c) 2007-2011, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
package org.chronopolis.ingest;

import org.apache.pivot.collections.ArrayList;

/**
 * host list that only concerns itself with a subset of partitions
 * @author toaster
 */
public class LabeledList<T, V> extends ArrayList<T> implements Comparable {

    private static LabelComparator lc = new LabelComparator();
    private V label;
    private boolean loaded = false;

    public LabeledList(V label) {
        this.label = label;
        setComparator(new LabeledList.Comparator());
    }

    public LabeledList(V label, java.util.List<T> list) {
        for (T item : list) {
            add(item);
        }
        this.label = label;
        setComparator(new LabeledList.Comparator());
    }

    public static java.util.Comparator getLabelComparator() {
        return lc;
    }

    public static class LabelComparator implements java.util.Comparator {

        public int compare(Object o1, Object o2) {
            Object newO1 = o1;
            Object newO2 = o2;
            if (o1 instanceof LabeledList) {
                newO1 = ((LabeledList) o1).label;
            }
            if (o2 instanceof LabeledList) {
                newO2 = ((LabeledList) o2).label;
            }
            return LabeledList.compare(newO1, newO2);
        }
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
        return label + " [" + getLength() + "]";
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
