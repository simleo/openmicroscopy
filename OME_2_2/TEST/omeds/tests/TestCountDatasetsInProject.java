/*
 * omeds.tests.TestCountDatasetsInProject
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
 
package omeds.tests;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omeds.DBFixture;
import omeds.LoadRowCommand;
import omeds.OMEDSTestCase;
import omeds.SQLCommand;
import omeds.dbrows.DatasetRow;
import omeds.dbrows.ExperimenterRow;
import omeds.dbrows.GroupRow;
import omeds.dbrows.ProjectDatasetMapRow;
import omeds.dbrows.ProjectRow;

import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Project;
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
public class TestCountDatasetsInProject
	extends OMEDSTestCase
{

	private GroupRow					groupRow;
	private ExperimenterRow				expRow;
	private ProjectRow					projectRow;
	private DatasetRow					datasetRow;
	private ProjectDatasetMapRow		pdmRow;
	private int							numberDataset;
	/* (non-Javadoc)
	 * @see omeds.OMEDSTestCase#prepareFixture(java.sql.Connection)
	 */
	protected DBFixture prepareFixture()
	{
		DBFixture dbFixture = new DBFixture();
		//Then create a experimenter.
		expRow = new ExperimenterRow("afalconi", "andrea","/lm/sync/ome_files",
									"falconi", "afalconi@toto.org", null);
		//Then a group.							
		groupRow = new GroupRow("ome 2", expRow);
		//create project & dataset.

		projectRow = new ProjectRow(groupRow, null, "project insert", expRow, 
									"lundi matin");
		datasetRow = new DatasetRow(false, groupRow," dataset insert", expRow,
									 "toujours le vieux discours. ");	
		pdmRow = new ProjectDatasetMapRow(projectRow, datasetRow);	

		SQLCommand lrc1, lrc2, lrc3, lrc4, lrc5, lrc6;
		lrc1 = new LoadRowCommand(expRow, dbFixture);
		lrc2 = new LoadRowCommand(groupRow, dbFixture);
		lrc3 = new SQLCommand(){
					public void execute()
						throws Exception
					{
						expRow.setGroupID(new Integer(groupRow.getID()));
						expRow.update();
					}
					//Do nothing b/c expRow and groupRow will be deleted.
					public void undo(){}
				};
		lrc4 = new LoadRowCommand(datasetRow, dbFixture);
		lrc5 = new LoadRowCommand(projectRow, dbFixture);
		lrc6 = new LoadRowCommand(pdmRow, dbFixture);

		dbFixture.enlist(lrc1);
		dbFixture.enlist(lrc2);
		dbFixture.enlist(lrc3);
		dbFixture.enlist(lrc4);
		dbFixture.enlist(lrc5);
		dbFixture.enlist(lrc6);
		numberDataset = 1;
		
		return dbFixture;
	}
	
	
	public void testCountDatasetsInProject()
	{
		Criteria c = new Criteria();
		c.addWantedField("#datasets");
		
		int projectID = projectRow.getID();
		Project p = (Project) omeds.load(Project.class, projectID, c);
		
		assertEquals(numberDataset, p.countDatasets());
	}
	
	public static void main(String[] args)
	{
		TestCountDatasetsInProject x = new TestCountDatasetsInProject();
		x.setUp();
		x.testCountDatasetsInProject();
		x.tearDown();
	}
	
}
