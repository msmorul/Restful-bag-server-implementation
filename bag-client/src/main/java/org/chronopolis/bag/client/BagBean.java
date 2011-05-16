package org.chronopolis.bag.client;

public class BagBean {

    private String href;
    private String id;

    @Override
    public String toString() {
        return "Href: " + href + " Id: " + id;
    }

    public String getHref() {
        return href;
    }

    public String getId() {
        return id;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setId(String id) {
        this.id = id;
    }
}
