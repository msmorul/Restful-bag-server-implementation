/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.chronopolis.bag.client.BagBean;

/**
 *
 * @author toaster
 */
public class BagBeanRenderer extends ListViewItemRenderer {

    @Override
    public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
        if (item instanceof BagBean) {
            super.render(((BagBean) item).getId(), index, listView, selected, checked, highlighted, disabled);
        } else {
            super.render(item, index, listView, selected, checked, highlighted, disabled);
        }
    }
}
