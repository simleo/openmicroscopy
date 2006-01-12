/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.HiViewerWin
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;

import java.util.Iterator;
import java.util.Map;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class RootLevelCmd
	implements ActionCmd
{

    /** Reference to the model. */
    private TreeViewer	model;
    
    /** 
     * The root of the hierarchy. One of the following constants:
     * {@link TreeViewer#WORLD_ROOT}, {@link TreeViewer#USER_ROOT} and
     * {@link TreeViewer#GROUP_ROOT}.
     */
    private int 		rootLevel;
    
    /** 
     * The id of the root node. This field is only used if the 
     * {@link #rootLevel} is {@link TreeViewer#GROUP_ROOT}.
     */
    private int 		rootID;
    
    /**
     * Checks if the specified level is supported.
     * 
     * @param level The level to control.
     */
    private void checkLevel(int level)
    {
        switch (level) {
	        case TreeViewer.WORLD_ROOT:
	        case TreeViewer.USER_ROOT:
	        case TreeViewer.GROUP_ROOT:    
	            return;
	        default:
	            throw new IllegalArgumentException("Root level not supported");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param rootLevel The root of the hierarchy.
     * 					One of the following constants:
     * 					{@link TreeViewer#WORLD_ROOT}, 
     * 					{@link TreeViewer#USER_ROOT},
     * 					{@link TreeViewer#GROUP_ROOT}.
     * @param rootID The id of the root node.
     */
    public RootLevelCmd(TreeViewer model, int rootLevel, int rootID)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        checkLevel(rootLevel);
        if (rootID < 0) 
            throw new IllegalArgumentException("Root ID not valid.");
        this.model = model;
        this.rootLevel = rootLevel;
        this.rootID = rootID;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        Map browsers = model.getBrowsers();
        Iterator i = browsers.values().iterator();
        while (i.hasNext())
            ((Browser) i.next()).setHierarchyRoot(rootLevel, rootID);
    }

}
