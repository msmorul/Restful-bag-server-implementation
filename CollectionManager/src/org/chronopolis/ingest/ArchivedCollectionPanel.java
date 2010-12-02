/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import edu.umiacs.ace.json.AceItem;
import edu.umiacs.ace.json.JsonGateway;
import edu.umiacs.ace.json.ParentChildBean;
import edu.umiacs.ace.json.PartnerSite;
import edu.umiacs.ace.json.StatusBean.CollectionBean;
import edu.umiacs.ace.json.SummaryBean;
import edu.umiacs.ace.json.SummaryBean.Summary;
import java.io.IOException;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ApplicationContextMessageListener;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.NodeCheckState;
import org.apache.pivot.wtk.TreeViewBranchListener;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.apache.pivot.wtk.content.TreeViewNodeRenderer;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 *
 * @author toaster
 */
public class ArchivedCollectionPanel extends Border {

    @WTKX
    private TreeView fileTreeView;
    @WTKX
    private Border fileDetailsPane;
    @WTKX
    private ListView reportListView;
    @WTKX
    private Border reportDetailsPane;
    private CollectionBean currentCollection = null;

    public ArchivedCollectionPanel() {

        try {
            WTKXSerializer serializer = new WTKXSerializer();
            TablePane mainW = (TablePane) serializer.readObject(this, "archivedCollectionPanel.wtkx");
            serializer.bind(this);
            setContent(mainW);

            ApplicationContext.subscribe(CollectionBean.class, new ApplicationContextMessageListener<CollectionBean>() {

                public void messageSent(CollectionBean t) {
                    currentCollection = t;
                    loadFileTree();
                    loadReports();
                    ArchivedCollectionPanel.this.load(t);
                }
            });

            fileTreeView.getTreeViewBranchListeners().add(new LazyLoadBranchListener());
            fileTreeView.setNodeRenderer(new FileTreeRenderer());
            fileTreeView.getTreeViewSelectionListeners().add(new DetailsLoader());

            reportListView.setItemRenderer(new ReportListRenderer());
            reportListView.getListViewSelectionListeners().add(new ReportListListener());

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadReports() {

        if (currentCollection != null) {
            ApplicationContext.queueCallback(new Runnable() {

                public void run() {

                    PartnerSite site = Main.getAceSite();
                    JsonGateway gateway = JsonGateway.getGateway();
                    SummaryBean reportbean = gateway.getSummaryBean(site);
//                    System.out.println("rb " + reportbean);
                    List ll = new ArrayList();
                    reportListView.setListData(ll);
                    for (Summary s : reportbean.getSummaries()) {
                        if (s.getCollection() == currentCollection.getId()) {
                            ll.add(s);
                        }
                    }
                }
            });
        } else {
        }
    }

    private class ReportListListener extends ListViewSelectionListener.Adapter {

        @Override
        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
            reportDetailsPane.load(listView.getSelectedItem());
        }
    }

    private class ReportListRenderer extends ListViewItemRenderer {

        @Override
        public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
            if (item instanceof Summary) {
                super.render(((Summary) item).getReportName(), index, listView, selected, checked, highlighted, disabled);

            } else {
                super.render(item, index, listView, selected, checked, highlighted, disabled);
            }
        }
    }

    private void loadFileTree() {
        if (currentCollection != null) {
            ApplicationContext.queueCallback(new Runnable() {

                public void run() {
                    PartnerSite site = Main.getAceSite();
                    JsonGateway gateway = JsonGateway.getGateway();
                    ParentChildBean root = gateway.getAceItem(site, currentCollection.getId(), null);
                    LabeledList ll = new LabeledList(root.getParent());
                    fileTreeView.setTreeData(ll);

                    for (AceItem child : root.getChildren()) {
                        if (child.isDirectory()) {
                            ll.add(new LabeledList(child));
                        } else {
                            ll.add(child);
                        }
                    }
                }
            });
        } else {
            fileTreeView.setTreeData(new ArrayList());
        }
    }

    private class DetailsLoader extends TreeViewSelectionListener.Adapter {

        @Override
        public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths) {

            Object o = treeView.getSelectedNode();
//            System.out.println("item " + o);
            if (o instanceof AceItem) {
                fileDetailsPane.load(o);
            } else if (o instanceof LabeledList) {
                fileDetailsPane.load(((LabeledList) o).getLabel());
            }
        }
    }

    private class LazyLoadBranchListener implements TreeViewBranchListener {

        public void branchExpanded(TreeView tv, Path path) {

            LabeledList lList = (LabeledList) Sequence.Tree.get(tv.getTreeData(), path);

            if (!lList.isLoaded()) {
                AceItem item = (AceItem) lList.getLabel();
                if (item.isDirectory()) {
                    PartnerSite site = Main.getAceSite();
                    JsonGateway gateway = JsonGateway.getGateway();
                    ParentChildBean details = gateway.getAceItem(site, currentCollection.getId(), item.getPath());

                    for (AceItem child : details.getChildren()) {
                        if (child.isDirectory()) {
                            lList.add(new LabeledList(child));
                        } else {
                            lList.add(child);
                        }
                    }
                }
                lList.setLoaded(true);
            }
        }

        public void branchCollapsed(TreeView tv, Path path) {
        }
    }

    private class FileTreeRenderer extends TreeViewNodeRenderer {

        @Override
        public void render(Object node, Path path, int rowIndex, TreeView treeView, boolean expanded, boolean selected, NodeCheckState checkState, boolean highlighted, boolean disabled) {

            if (node instanceof AceItem) {
                AceItem o = (AceItem) node;
                super.render(o.getPath().substring(o.getParentPath().length() + 1), path, rowIndex, treeView, expanded, selected, checkState, highlighted, disabled);

            } else if (node instanceof LabeledList) {
                AceItem o = (AceItem) ((LabeledList) node).getLabel();
                super.render(o.getPath().substring(o.getParentPath().length() + 1), path, rowIndex, treeView, expanded, selected, checkState, highlighted, disabled);

            } else {
                super.render(node, path, rowIndex, treeView, expanded, selected, checkState, highlighted, disabled);
            }
        }
    }
}
