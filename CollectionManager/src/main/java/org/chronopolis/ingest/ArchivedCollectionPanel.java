/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.NodeCheckState;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.apache.pivot.wtk.content.TreeViewNodeRenderer;
import org.chronopolis.bag.client.BagBean;
import org.chronopolis.bag.client.JsonGateway;
import org.chronopolis.bag.client.Manifest;
import org.chronopolis.bag.client.Manifest.PayloadItem;

/**
 *
 * @author toaster
 */
public class ArchivedCollectionPanel extends Border {

    private static final Logger LOG = Logger.getLogger(ArchivedCollectionPanel.class);
    @BXML
    private TreeView fileTreeView;
    @BXML
    private Border fileDetailsPane;
    @BXML
    private ListView reportListView;
    @BXML
    private Border reportDetailsPane;
    @BXML
    private TablePane collectionTable;
    private BagBean currentCollection = null;

    public ArchivedCollectionPanel() {

        try {
            BXMLSerializer serializer = new BXMLSerializer();
            TablePane mainW = (TablePane) serializer.readObject(ArchivedCollectionPanel.class, "archivedCollectionPanel.bxml");
            serializer.bind(this);
            setContent(mainW);

            MessageBus.subscribe(BagBean.class, new MessageBusListener<BagBean>() {

                public void messageSent(BagBean t) {
                    currentCollection = t;
                    loadFileTree();
//                    loadReports();
                    collectionTable.load(currentCollection);

                    //ArchivedCollectionPanel.this.load(t);
                }
            });

//            fileTreeView.getTreeViewBranchListeners().add(new LazyLoadBranchListener());
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

//    private void loadReports() {
//
//        if (currentCollection != null) {
//            ApplicationContext.queueCallback(new Runnable() {
//
//                public void run() {
//
//                    JsonGateway gateway = Main.getGateway();
////                    SummaryBean reportbean = gateway.getSummaryBean(site);
////                    List ll = new ArrayList();
////                    reportListView.setListData(ll);
////                    for (Summary s : reportbean.getSummaries()) {
////                        if (s.getCollection() == currentCollection.getId()) {
////                            ll.add(s);
////                        }
////                    }
//                }
//            });
//        } else {
//        }
//    }
    private class ReportListListener extends ListViewSelectionListener.Adapter {

        @Override
        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
            reportDetailsPane.load(listView.getSelectedItem());
        }
    }

    private class ReportListRenderer extends ListViewItemRenderer {
//        @Override
//        public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
//            if (item instanceof Summary) {
//                super.render(((Summary) item).getReportName(), index, listView, selected, checked, highlighted, disabled);
//
//            } else {
//                super.render(item, index, listView, selected, checked, highlighted, disabled);
//            }
//        }
    }

    private void loadFileTree() {
        if (currentCollection != null) {
            ApplicationContext.queueCallback(new Runnable() {

                public void run() {
                    JsonGateway gateway = Main.getGateway();
                    Manifest m = gateway.getManifest(currentCollection.getId());
                    LabeledList ll = new LabeledList(m);
                    for (Manifest.PayloadItem pi : m.getPayload()) {
//                        LOG.trace("Loading " + pi.getPath());
                        loadPath(ll, pi);
                    }
                    fileTreeView.setTreeData(ll);
                }
            });
        } else {
            fileTreeView.setTreeData(new ArrayList());
        }
    }

    private void loadPath(LabeledList root, Manifest.PayloadItem pi) {
        String path = pi.getPath();
        if (path != null && !path.isEmpty()) {
            LabeledList currList = root;
            currList.setComparator(LabeledList.getLabelComparator());
            String[] pathList = path.split("/");
            for (int i = 0; i < (pathList.length - 1); i++) {
                int idx = currList.indexOf(pathList[i]);
//                LOG.trace("idx: " + idx + " path " + pathList[i]);
                if (idx != -1) {
                    currList = (LabeledList) currList.get(idx);

                } else {
                    LabeledList newList = new LabeledList(pathList[i]);
                    newList.setComparator(LabeledList.getLabelComparator());
                    currList.add(newList);
                    currList = newList;

                }
            }
            currList.add(pi);
        }
    }

    private class DetailsLoader extends TreeViewSelectionListener.Adapter {
//        @Override
//        public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths) {
//
//            Object o = treeView.getSelectedNode();
//            if (o instanceof AceItem) {
//                fileDetailsPane.load(o);
//            } else if (o instanceof LabeledList) {
//                fileDetailsPane.load(((LabeledList) o).getLabel());
//            }
//        }
    }

    private class FileTreeRenderer extends TreeViewNodeRenderer {

        public FileTreeRenderer() {
            setIconWidth(0);
            setIconHeight(0);
        }

        @Override
        public void render(Object node, Path path, int rowIndex, TreeView treeView, boolean expanded, boolean selected, NodeCheckState checkState, boolean highlighted, boolean disabled) {

            Object renderObj = node;
            if (node instanceof LabeledList) {
                renderObj = ((LabeledList) node).getLabel();
            } else if (node != null) {
                PayloadItem pi = (PayloadItem) node;
                renderObj = pi.getPath().substring(pi.getPath().lastIndexOf("/") + 1);
            }

            super.render(renderObj, path, rowIndex, treeView, expanded, selected, checkState, highlighted, disabled);

        }
    }
}
