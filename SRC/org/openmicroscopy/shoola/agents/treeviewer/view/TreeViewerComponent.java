/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerComponent
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

package org.openmicroscopy.shoola.agents.treeviewer.view;



//Java imports
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.editors.DOEditor;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;

/** 
 * Implements the {@link TreeViewer} interface to provide the functionality
 * required of the tree viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
 * @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
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
class TreeViewerComponent
    extends AbstractComponent
    implements TreeViewer
{
    
    /** The Model sub-component. */
    private TreeViewerModel     model;
    
    /** The Controller sub-component. */
    private TreeViewerControl   controller;
    
    /** The View sub-component. */
    private TreeViewerWin       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    TreeViewerComponent(TreeViewerModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new TreeViewerControl(this);
        view = new TreeViewerWin();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        controller.initialize(view);
        view.initialize(controller, model);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#activate()
     */
    public void activate()
    {
        switch (model.getState()) {
	        case NEW:
	            model.fireUserDetailsLoading();
                fireStateChange();
	            break;
	        case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
        } 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getBrowsers()
     */
    public Map getBrowsers() { return model.getBrowsers(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#discard()
     */
    public void discard()
    {
        Map browsers = getBrowsers();
        Iterator i = browsers.values().iterator();
        while (i.hasNext())
            ((Browser) i.next()).discard();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getSelectedBrowser()
     */
    public Browser getSelectedBrowser() { return model.getSelectedBrowser(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setSelectedBrowser(Browser)
     */
    public void setSelectedBrowser(Browser browser)
    {
        //check state
        model.setSelectedBrowser(browser);
        removeEditor();
    }
    
    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#addBrowser(int)
     */
    public void addBrowser(int browserType)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(browserType));
        if (browser != null) {
            model.setSelectedBrowser(browser);
            view.addBrowser(browser);
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#showProperties(DataObject, int)
     */
    public void showProperties(DataObject object, int editorType)
    {
        if (editorType == EDIT_PROPERTIES || editorType == CREATE_PROPERTIES)
            model.setEditorType(editorType);
        else return;
        //model.setDataObject(object);
        DOEditor panel = new DOEditor(this, object, editorType);
        panel.addPropertyChangeListener(DOEditor.CANCEL_CREATION_PROPERTY, 
                                        controller);;
        view.addComponent(panel); 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#saveObject(DataObject)
     */
    public void saveObject(DataObject object)
    {
        //check state and editor type.
        Browser browser = model.getSelectedBrowser();
        if (browser == null) return;
        Object userObject = browser.getSelectedDisplay().getUserObject();
        model.fireDataObjectEdition(object, userObject);
        LoadingWindow window = view.getLoadingWindow();
        window.setTitleAndText(LoadingWindow.SAVING_TITLE,
                                LoadingWindow.SAVING_MSG);
        UIUtilities.centerAndShow(window);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#cancel()
     */
    public void cancel()
    {
        //TODO: check state.
        //Remove loading window.
        model.cancel();
        if (view.getLoadingWindow().isVisible())
            view.getLoadingWindow().setVisible(false);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#cancel()
     */
    public void removeEditor()
    {
        //TODO: check state 
        model.setEditorType(NO_EDITOR);
        view.removeAllFromRightPane();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setSaveResult(DataObject)
     */
    public void setSaveResult(DataObject object)
    {
        if (model.getState() != READY)
            throw new IllegalStateException(
                    "This method can only be invoked in the READY state.");
        Browser browser = model.getSelectedBrowser();
        if (browser != null) {
            switch (model.getEditorType()) {
                case TreeViewer.CREATE_PROPERTIES:
                    browser.setCreatedNode(
                            TreeViewerTranslator.transformDataObject(object));
                    break;
                case TreeViewer.EDIT_PROPERTIES:
                    break;
            }
        }  
        if (view.getLoadingWindow().isVisible())
            view.getLoadingWindow().setVisible(false);
        view.removeAllFromRightPane();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#setUserDetails(UserDetails)
     */
    public void setUserDetails(UserDetails details)
    {
        if (model.getState() != LOADING_DETAILS)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DETAILS " +
                    "state.");
        if (details == null)
            throw new IllegalArgumentException("details shouldn't be null.");
        model.setUserDetails(details);
        firePropertyChange(DETAILS_LOADED_PROPERTY, null, details);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getUserDetails()
     */
    public UserDetails getUserDetails()
    {
        if (model.getState() != READY)
            throw new IllegalStateException(
                    "This method can only be invoked in the READY state.");
        return model.getUserDetails();
    }
    
}
