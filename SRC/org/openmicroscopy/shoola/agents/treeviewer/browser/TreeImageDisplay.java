/*
 * org.openmicroscopy.shoola.agents.treemng.browser.TreeImageDisplay
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;

import pojos.ImageData;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class TreeImageDisplay
    extends DefaultMutableTreeNode
{
    
    /** 
     * Back pointer to the parent node or <code>null</code> if this is the root.
     */
    private TreeImageDisplay    parentDisplay;
    
    /** 
     * The set of nodes that have been added to this node.
     * Will always be empty for a leaf node. 
     */
    private Set                 childrenDisplay;
    
    /** The name of the hierarchy object. */
    private String              name;
    
    /**
     * The tooltip: annotation if the <code>DataObject</code>
     * can be annotated and the inserted date if any.
     */
    private String              tooltip;
    
    /** The number of items. */
    protected int				numberItems;
    
    /**
     * Checks if the algorithm to visit the tree is one of the constants
     * defined by {@link TreeImageDisplayVisitor}.
     * 
     * @param type The algorithm type.
     * @return  Returns <code>true</code> if the type is supported,
     *          <code>false</code> otherwise.
     */
    private boolean checkAlgoType(int type)
    {
        switch (type) {
            case TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY:
            case TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY:    
            case TreeImageDisplayVisitor.ALL_NODES:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Constructor used by subclasses.
     * 
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node.  
     *                        Never pass <code>null</code>. 
     */
    protected TreeImageDisplay(Object hierarchyObject)
    {
        super();
        if (hierarchyObject == null) 
            throw new NullPointerException("No hierarchy object.");
        if (hierarchyObject instanceof String) name = (String) hierarchyObject;
        setUserObject(hierarchyObject);
        childrenDisplay = new HashSet();
        numberItems = -1;
    }
    
    /**
     * Returns the parent node to this node in the visualization tree.
     * 
     * @return The parent node or <code>null</code> if this node has no parent.
     *          This can happen if this node hasn't been linked yet or if it's
     *          the root node.
     */
    public TreeImageDisplay getParentDisplay() { return parentDisplay; }
    
    /**
     * Returns all the child nodes to this node in the visualization tree.
     * Note that, although never <code>null</code>, the returned set may be
     * empty.  In particular, this is always the case for a leaf node &#151;
     * that is an {@link TreeImageNode}.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
    public Set getChildrenDisplay() 
    { 
        return Collections.unmodifiableSet(childrenDisplay);
    }
    
    /**
     * Adds a node to the visualization tree as a child of this node.
     * The node is added to the internal desktop of this node, but you
     * will have to set its bounds for it to show up &#151; this is a
     * consequence of the fact that a desktop has no layout manager.
     * The <code>child</code>'s parent is set to be this node.  If <code>
     * child</code> is currently a child to another node <code>n</code>, 
     * then <code>child</code> is first 
     * {@link #removeChildDisplay(TreeImageDisplay) removed} from <code>n</code>
     * and then added to this node. 
     * 
     * @param child The node to add. Mustn't be <code>null</code>.
     * @see DefaultMutableTreeNode
     */
    public void addChildDisplay(TreeImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) return;
        if (child.parentDisplay != null)  //Was the child of another node.
            child.parentDisplay.removeChildDisplay(child);
        child.parentDisplay = this;
        childrenDisplay.add(child);
        numberItems = childrenDisplay.size();
    }
    
    /**
     * Removes the specified <code>child</code> node.
     * If <code>child</code> is not among the children of this node, no action
     * is taken. Otherwise, it is removed from the children set and orphaned.
     * That is, its parent (which is this node) is set to <code>null</code>.
     * 
     * @param child The node to remove. Mustn't be <code>null</code>.
     */
    public void removeChildDisplay(TreeImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) {
            //NOTE: parentDisplay != null b/c child has been added through
            //the add method.
            child.parentDisplay.childrenDisplay.remove(child);
            child.parentDisplay = null;
            numberItems = childrenDisplay.size();
        }
    }
    
    /** Removes all <code>children</code> nodes from the children set. */
    public void removeAllChildrenDisplay()
    {
        Iterator i = childrenDisplay.iterator();
        Set toRemove = new HashSet(childrenDisplay.size());
        while (i.hasNext())
            toRemove.add(i.next());
        i = toRemove.iterator();
        while (i.hasNext())
            removeChildDisplay((TreeImageDisplay) i.next());
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * For each node, the <code>visit</code> method is called passing in the
     * node being visited.
     * 
     * @param visitor The visitor. Mustn't be <code>null</code>.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * According to the specified <code>algoType</code>,
     * the <code>visit</code> method is called passing in the
     * node being visited. 
     * 
     * @param visitor   The visitor. Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected. Must be one of the constants
     *                  defined by {@link TreeImageDisplayVisitor}.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor, int algoType)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        if (!checkAlgoType(algoType))
            throw new IllegalArgumentException("Algorithm not supported.");
        Iterator i = childrenDisplay.iterator();
        TreeImageDisplay child;
        switch (algoType) {
            case TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY:
                while (i.hasNext()) {
                    child = (TreeImageDisplay) i.next();
                    child.accept(visitor, algoType);
                }
                if (this instanceof TreeImageNode) doAccept(visitor);
                break;
            case TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY:
                while (i.hasNext()) {
                    child = (TreeImageDisplay) i.next();
                    if (child instanceof TreeImageSet)
                        child.accept(visitor, algoType);
                }
                if (this instanceof TreeImageSet) doAccept(visitor);
                break;
            case TreeImageDisplayVisitor.ALL_NODES:
                while (i.hasNext()) {
                    child = (TreeImageDisplay) i.next();
                    child.accept(visitor, algoType);
                }
                doAccept(visitor);
                break;
        }
    }
    
    /**
     * Sets the text displayed in a tool tip.
     * 
     * @param tooltip The text to set.
     */
    public void setToolTip(String tooltip) { this.tooltip = tooltip; }
    
    /**
     * Returns the text displayed in a tool tip.
     * 
     * @return See above.
     */
    public String getToolTip() { return tooltip; }
    
    /**
     * Sets the name of the node.
     * 
     * @param name The name of the node.
     */
    public void setNodeName(String name) { this.name = name; }
    
    /**
     * Returns the name of the node.
     * 
     * @return See above.
     */
    public String getNodeName() { return name; }
    
    /**
     * Returns the name of the node and the number of items
     * in the specified containers.
     * 
     * @return See above.
     */
    public String getNodeText()
    {
        if (getUserObject() instanceof ImageData) return name;
        if (numberItems == -1) return (name+"    ...");
        String s = "item";
        if (numberItems > 1) s +="s";
        return (name+"    "+numberItems+" "+s);
    }
    
    /** 
     * Overriden to return the name of the hierarchy object. 
     * @see #toString()
     */
    public String toString() { return name; }
    
    /**
     * Made final to ensure objects are compared by reference so that the
     * {@link #addChildDisplay(TreeImageDisplay) addChildDisplay} and
     * {@link #removeChildDisplay(TreeImageDisplay) removeChildDisplay} methods
     * will work fine.
     * @see #equals(Object)
     */
    public final boolean equals(Object x) { return (this == x); }
    
    /**
     * Implemented by subclasses to call the right version of the <code>visit
     * </code> method on the specified <code>visitor</code>.
     * This method is called by {@link #accept(TreeImageDisplayVisitor)} during
     * the nodes iteration.  Subclasses will just call the <code>visit</code>
     * method passing a reference to <code>this</code>.
     * 
     * @param visitor The visitor.  Will never be <code>null</code>.
     */
    protected abstract void doAccept(TreeImageDisplayVisitor visitor);
    
    /**
     * Tells if the children of this node are {@link TreeImageNode}s.
     * 
     * @return <code>true</code> if there's at least one {@link TreeImageNode} 
     *          child, <code>false</code> otherwise.
     */
    public abstract boolean containsImages();
    
}
