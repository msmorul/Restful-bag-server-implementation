/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.manager;

import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.chronopolis.bagserver.BagEntry;

/**
 *
 * @author toaster
 */
public class BagListRenderer extends ListViewItemRenderer {

    @Override
    public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
        if (item instanceof BagEntry) {
            super.render(((BagEntry) item).getIdentifier(), index, listView, selected, checked, highlighted, disabled);
            if (((BagEntry) item).getBagState() == BagEntry.State.OPEN) {
                label.getStyles().put("color", "green");
            }
        } else {
            super.render(item, index, listView, selected, checked, highlighted, disabled);
        }
    }
}
