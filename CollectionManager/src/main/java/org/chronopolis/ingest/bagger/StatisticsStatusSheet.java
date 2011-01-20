/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.bagger;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;

/**
 *
 * @author toaster
 */
public class StatisticsStatusSheet extends Sheet
{

    private PushButton pb;
    public StatisticsStatusSheet() {
        setPreferredHeight(175);
                setPreferredWidth(325);
                ActivityIndicator idc = new ActivityIndicator();
                idc.setPreferredWidth(96);
                idc.setPreferredHeight(96);
                idc.setActive(true);
                BoxPane bp = new BoxPane();
                bp.getStyles().put("horizontalAlignment", "center");
                bp.add(idc);
                bp.setOrientation(Orientation.VERTICAL);
                bp.add(new Label("Scanning Bag..."));
                pb = new PushButton("Skip");
                
                bp.add(pb);
                setContent(bp);
                
    }

    public ListenerList<ButtonPressListener> getCloseButtonPressListeners()
    {
        return pb.getButtonPressListeners();
    }

}
