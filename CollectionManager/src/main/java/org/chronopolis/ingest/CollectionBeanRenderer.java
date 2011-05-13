/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.content.ListViewItemRenderer;

/**
 *
 * @author toaster
 */
public class CollectionBeanRenderer extends ListViewItemRenderer {

    @Override
    public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
//        if (item instanceof CollectionBean) {
//            super.render(((CollectionBean) item).getName(), index, listView, selected, checked, highlighted, disabled);
//        } else {
            super.render(item, index, listView, selected, checked, highlighted, disabled);
//        }
    }
}
