/*
 * org.openmicroscopy.shoola.agents.browser.images.ImageAnnotationOverlay
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ImageAnnotationOverlay extends OverlayNode
{
    private final Shape imageShape =
        PaintShapeGenerator.getInstance().getAnnotationNoteShape(0,0);
    private final Color fillColor = new Color(255,255,192);
    
    private ImageAnnotationNode popupNode;
    
    /**
     * Constructs an ImageAnnotationOverlay that bases its actions and display
     * action on the context of the parent thumbnail.
     * @param parent The parent thumbnail.
     */
    public ImageAnnotationOverlay(final Thumbnail parent)
    {
        super(OverlayNodeDictionary.ANNOTATION_NODE,
              parent);
        setBounds(imageShape.getBounds2D());
        
        final ImageAnnotationOverlay refCopy = this;
        
        PiccoloAction mouseEnterAction = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                AttributeMap map = parent.getModel().getAttributeMap();
                ImageAnnotation ia = 
                    (ImageAnnotation)map.getAttribute("ImageAnnotation");
                PCamera camera = e.getCamera();
                Rectangle2D cameraBounds = camera.getBounds().getBounds2D();
                popupNode = new ImageAnnotationNode(ia,refCopy,camera);
                
                Point2D originalPoint = new Point2D.Double(getX(),getY());
                Point2D offset = camera.viewToLocal(localToGlobal(originalPoint));
                double x = offset.getX();
                double y = offset.getY();
                if(offset.getX()+popupNode.getWidth() > cameraBounds.getWidth())
                {
                    x = cameraBounds.getWidth()-popupNode.getWidth();
                }
                if(offset.getY()+popupNode.getHeight() > cameraBounds.getHeight())
                {
                    y = cameraBounds.getHeight()-popupNode.getHeight();
                }
                popupNode.setOffset(new Point2D.Double(x,y));
                repaint();
            }
        };
        
        setOffset(parent.getBounds().getWidth()-10,
                  parent.getBounds().getHeight()-10);
        
        getMouseOverActions().setMouseEnterAction(PiccoloModifiers.NORMAL,
                                                  mouseEnterAction);
                                                  
        PiccoloAction annotateAction =
            PiccoloActionFactory.getAnnotateImageAction(parent);
        
        getMouseDownActions().setMouseClickAction(PiccoloModifiers.NORMAL,
                                                  annotateAction);
    }
    
    public void paint(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        g2.setPaint(fillColor);
        g2.fill(imageShape);
        g2.setColor(Color.black);
        g2.draw(imageShape);
    }
}
