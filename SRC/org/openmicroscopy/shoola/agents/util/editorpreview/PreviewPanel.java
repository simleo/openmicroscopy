/*
 * org.openmicroscopy.shoola.agents.editor.browser.FieldPanelsComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.editorpreview;

//Java imports
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.editorpreview.MetadataComponent;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays an entire Editor file, using code based on code from
 * {@link org.openmicroscopy.shoola.agents.metadata.editor.ImageAcquisitionComponent}
 * 
 * This is simply the display panel, and does not include E.g the JXTaskPane 
 * Other classes may display this panel in a JXTaskPane, and use the 
 * {@link getTitle} method to set the JXTaskPane title. 
 * 
 * Steps are displayed as panels with labeled border, with their
 * parameters displayed as name-value pairs. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PreviewPanel 
	extends JPanel
{
	
	/** The property fired to open the file. */
	public static final String		OPEN_FILE_PROPERTY = "openFile";
	
	/** max number of characters to display in field value */
	public static final int			MAX_CHARS = 50;

	/** The element that holds the value of a parameter */
	public static final String 		VALUE = "v";

	/** The element that defines a parameter within a step */
	public static final String 		PARAMETER = "p";

	/** The attribute holding the 'level' of a single step in the hierarchy */
	public static final String 		LEVEL = "l";

	/** The element defining a single step */
	public static final String 		STEP = "s";

	/** The element holding the list of steps for the protocol */
	public static final String 		STEPS = "ss";

	/** The element holding the description/abstract of the protocol */
	public static final String 		DESCRIPTION = "d";

	/** The element/attribute holding the name of a parameter or protocol */
	public static final String 		NAME = "n";

	/** Default text if no data entered. */
	private static final String		DEFAULT_TEXT = "None";
	
	/**
	 * The Model passes the XML description. Then the steps and the name and
	 * description/abstract of the protocol can be retrieved. 
	 */
	private PreviewModel	model;
	
	/** The id of the file. */
	private long			fileID;

	/**
	 * Lays out the title and the a button to open the file.
	 * 
	 * @return See above.
	 */
	private JPanel layoutTiTle()
	{
		IconManager icons = IconManager.getInstance();
		JButton open = new JButton(icons.getIcon(IconManager.FILE_EDITOR));
		open.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(open);
		open.setBackground(UIUtilities.BACKGROUND_COLOR);
		open.setToolTipText("Open the file.");
		open.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				if (fileID >= 0)
					firePropertyChange(OPEN_FILE_PROPERTY, -1, fileID);
			}
		});
		JToolBar bar = new JToolBar();
    	bar.setBorder(null);
    	bar.setFloatable(false);
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.add(open);
    	
    	JPanel p = new JPanel();
    	double[][] size = {{TableLayout.PREFERRED, TableLayout.FILL}, 
    			{TableLayout.PREFERRED, TableLayout.FILL}};
    	p.setLayout(new TableLayout(size));
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	if (fileID > 0) p.add(bar, "0, 0, l, t");
    	p.add(UIUtilities.setTextFont(getTitle()), "1, 0, 1, 1");
    	
    	JPanel content = UIUtilities.buildComponentPanel(p, 0, 0);
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return content;
	}
	
	/** Initialises the components. */
	private void initComponents()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		String description = model.getDescription();
		if (description != null) {
			//TODO: externalize that
			description = "<html>" +
					"<span style='font-family:sans-serif;font-size:11pt'>"
							+ description + "</span></html>";
			// display description in EditorPane, because text wraps nicely!
			JEditorPane ep = new JEditorPane("text/html", description);
			ep.setEditable(false);
			ep.setBorder(new EmptyBorder(3, 5, 5, 3));
			p.add(ep);
		}
		
		List<StepObject> protocolSteps = model.getSteps();
		
		String stepName;
		JPanel nodePanel;
		Border border;
		int indent;
		List<MetadataComponent> paramComponents;
		for (StepObject stepObject : protocolSteps) {

			// each child node has a list of parameters 
			paramComponents = transformFieldParams(stepObject);
			
			// don't add a field unless it has some parameters to display
			if (paramComponents.isEmpty()) continue;
			
			stepName = stepObject.getName();
			
			indent = (stepObject.getLevel())*10;
			nodePanel = new JPanel();
			border = new EmptyBorder(0, indent, 0, 0);
			border = BorderFactory.createCompoundBorder(border, 
								BorderFactory.createTitledBorder(stepName));
			nodePanel.setBorder(border);
			nodePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
			nodePanel.setLayout(new GridBagLayout());
			
			layoutFields(nodePanel, null, paramComponents, true);
			
			p.add(nodePanel);
		}
		setLayout(new BorderLayout());
		add(layoutTiTle(), BorderLayout.NORTH);
		add(p, BorderLayout.CENTER);
	}
	
	/** Inovked when the model changes. */
	private void rebuildUI() 
	{	
		removeAll();
		initComponents();
		revalidate();
		repaint();
	}

	/** 
	 * Lays out the passed component.
	 * 
	 * @param pane 		The main component.
	 * @param button	The button to show or hide the unset fields.
	 * @param fields	The fields to lay out.
	 * @param shown		Pass <code>true</code> to show the unset fields,
	 * 					<code>false</code> to hide them.
	 */
	private void layoutFields(JPanel pane, JButton button, 
			List<MetadataComponent> fields, boolean shown)
	{
		pane.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
	    
		for (MetadataComponent comp : fields) {
	        c.gridx = 0;
	        if (comp.isSetField() || shown) {
	        	 ++c.gridy;
	        	 c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	             c.fill = GridBagConstraints.NONE;      //reset to default
	             c.weightx = 0.0;  
	             pane.add(comp.getLabel(), c);
	             c.gridx++;
	             pane.add(Box.createHorizontalStrut(5), c); 
	             c.gridx++;
	             //c.gridwidth = GridBagConstraints.REMAINDER;     //end row
	             //c.fill = GridBagConstraints.HORIZONTAL;
	             c.weightx = 1.0;
	             pane.add(comp.getArea(), c);  
	        } 
	    }
	    ++c.gridy;
	    c.gridx = 0;
	    c.weightx = 0.0;  
	    if (button != null) pane.add(button, c);
	}

	/**
	 * Transforms the Step into the corresponding UI objects.
	 * 
	 * @param step The step to transform.
	 * @return See above.
	 */
	private List<MetadataComponent> transformFieldParams(StepObject step)
	{
		List<MetadataComponent> cmps = new ArrayList<MetadataComponent>();
		MetadataComponent comp;
		JLabel label;
		JComponent area;
		String key;
		String value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		
		List<StepObject.Param> params = step.getParams();
		for (StepObject.Param param : params) {
			key = param.getName();
			value = param.getValue();
			if ((value != null) && (value.length() > MAX_CHARS)) {
				value = value.substring(0, MAX_CHARS-1) + "...";
			}
			
			area = UIUtilities.createComponent(OMETextArea.class, 
					null);
			if (value == null || value.equals(""))
				value = DEFAULT_TEXT;
			((OMETextArea) area).setText(value);
			((OMETextArea) area).setEditedColor(
					UIUtilities.EDITED_COLOR);
			
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);
			
			comp = new MetadataComponent(label, area);
			comp.setSetField(value != null);
			cmps.add(comp);
		}
		
		return cmps;
	}
	
	/**
	 * Creates a new instance and sets the content with the XML summary. 
	 * 
	 * @param xmlDescription A preview summary in XML. 
	 * @param fileID The id of the file.
	 */
	public PreviewPanel(String xmlDescription, long fileID)
	{
		this.fileID = fileID;
		setDescriptionXml(xmlDescription);
	}
	
	/**
	 * Creates an instance without setting the content. 
	 * Use {@link #setDescriptionXml(String)} to set the content with XML
	 */
	public PreviewPanel()
	{
		fileID = -1;
	}
	
	/**
	 * Sets the XML description (XML that summarises an OMERO.editor file),
	 * as retrieved from the description of a File-Annotation for an Editor file. 
	 * 
	 * @param xmlDescription
	 */
	public void setDescriptionXml(String xmlDescription) 
	{
		model = new PreviewModel(xmlDescription);
		rebuildUI();
	}
	
	/**
	 * Allows classes that want to display this panel to get access to the 
	 * protocol title, which is not displayed in this panel. 
	 * 
	 * @return		the protocol title. 
	 */
	public String getTitle()
	{
		if (model == null) return "OMERO.editor";
		return model.getTitle();
	}
	
}
