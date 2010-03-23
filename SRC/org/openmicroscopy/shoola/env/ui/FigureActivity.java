/*
 * org.openmicroscopy.shoola.env.ui.FigureActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.FigureActivityParam;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Activity to create a figure.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FigureActivity 
	extends ActivityComponent
{

	/** The description of the activity. */
	private static final String		DESCRIPTION_CREATION = "Creating a figure";
	
	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_CREATED = "Figure created";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = "Figure creation" +
			" cancelled";
	
	/** The parameters hosting information about the figure to make. */
    private FigureActivityParam	parameters;

    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param parameters  	The parameters used to create a figure.
     */
	public FigureActivity(UserNotifier viewer, Registry registry,
			FigureActivityParam	parameters)
	{
		super(viewer, registry, DESCRIPTION_CREATION, parameters.getIcon(), 
				ActivityComponent.ADVANCED);
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
	}

	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new FigureCreator(viewer,  registry, parameters.getParameters(), 
				parameters.getIds(), parameters.getObjectType(), this);
		return loader;
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		type.setText(DESCRIPTION_CREATED);
		Object p = parameters.getParameters();
		if (p instanceof FigureParam) {
			DataObject data = ((FigureParam) p).getAnchor();
			if (data == null) return;
			String name = null;
			if (data instanceof ImageData) {
				name = "image: "+((ImageData) data).getName();
			} else if (data instanceof DatasetData) {
				name = "dataset: "+((DatasetData) data).getName();
			} else if (data instanceof ProjectData) {
				name = "project: "+((ProjectData) data).getName();
			}
			if (name != null)
				messageLabel.setText("Attached to "+name);
		}
	}
	
	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityCancelled()
	 */
	protected void notifyActivityCancelled()
	{
		type.setText(DESCRIPTION_CANCEL);
	}
	
	/** Notifies to downnload the file. */
	protected void notifyDownload()
	{
		//Check name space.
		if (!(result instanceof FileAnnotationData)) {
			downloadButton.setEnabled(false);
			return;
		}
		final FileAnnotationData data = (FileAnnotationData) result;
		JFrame f = registry.getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, 
				true);
		IconManager icons = IconManager.getInstance(registry);
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setSelectedFileFull(data.getFileName());
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File folder = (File) evt.getNewValue();
					if (data == null) return;
					OriginalFile f = (OriginalFile) data.getContent();
					IconManager icons = IconManager.getInstance(registry);
					DownloadActivityParam activity = 
						new DownloadActivityParam(f,
							folder, icons.getIcon(IconManager.DOWNLOAD_22));
					activity.setLegend(data.getDescription());
					viewer.notifyActivity(activity);
				}
			}
		});
		chooser.centerDialog();
	}
	
}
