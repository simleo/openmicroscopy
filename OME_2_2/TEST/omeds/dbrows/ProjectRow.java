/*
 * omeds.dbrows.Project
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

package omeds.dbrows;

//Java imports
import java.sql.PreparedStatement;

//Third-party libraries

//Application-internal dependencies
import omeds.DBManager;
import omeds.DBRow;

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
public class ProjectRow
	extends DBRow
{

	private static String	INSERT_STM, UPDATE_STM;

	static {
		//INSERT_STM
		StringBuffer    buf = new StringBuffer();
		buf.append("INSERT INTO projects ");
		buf.append("(project_id, group_id, view, name, owner_id, description)");
		buf.append(" VALUES (?, ?, ?, ?, ?, ?)");
		INSERT_STM = buf.toString();
		
		//UPDATE_STM
		buf = new StringBuffer();
		buf.append("UPDATE projects ");
	  	buf.append("SET group_id = ?, view = ?, name = ?, owner_id = ?, ");
	  	buf.append("description = ? ");
	  	buf.append("WHERE project_id = ?");
	  	UPDATE_STM = buf.toString();
	}
	
	
	private String			name;
	private String			description;
	private String			view;
	private ExperimenterRow	expRow;
	private GroupRow		groupRow;
	
	public ProjectRow(GroupRow groupRow, String view, String name, 
						ExperimenterRow expRow, String description)
	{
		this.groupRow = groupRow;
		this.view = view;
		this.name = name;
		this.expRow = expRow;
		this.description = description;	
	}
	
	/* (non-Javadoc)
	 * @see omeds.DBRow#getTableName()
	 */
	public String getTableName()
	{
		return "projects";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#getIDColumnName()
	 */
	public String getIDColumnName()
	{
		return "project_id";
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#fillFromDB(java.sql.Connection, int)
	 */
	public void fillFromDB(int id)
		throws Exception
	{
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#insert(java.sql.Connection)
	 */
	public void insert()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(INSERT_STM);
		ps.setInt(1, getID());	
		ps.setInt(2, groupRow.getID());
		ps.setString(3, view);
		ps.setString(4, name);
		ps.setInt(5, expRow.getID());
		ps.setString(6, description);
		ps.execute();
		ps.close();
	}

	/* (non-Javadoc)
	 * @see omeds.DBRow#update(java.sql.Connection)
	 */
	public void update()
		throws Exception
	{
		DBManager dbm = DBManager.getInstance();
		PreparedStatement ps = dbm.getPreparedStatement(UPDATE_STM);
		ps.setInt(1, groupRow.getID());
		ps.setString(2, view);
		ps.setString(3, name);
		ps.setInt(4, expRow.getID());
		ps.setString(5, description);
		ps.setInt(6, getID());
		ps.execute();
		ps.close();
	}

	public String getDescription()
	{
		return description;
	}

	public GroupRow getGroupRow()
	{
		return groupRow;
	}

	public String getName()
	{
		return name;
	}

	public ExperimenterRow getExperimenterRow()
	{
		return expRow;
	}

	public String getView()
	{
		return view;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setGroupRow(GroupRow groupRow)
	{
		this.groupRow = groupRow;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setExperimenterRow(ExperimenterRow expRow)
	{
		this.expRow = expRow;
	}

	public void setView(String view)
	{
		this.view = view;
	}

}
