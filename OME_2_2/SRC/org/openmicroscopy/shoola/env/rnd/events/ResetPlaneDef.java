/*
 * org.openmicroscopy.shoola.env.rnd.events.ResetPlaneDef
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

package org.openmicroscopy.shoola.env.rnd.events;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;

/** 
 * 
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
public class ResetPlaneDef
	extends RequestEvent
{

	/** The ID of the pixels set. */
	private int			pixelsID;
		
	/** 
	 * Defines what data, within the pixels set, to render.
	 * May be <code>null</code>, if this is a request to render the current
	 * plane. 
	 */
	private PlaneDef	planeDef;
	
	
	/**
	 * Creates a request to render the given plane within the given
	 * pixels set.
	 * 
	 * @param pixelsID	The ID of the pixels set.
	 * @param planeDef	Selects a plane orthogonal to one of the <i>X</i>, 
	 * 					<i>Y</i>, or <i>Z</i> axes.  Mustn't be 
	 * 					<code>null</code>.
	 */
	public ResetPlaneDef(int pixelsID, PlaneDef planeDef)
	{
		if (planeDef == null)
			throw new NullPointerException("No plane definition.");
		if (planeDef.getSlice() != PlaneDef.XY)
			throw new IllegalArgumentException("Can only reset the XYPlane.");
		this.planeDef = planeDef;
		this.pixelsID = pixelsID;
	}

	/** Return the ID of the pixels set. */
	public int getPixelsID() { return pixelsID; }

	/**
	 * Returns the definition of what data, within the pixels set, to render.
	 * Will return <code>null</code> if this is a request to render the current
	 * plane.
	 * 
	 * @return	See above.
	 */
	public PlaneDef getPlaneDef() { return planeDef; }

}
