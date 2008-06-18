/*
 * blitzgateway.service.ServiceFactory 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omeroj.service;

//Java imports
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
//Third-party libraries

//Application-internal dependencies

import omero.RType;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;
import omeroj.service.gateway.GatewayFactory;
import omeroj.service.stateful.RawFileStoreService;
import omeroj.service.stateful.RawFileStoreServiceImpl;
import omeroj.service.stateful.RawPixelsStoreService;
import omeroj.service.stateful.RawPixelsStoreServiceImpl;
import omeroj.service.stateful.RenderingService;
import omeroj.service.stateful.RenderingServiceImpl;
import omeroj.service.stateful.ThumbnailService;
import omeroj.service.stateful.ThumbnailServiceImpl;
import omeroj.util.OMEROClass;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OmeroJService
{		
	/** The gateway factory to create make connection, create and access 
	 *  services .
	 */
	private GatewayFactory 	gatewayFactory;
	
	/** The Data service object. */
	private DataService 	dataService;
	
	/** The Image service object. */
	private ImageService	imageService;

	/** The FileService object. */
	private FileService		fileService;
	
	/** The rendering service object. */
	private RenderingService renderingService;
	
	/** The rawFile service object. */
	private RawFileStoreService rawFileStoreService;
	
	/** The raw pixels service object. */
	private RawPixelsStoreService rawPixelsStoreService;
	
	/** The thumbnail service. */
	private ThumbnailService 	thumbnailService;
	
	private HeartBeatService heartbeatService;
	
	/**
	 * Create the service factory which creates the gateway and services
	 * and links the different services together.  
	 * 
	 * @param iceConfig path to the ice config file.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public OmeroJService(String iceConfig) 
		throws DSOutOfServiceException, DSAccessException
	{
		gatewayFactory = new GatewayFactory(iceConfig);
	}
	
	/**
	 * Create the service factory which creates the gateway and services
	 * and links the different services together.  
	 * 
	 * @param client an already existing client object.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public OmeroJService(omero.client client) 
	throws DSOutOfServiceException, DSAccessException
	{
		gatewayFactory = new GatewayFactory(client);
		startServices();
	}
	/**
	 * Is the session closed?
	 * @return true if closed.
	 */
	public boolean isClosed()
	{
			return gatewayFactory.isClosed();
	}
	
	/**
	 * Close the session with the server.
	 */
	public void close()
	{
		heartbeatService.scheduler.shutdown();
		gatewayFactory.close();
		dataService = null;
		imageService = null;
		fileService = null;
		renderingService = null;
		rawFileStoreService = null;
		rawPixelsStoreService = null;
		thumbnailService = null;
	}
	
	/**
	 * Inner class which starts the heartbeat service, this keeps all services
	 * alive in the session.
	 */
	class HeartBeatService
	{
		/**
		 * We only need one thread. 
		 */
		private final ScheduledExecutorService	scheduler	=
										Executors.newScheduledThreadPool(1);
		/**
		 * This starts the service.
		 */
		public void heartBeat()
		{
			final Runnable beat=new Runnable()
			{
				public void run() 
				{
					try{
						keepAlive();
					}catch(Exception e)
					{
					}
				}
			};
			
			/** 
			 * handle to the service.
			 */
			final ScheduledFuture<?> beatHandle=
					scheduler.scheduleAtFixedRate(beat, 10, 10, SECONDS);
						
		}
	}
	 
	/**
	 * Start all the services from the gatewayFactory.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private void startServices() throws DSOutOfServiceException, DSAccessException
	{
		renderingService = new RenderingServiceImpl(gatewayFactory);
		thumbnailService = new ThumbnailServiceImpl(gatewayFactory);
		rawFileStoreService = new RawFileStoreServiceImpl(gatewayFactory);
		rawPixelsStoreService = new RawPixelsStoreServiceImpl(gatewayFactory);
		dataService = new DataServiceImpl(gatewayFactory.getIPojoGateway(), 
			gatewayFactory.getIQueryGateway(), 
			gatewayFactory.getITypeGateway(),
			gatewayFactory.getIUpdateGateway()
		);
		imageService = new ImageServiceImpl(
			rawPixelsStoreService,
			renderingService,
			thumbnailService,
			gatewayFactory.getIPixelsGateway(), 
			gatewayFactory.getIQueryGateway(), 
			gatewayFactory.getIUpdateGateway());
		fileService = new FileServiceImpl(rawFileStoreService, 
			gatewayFactory.getIScriptGateway(), 
			gatewayFactory.getIQueryGateway());
		heartbeatService = new HeartBeatService();
		heartbeatService.heartBeat();
	}
	 
	/**
	 * Open a session to the server with username and password.
	 * 
	 * @param username
	 *            see above.
	 * @param password
	 *            see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void createSession(String username, String password) 
				throws DSOutOfServiceException, DSAccessException
	{
		gatewayFactory.createSession(username, password);
		startServices();
	}
	
	/**
	 * Get the projects, and datasets in the OMERO.Blitz server in the user 
	 * account.
	 * @param ids user ids to get the projects from, if null will retrieve all
	 * projects from the users account.
	 * @param withLeaves get the projects, images and pixels too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Project> getProjects(List<Long> ids, boolean withLeaves) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getProjects(ids, withLeaves);
	}

	/**
	 * Get the datasets in the OMERO.Blitz server in the projects ids.
	 * @param ids of the datasets to retrieve, if null get all users datasets.
	 * @param withLeaves get the images and pixels too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean withLeaves) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getDatasets(ids, withLeaves);
	}

	/**
	 * Get the pixels associated with the image, this is normally one pixels per
	 * image, but can be more.
	 * @param imageId
	 * @return the list of pixels.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromImage(long imageId) 
		throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromImage(imageId);
	}

	
	/**
	 * Get the image with id
	 * @param id see above
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Image getImage(long id) 
			throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getImage(id);
	}
	
	/**
	 * Get the images in the OMERO.Blitz server from the object parentType with
	 * id's in list ids. 
	 * @param parentType see above.
	 * @param ids see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImages(OMEROClass parentType, List<Long> ids ) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImages(parentType, ids);
	}

	/**
	 * Run the query passed as a string in the iQuery interface. This method will
	 * return list of objects.
	 * @param myQuery string containing the query.
	 * @return the result.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Object findAllByQuery(String myQuery) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.findAllByQuery(myQuery);
	}
	
	/**
	 * Run the query passed as a string in the iQuery interface.
	 * The method expects to return only one result from the query, if more than
	 * one result is to be returned the method will throw an exception.
	 * @param myQuery string containing the query.
	 * @return the result.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Object findByQuery(String myQuery) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.findByQuery(myQuery);
	}
	
	/**
	 * Get the raw plane for the pixels pixelsId, this returns a 2d array 
	 * representing the plane, it returns doubles but will not lose data.
	 * @param pixelsId id of the pixels to retrieve.
	 * @param c the channel of the pixels to retrieve.
	 * @param t the time point to retrieve.
	 * @param z the z section to retrieve.
	 * @return The raw plane in 2-d array of doubles. 
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public double[][] getPlane(long pixelsId, int z, int c, int t) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPlane(pixelsId, z, c, t);
	}
	
	
	/**
	 * Get the pixels information for an image, this method will also 
	 * attach the logical channels, channels, and other metadata in the pixels.
	 * @param pixelsId image id relating to the pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels(long pixelsId) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPixels(pixelsId);
	}
	
	/**
	 * Copy the pixels to a new pixels, this is only the data object 
	 * and does not create a pixels object in the RawPixelsStore,
	 * To load data into the plane the {@link #uploadPlane(long, int, int, int, double[][])} 
	 * to add data to the pixels. 
	 * @param pixelsID pixels id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param channelList the list of channels to copy, this is the channel index.
	 * @param methodology user supplied text, describing the methods that 
	 * created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long copyPixels(long pixelsID, int x, int y,
		int t, int z, List<Integer> channelList, String methodology) throws 
		DSOutOfServiceException, DSAccessException
	{
		return imageService.copyPixels(pixelsID, x, y, t, z, channelList, methodology);
	}
	
	
	/**
	 * Copy the pixels to a new pixels, this is only the data object 
	 * and does not create a pixels object in the RawPixelsStore,
	 * To load data into the plane the {@link #uploadPlane(long, double[][])} 
	 * to add data to the pixels. 
	 * @param pixelsID pixels id to copy.
	 * @param channelList the list of channels to copy, this is the channel index.
	 * @param methodology user supplied text, describing the methods that 
	 * created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long copyPixels(long pixelsID, List<Integer> channelList, String methodology) throws 
		DSOutOfServiceException, DSAccessException
	{
		return imageService.copyPixels(pixelsID, channelList, methodology);
	}
	
	/**
	 * Copy the image and it's attached pixels and 
	 * metadata to a new Image and return the id of the new image. The method 
	 * will not copy annotations or attachments. 
	 * @param imageId image id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t The number of time-points
	 * @param z The number of zSections.
	 * @param channelList the list of channels to copy, [0-(sizeC-1)].
	 * @param imageName The new imageName.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long copyImage(long imageId, int x, int y,
		int t, int z, List<Integer> channelList, String imageName) throws 
		DSOutOfServiceException, DSAccessException
	{
		return imageService.copyImage(imageId, x, y, t, z, channelList, imageName);
	}
	
	/**
	 * Upload the plane to the server, on pixels id with channel and the 
	 * time, + z section. the data is the client 2d data values. This will
	 * be converted to the raw server bytes.
	 * @param pixelsId pixels id to upload to .  
	 * @param z z section. 
	 * @param c channel.
	 * @param t time point.
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t, 
			double [][] data) throws DSOutOfServiceException, DSAccessException
	{
		imageService.uploadPlane(pixelsId, z, c, t, data);
	}

	/**
	 * Update the pixels object on the server, updating appropriate tables in the
	 * database and returning a new copy of the pixels.
	 * @param object see above.
	 * @return the new updated pixels.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels updatePixels(Pixels object) 
	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.updatePixels(object);
	}

	/**
	 * Get a list of all the possible pixelsTypes in the server.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<PixelsType> getPixelTypes() 
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelTypes();
	}

	/**
	 * Get the pixelsType for type of name type.
	 * @param type see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public PixelsType getPixelType(String type) 
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelType(type);
	}
	
	/**
	 * Get the scripts from the iScript Service. 
	 * @return All the available scripts in a map by id and name.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<Long, String> getScripts() throws   DSOutOfServiceException, 
											DSAccessException
	{
		return fileService.getScripts();
	}
	
	/**
	 * Get the id of the script with name 
	 * @param name name of the script.
	 * @return the id of the script.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long getScriptID(String name) throws DSOutOfServiceException, 
										 DSAccessException
	{
		return fileService.getScriptID(name);
	}
	
	/**
	 * Upload the script to the server.
	 * @param script script to upload
	 * @return id of the new script.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long uploadScript(String script) throws DSOutOfServiceException, 
											DSAccessException	
	{
		return fileService.uploadScript(script);
	}

	/**
	 * Get the script with id, this returns the actual script as a string.
	 * @param id id of the script to retrieve.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public String getScript(long id) throws DSOutOfServiceException, 
									 DSAccessException
	{
		return fileService.getScript(id);
	}
	
	/**
	 * Get the parameters the script takes, this is a map of the parameter name and type. 
	 * @param id id of the script.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<String, RType> getParams(long id) throws DSOutOfServiceException, 
												 DSAccessException
	{
		return fileService.getParams(id);
	}
	
	/**
	 * Run the script and get the results returned as a name , value map.
	 * @param id id of the script to run.
	 * @param map the map of parameters, values for inputs.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<String, RType> runScript(long id, Map<String, RType> map) 
						throws DSOutOfServiceException, DSAccessException
	{
		return fileService.runScript(id, map);
	}
	
	/**
	 * Delete the script with id from the server.
	 * @param id id of the script to delete.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void deleteScript(long id) throws 	DSOutOfServiceException, 
										DSAccessException
	{
		fileService.deleteScript(id);
	}	
	
	/**
	 * Get the zSection stack from the pixels at timepoint t
	 * @param pixelId The pixelsId from the imageStack.
	 * @param c The channel.
	 * @param t The time-point.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double[][][] getPlaneStack(long pixelId, int c, int t) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPlaneStack(pixelId, c, t);
	}
	
	/**
	 * Render the pixels for the zSection z and timePoint t. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return The image as a buffered image.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public BufferedImage getRenderedImage(long pixelsId, int z, int t)	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getRenderedImage(pixelsId, z, t);
	}

	/**
	 * Render the pixels for the zSection z and timePoint t. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return The image as a 3d array where it represents the image as 
	 * [x][y][channel]
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getRenderedImageMatrix(pixelsId, z, t);
	}
	
	/**
	 * Render the pixels for the zSection z and timePoint t. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return The pixels are returned as 4 bytes representing the r,g,b,a of 
	 * image.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] renderAsPackedInt(long pixelsId, int z, int t) throws DSOutOfServiceException, DSAccessException
	{
		return imageService.renderAsPackedInt(pixelsId, z, t);
	}
	
	/**
	 * Set the active channels to be on or off in the rendering engine for 
	 * the pixels.
	 * @param pixelsId the pixels id.
	 * @param w the channel
	 * @param active set active?
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setActive(long pixelsId, int w, boolean active) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setActive(pixelsId, w, active);
	}

	/**
	 * Is the channel active, turned on in the rendering engine.
	 * @param pixelsId the pixels id.
	 * @param w channel
	 * @return true if the channel active.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public boolean isActive(long pixelsId, int w) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.isActive(pixelsId, w);
	}

	/**
	 * Get the default zSection of the image, this is the zSection the image 
	 * should open on when an image viewer is loaded.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int getDefaultZ(long pixelsId) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getDefaultZ(pixelsId);
	}
	
	/**
	 * Get the default time-point of the image, this is the time-point the image 
	 * should open on when an image viewer is loaded.
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int getDefaultT(long pixelsId) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getDefaultT(pixelsId);
	}
	
	/**
	 * Set the default zSection of the image, this is the zSection the image 
	 * should open on when an image viewer is loaded.
	 * @param pixelsId the pixelsId of the image.
	 * @param z see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setDefaultZ(long pixelsId, int z) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setDefaultZ(pixelsId, z);
	}
	
	/**
	 * Set the default timepoint of the image, this is the timepoint the image 
	 * should open on when an image viewer is loaded.
	 * @param pixelsId the pixelsId of the image.
	 * @param t see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setDefaultT(long pixelsId, int t) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setDefaultT(pixelsId, t);
	}
		
	/**
	 * Set the channel Minimum, Maximum values, that map from image space to 
	 * rendered space (3 channel, 8 bit, screen).  
	 * @param pixelsId the pixelsId of the image the mapping applied to.
	 * @param w channel of the pixels.
	 * @param start The minimum value to map from.
	 * @param end The maximum value to map to.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setChannelWindow(long pixelsId, int w, double start, double end) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setChannelWindow(pixelsId, w, start, end);
	}
	
	/**
	 * Get the channel Minimum value, that maps from image space to 
	 * rendered space.  
	 * @param pixelsId the pixelsId of the image the mapping applied to.
	 * @param w channel of the pixels.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double getChannelWindowStart(long pixelsId, int w) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getChannelWindowStart(pixelsId, w);
	}
	
	/**
	 * Get the channel Maximum value, that maps from image space to 
	 * rendered space.  
	 * @param pixelsId the pixelsId of the image the mapping applied to.
	 * @param w channel of the pixels.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double getChannelWindowEnd(long pixelsId, int w) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getChannelWindowEnd(pixelsId, w);
	}
	
	/**
	 * Set the rendering definition of the rendering engine from the default 
	 * to the one supplied. This allows for more than one rendering definition-
	 * mapping per pixels.
	 * @param pixelsId for pixelsId 
	 * @param renderingDefId see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setRenderingDefId(long pixelsId, long renderingDefId) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setRenderingDefId(pixelsId, renderingDefId);
	}
	
	/**
	 * Get the thumbnail of the image.
	 * @param pixelsId for pixelsId 
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getThumbnail(long pixelsId, omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnail(pixelsId, sizeX, sizeY);
	}
	
	/**
	 * Get a set of thumbnails, of size X, Y from the list of pixelId's supplied
	 * in the list.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<Long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}
	
	/**
	 * Get a set of thumbnails from the pixelsId's in the list, 
	 * maintaining aspect ratio.
	 * @param size size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<Long, byte[]>getThumbnailBylongestSideSet(omero.RInt size, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnailByLongestSideSet(size, pixelsIds);
	}
	
	/**
	 * Get the thumbnail of the image, maintain aspect ratio.
	 * @param pixelsId for pixelsId 
	 * @param size size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getThumbnailBylongestSide(long pixelsId, omero.RInt size) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnailByLongestSide(pixelsId, size);
	}
	
	/**
	 * Attach an image to a dataset.
	 * @param dataset see above.
	 * @param image see above.
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 * 
	 */
	public void attachImageToDataset(Dataset dataset, Image image) throws  DSOutOfServiceException, DSAccessException
	{
		dataService.attachImageToDataset(dataset, image);
	}

	/**
	 * Create a new Image of X,Y, and zSections+time-points. The channelList is 
	 * the emission wavelength of the channel and the pixelsType.
	 * @param sizeX width of plane.
	 * @param sizeY height of plane.
	 * @param sizeZ num zSections.
	 * @param sizeT num time-points
	 * @param channelList the list of channels to copy.
	 * @param pixelsType the type of pixels in the image.
	 * @param name the image name.
	 * @param description the description of the image.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
			List<Integer> channelList, PixelsType pixelsType, String name,
			String description) throws DSOutOfServiceException,
			DSAccessException
	{
		return imageService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, name, description);
	}
	
	/**
	 * Get the images from as dataset.
	 * @param dataset see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImagesFromDataset(Dataset dataset)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImagesFromDataset(dataset);
	}
	
	/**
	 * Get the plane from the image with imageId.
	 * @param imageId see above.
	 * @param z zSection of the plane.
	 * @param c channel of the plane.
	 * @param t timepoint of the plane.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double[][] getPlaneFromImage(long imageId, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException
	{
		List<Pixels> pixels = getPixelsFromImage(imageId);
		Pixels firstPixels = pixels.get(0);
		return getPlane(firstPixels.id.val, z, c, t);
	}
	
	/**
	 * This is a helper method and makes no calls to the server. It 
	 * gets a list of all the dataset in a project if the project has already
	 * had the datasets attached, via getLeaves in {@link #getProjects(List, boolean)}
	 * or fetched via HQL in {@link #findAllByQuery(String)}, {@link #findByQuery(String)} 
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasetsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getDatasetsFromProject(project);
	}
	
	/**
	 * This is a helper method and makes no calls to the server. It 
	 * gets a list of all the pixels in a dataset if the dataset has already
	 * had the pixels attached, via getLeaves in {@link #getProjects(List, boolean)}
	 * {@link #getDatasets(List, boolean)} or fetched via HQL in 
	 * {@link #findAllByQuery(String)}, {@link #findByQuery(String)} 
	 * @param dataset see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromDataset(Dataset dataset)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromDataset(dataset);
	}
	
	/**
	 * This is a helper method and makes no calls to the server. It 
	 * gets a list of all the pixels in a project if the project has already
	 * had the pixels attached, via getLeaves in {@link #getProjects(List, boolean)}
	 * or fetched via HQL in {@link #findAllByQuery(String)}, 
	 * {@link #findByQuery(String)} 
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromProject(project);
	}
	
	/**
	 * This is a helper methods, which makes no calls to the server. It get all
	 * the pixels attached to a list of images. It requires that the pixels are
	 * already attached via  {@link #getProjects(List, boolean)}
	 * {@link #getDatasets(List, boolean)} or fetched via HQL in 
	 * {@link #findAllByQuery(String)}, {@link #findByQuery(String)}
	 * Get the pixels from the images in the list.
	 * @param images see above.
	 * @return map of the pixels-->imageId.
	 */
	public Map<Long, Pixels> getPixelsImageMap(List<Image> images)
	{
		return dataService.getPixelsImageMap(images);
	}


	/**
	 * This is a helper methods, which makes no calls to the server. It get all
	 * the pixels attached to a list of images. It requires that the pixels are
	 * already attached via  {@link #getProjects(List, boolean)}
	 * {@link #getDatasets(List, boolean)} or fetched via HQL in 
	 * {@link #findAllByQuery(String)}, {@link #findByQuery(String)}
	 * Get the pixels from the images in the list.
	 * @param images see above.
	 * @return list of the pixels.
	 */
	public List<Pixels> getPixelsFromImageList(List<Image> images)
	{
		return getPixelsFromImageList(images);
	}
	
	/**
	 * Get the images from the dataset with name, this can use wild cards.
	 * @param datasetId see above.
	 * @param imageName see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImageFromDatasetByName(long datasetId, String imageName)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImageFromDatasetByName(datasetId, imageName);
	}

	/**
	 * Get the list of images with name containing imageName.
	 * @param imageName see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImageByName(String imageName)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImageByName(imageName);
	}
	/**
	 * Save the object to the db . 
	 * @param obj see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void saveObject(IObject obj) 
							throws  DSOutOfServiceException, DSAccessException
	{
		dataService.saveObject(obj);
	}
	
	/**
	 * Save and return the Object.
	 * @param obj see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public IObject saveAndReturnObject(IObject obj) 
							throws  DSOutOfServiceException, DSAccessException
	{
		return dataService.saveAndReturnObject(obj);
	}
	/**
	 * Save the array.
	 * @param graph see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void saveArray(List<IObject> graph) 
							throws  DSOutOfServiceException, DSAccessException
	{
		dataService.saveArray(graph);
	}
	
	/**
	 * Save and return the array.
	 * @param <T> The Type to return.
	 * @param graph the object
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	 public <T extends omero.model.IObject>List<T> 
	 			saveAndReturnArray(List<IObject> graph)
	 						throws  DSOutOfServiceException, DSAccessException
	 {
		 return dataService.saveAndReturnArray(graph);
	 }
	 
	 /**
	  * Delete the object.
	  * @param row the object.(commonly a row in db)
	  * @throws DSOutOfServiceException
	  * @throws DSAccessException
	  */
	 public void deleteObject(IObject row) 
							throws  DSOutOfServiceException, DSAccessException
	{
		dataService.deleteObject(row);
	}
	 
	/**
	 * Keep service alive.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	
	public void keepAlive() throws DSOutOfServiceException, DSAccessException
	{
		gatewayFactory.keepAlive();
		dataService.keepAlive();
		imageService.keepAlive();
		fileService.keepAlive();
	}
	
	/**
	 * Get the username. 
	 * @return see above.
	 */
	public String getUsername()
	{
		return gatewayFactory.getUsername();
	}
	
	public Dataset getDataset(long datasetId, boolean leaves) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> datasetIdList = new ArrayList();
		datasetIdList.add(datasetId);
		List<Dataset> datasets = getDatasets(datasetIdList, leaves);
		if(datasets.size()==1)
			return datasets.get(0);
		return null;
	}
}


