/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bag.client;

import java.util.List;

/**
 *
 * @author toaster
 */
public class BagList {

    private PaginationBean pagination;
    private List<BagBean> objects;

    public List<BagBean> getObjects() {
        return objects;
    }

    public PaginationBean getPagination() {
        return pagination;
    }

    public void setObjects(List<BagBean> objects) {
        this.objects = objects;
    }

    public void setPagination(PaginationBean pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pagination ");
        sb.append(pagination);
        sb.append("Objects: [");
        if (objects != null) {
            for (BagBean ob : objects) {
                sb.append(" ");
                sb.append(ob);

            }
        } else {
            sb.append("null");
        }
        sb.append("]");

        return sb.toString();
    }

    public static class PaginationBean {

        private int offset;
        private int limit;
        private int total_count;
        private int next;
        private int previous;

        @Override
        public String toString() {
            return "Offset: "  + offset + " limit: " + limit + " total: " + total_count
                    + " next: " + next + " previous: " + previous;
        }

        public int getLimit() {
            return limit;
        }

        public int getNext() {
            return next;
        }

        public int getOffset() {
            return offset;
        }

        public int getPrevious() {
            return previous;
        }

        public int getTotal_count() {
            return total_count;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public void setNext(int next) {
            this.next = next;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public void setPrevious(int previous) {
            this.previous = previous;
        }

        public void setTotal_count(int total_count) {
            this.total_count = total_count;
        }
    }
}
