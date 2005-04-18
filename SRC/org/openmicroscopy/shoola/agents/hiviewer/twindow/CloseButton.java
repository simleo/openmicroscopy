/*
 * org.openmicroscopy.shoola.agents.hiviewer.twindow.CloseButton
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

package org.openmicroscopy.shoola.agents.hiviewer.twindow;

import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The close button in the {@link TitleBar}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class CloseButton
    extends JButton
{
    
    //TODO: replace this code with proper code for icons!!!
    static String DIR = "../graphx/";
    static ImageIcon CLOSE, CLOSE_OVER;
    static
    {
        java.net.URL imgURL = SizeButton.class.getResource(DIR+"minus.png");
        CLOSE = new ImageIcon(imgURL);
        imgURL = SizeButton.class.getResource(DIR+"minus_over.png");
        CLOSE_OVER = new ImageIcon(imgURL);
    }
    
    /** 
     * Identifies the close action.
     * @see #setActionType(int) 
     */
    static final int    CLOSE_ACTION = 1;
    
    /** Tooltip text when the button repsents the close action. */
    static final String CLOSE_TOOLTIP = "Close";
       
    /** Creates a new instance. */
    CloseButton() 
    {
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        //Just to make sure button sz=icon sz.
        setMargin(new Insets(0, 0, 0, 0));  
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        setRolloverEnabled(true);
    }
    
    /**
     * Sets the button to represent the specified action.
     * 
     * @param type One of the constants defined by this class.
     */
    void setActionType(int type)
    {
        switch (type) {
            case CLOSE_ACTION:
                setIcon(CLOSE);
                setRolloverIcon(CLOSE_OVER);
                setToolTipText(CLOSE_TOOLTIP);
        }
    }
    
    /** Overridden to make sure no focus is painted on top of the icon. */
    public boolean isFocusTraversable() { return false; }
    
    /** Overridden to make sure no focus is painted on top of the icon. */
    public void requestFocus() {}

}