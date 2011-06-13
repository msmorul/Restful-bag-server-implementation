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
