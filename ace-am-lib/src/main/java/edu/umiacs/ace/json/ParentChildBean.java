/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.json;
import java.util.List;

/**
 *
 * @author toaster
 */
public class ParentChildBean
{
//    private static final Logger LOG = Logger.getLogger(ParentChildBean.class);
    private AceItem parent;
    private List<AceItem> children;

    public List<AceItem> getChildren()
    {
//        if (children != null && children.size() > 0)
//        {
//            LOG.trace("get type" + children.get(0).getClass());
//        }

        return children;
    }

    public void setChildren(List<AceItem> children)
    {
//        if (children != null && children.size() > 0)
//        {
//            LOG.trace("set type" + children.get(0).getClass());
//            Thread.dumpStack();
//        }
        this.children = children;
    }

    public AceItem getParent()
    {
        return parent;
    }

    public void setParent(AceItem parent)
    {
        this.parent = parent;
    }
}
