/*
 * org.openmicroscopy.shoola.env.rnd.GreyScaleStrategy
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.data.Plane2D;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumStrategy;

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
class GreyScaleStrategy 
	extends RenderingStrategy
{
	
	/** Number of Pixels on the X1-axis. */
	private int 		sizeX1;
	
	/** Number of Pixels on the X2-axis. */
	private int 		sizeX2;
	
	private Renderer	renderer;
	
	BufferedImage render(Renderer ctx)
		throws DataSourceException, QuantizationException
	{
		//Set the context and retrieve rendering settings.
		renderer = ctx;
		PixelsDimensions dims = renderer.getPixelsDims();
		PlaneDef planeDef = renderer.getPlaneDef();
		RenderingDef rndDef = renderer.getRenderingDef();
		QuantumManager qManager = renderer.getQuantumManager();
		DataSink dSink = renderer.getDataSink();
		ChannelBindings[] cBindings = rndDef.getChannelBindings();
		
		//Initialize sizeX1 and sizeX2 according to the plane definition and
		//create the GS buffer used by the Java2D API.
		initAxesSize(planeDef, dims);
		DataBufferByte renderedDataBuf = new DataBufferByte(sizeX1*sizeX2, 3);

		//Process the first active wavelength. 
		Plane2D wData;
		for (int i = 0; i < cBindings.length; i++) {
			if (cBindings[i].isActive()) {
				//NOTE: RenderingDef enforces the constraint 
				wData = dSink.getPlane2D(planeDef, i);
				try {
					renderWave(renderedDataBuf, wData, 
								qManager.getStrategyFor(i), 
								cBindings[i].getRGBA());
				} catch (QuantizationException e) {
					e.setWavelength(i);
					throw e;
				}
				break;
			}
		}

		//Now we only need to tell Java2D how to handle the RGB buffer. 
		ComponentColorModel ccm = new ComponentColorModel(
									ColorSpace.getInstance(ColorSpace.CS_sRGB), 
									null, false, false, Transparency.OPAQUE, 
									DataBuffer.TYPE_BYTE);
		BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
									sizeX1, sizeX2, 3);
		return new BufferedImage(ccm, 
					Raster.createWritableRaster(csm, renderedDataBuf, null), 
					false, null);
	}

	/** 
	 * Initialize the sizes <code>sizeX1</code> and <code>sizeX2</code>
	 * according to the specified {PlaneDef#slice slice}.
	 * @param pd	reference to the PlaneDef object defined for the strategy.
	 * @param d		image dimensions object.
	 */
	private void initAxesSize(PlaneDef pd, PixelsDimensions d)
	{
		try {
			switch (pd.getSlice()) {
				case PlaneDef.XY:
					initSizes(d.sizeX, d.sizeY);
					break;
				case PlaneDef.XZ:
					initSizes(d.sizeX, d.sizeZ);
					break;
				case PlaneDef.ZY:
					initSizes(d.sizeZ, d.sizeY);
			}	
		}catch(NumberFormatException nfe) {   
			throw new Error("Invalid Action ID "+pd.getSlice(), nfe);
		} 
	}
	
	/** Initializes the fields <code>sizeX1</code> and <code>sizeX2</code>. */
	private void initSizes(int sizeX1, int sizeX2)
	{
		this.sizeX1 = sizeX1;
		this.sizeX2 = sizeX2;
	}
	
	/** Render an active wavelength. */
	private void renderWave(DataBufferByte dataBuf, Plane2D plane, 
		QuantumStrategy qs, int[] rgba)
		throws QuantizationException
	{
		CodomainChain cc = renderer.getCodomainChain();
		int x1, x2, discreteValue, v, value;
		float alpha = ((float) rgba[3])/255;
		for (x2 = 0; x2 < sizeX2; ++x2) {
			for (x1 = 0; x1 < sizeX1; ++x1) {
				discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
				v = cc.transform(discreteValue);
				value = (int) (v*alpha);
				dataBuf.setElem(RGBStrategy.R_BAND, sizeX1*x2+x1, value);
				dataBuf.setElem(RGBStrategy.G_BAND, sizeX1*x2+x1, value);
				dataBuf.setElem(RGBStrategy.B_BAND, sizeX1*x2+x1, value);
	   		} 
		}
	}
	
}

