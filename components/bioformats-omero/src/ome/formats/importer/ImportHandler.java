/*
 * ome.formats.testclient.ImportHandler
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import loci.formats.FormatException;
import ome.api.IRepositoryInfo;
import ome.formats.OMEROMetadataStore;
import ome.model.core.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Importer is master file format importer for all supported formats and imports
 * the files to an OMERO database
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @basedOnCodeFrom Curtis Rueden ctrueden at wisc.edu
 */
public class ImportHandler 
{

    private ImportLibrary   library;
    private IRepositoryInfo iInfo;
    private OMEROWrapper    reader;

    private Main      viewer;
    private static boolean   runState = false;
    private Thread runThread;
    
    //private ProgressMonitor monitor;
    
    private FileQueueTable  qTable;

    @SuppressWarnings("unused")
    private static Log      log = LogFactory.getLog(ImportHandler.class);
    
    private OMEROMetadataStore store;

    public ImportHandler(Main viewer, FileQueueTable qTable, OMEROMetadataStore store,
            OMEROWrapper reader, ImportContainer[] fads)
    {
        if (runState == true)
        {
            log.error("ImportHandler running twice");
            if (runThread != null) log.error(runThread);
            throw new RuntimeException("ImportHandler running twice");
        }
        runState = true;
        try {
            this.viewer = viewer;
            this.store = store;
            this.qTable = qTable;
            this.reader = reader;
            this.library = new ImportLibrary(store, reader, fads);
            
            this.iInfo = store.getRepositoryInfo();
           
            runThread = new Thread()
            {
                public void run()
                {
                    try
                    {
                        importImages();
                    }
                    catch (Exception e)
                    {
                        new DebugMessenger(null, "Error Dialog", true, e);
                    }
                }
            };
            runThread.start();
        }
        finally {
            runState = false;
        }
}

    /**
     * Begin the import process, importing first the meta data, and then each
     * plane of the binary image data.
     */
    private void importImages()
    {
        long timestampIn;
        long timestampOut;
        long timestampDiff;
        long timeInSeconds;
        long hours, minutes, seconds;
        Date date = null;

        // record initial timestamp and record total running time for the import
        timestampIn = System.currentTimeMillis();
        date = new Date(timestampIn);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String myDate = formatter.format(date);

        viewer.appendToOutputLn("> Starting import at: " + myDate + "\n");
        viewer.statusBar.setStatusIcon("gfx/import_icon_16.png", "Now importing.");

        viewer.statusBar.setProgressMaximum(library.getFilesAndDatasets().length);
        
        ImportContainer[] fads = library.getFilesAndDatasets();
        qTable.importBtn.setText("Cancel");
        qTable.importing = true;
        
        for(int i = 0; i < fads.length; i++)
        {
           	qTable.setProgressPending(i);
        }
        
        for (int j = 0; j < fads.length; j++)
        {
            if (qTable.table.getValueAt(j, 2).equals("pending") 
                    && qTable.cancel == false)
            {
                String filename = fads[j].file.getAbsolutePath();
                
                viewer.appendToOutputLn("> [" + j + "] Importing \"" + filename
                        + "\"");
                
                library.setDataset(fads[j].dataset);
                
                try
                {
                	importImage(fads[j].file, j,
                			    library.getFilesAndDatasets().length,
                			    fads[j].imageName,
                			    fads[j].archive);
                }
                catch (Exception e)
                {
                	qTable.setProgressFailed(j);
                    viewer.appendToOutputLn("> [" + j + "] Failure importing.");
                    new DebugMessenger(null, "Error Dialog", true, e);
                }
            }
        }
        qTable.importBtn.setText("Import"); 
        qTable.importBtn.setEnabled(true);
        qTable.queue.setRowSelectionAllowed(true);
        qTable.removeBtn.setEnabled(true);
        if (qTable.failedFiles == true) 
            qTable.clearFailedBtn.setEnabled(true);
        if (qTable.doneFiles == true) 
            qTable.clearDoneBtn.setEnabled(true);
        qTable.importing = false;
        qTable.cancel = false;
        
        viewer.statusBar.setProgress(false, 0, "");
        //monitor.close();
        viewer.statusBar.setStatusIcon("gfx/import_done_16.png", "Import complete.");

        
        timestampOut = System.currentTimeMillis();
        timestampDiff = timestampOut - timestampIn;

        // calculate hour/min/sec time for the run
        timeInSeconds = timestampDiff / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;

        viewer.appendToOutputLn("> Total import time: " + hours + " hour(s), "
                + minutes + " minute(s), " + seconds + " second(s).");

        viewer.appendToOutputLn("> Image import completed!");
    }

    /**
     * @param file
     * @param index
     * @param total Import the actual image planes
     * @param b 
	 * @throws FormatException if there is an error parsing metadata.
	 * @throws IOException if there is an error reading the file.
     */
    private List<Pixels> importImage(File file, int index, int total, String imageName, 
            boolean archive)
    	throws FormatException, IOException
    {        
        String fileName = file.getAbsolutePath();
        String shortName = file.getName();

        viewer.appendToOutput("> [" + index + "] Loading image \"" + shortName
                + "\"...");
        open(file.getAbsolutePath());
        
        viewer.appendToOutput(" Succesfully loaded.\n");

        viewer.statusBar.setProgress(true, 0, "Importing file " + 
                (index +1) + " of " + total);
        viewer.statusBar.setProgressValue(index);

        viewer.appendToOutput("> [" + index + "] Importing metadata for "
                + "image \"" + shortName + "\"... ");

        qTable.setProgressPrepping(index);

        String[] fileNameList = reader.getUsedFiles();
        File[] files = new File[fileNameList.length];
        for (int i = 0; i < fileNameList.length; i++) 
        {
            files[i] = new File(fileNameList[i]); 
        }
        if (archive == true)
        {
            store.setOriginalFiles(files); 
        }
        reader.getUsedFiles();
        List<Pixels> pixList = library.importMetadata(imageName);

        int seriesCount = reader.getSeriesCount();
        
//        if (seriesCount > 1)
//        {
//            System.err.println("Series Count: " + reader.getSeriesCount());
//            throw new RuntimeException("More then one image in series");
//        }
        
        for (int series = 0; series < seriesCount; series++)
        {
            int count = library.calculateImageCount(fileName, series);
            Long pixId = pixList.get(series).getId(); 

            viewer.appendToOutputLn("Successfully stored to dataset \""
                    + library.getDataset() + "\" with id \"" + pixId + "\".");
            viewer.appendToOutputLn("> [" + index + "] Importing pixel data for "
                    + "image \"" + shortName + "\"... ");

            qTable.setProgressInfo(index, count);
            
            //viewer.appendToOutput("> Importing plane: ");
            library.importData(pixId, fileName, series, new ImportLibrary.Step()
            {

                @Override
                public void step(int i)
                {
                    if (i < qTable.getMaximum()) 
                    {   
                        qTable.setImportProgress(i);
                    }
                }
            });
            
            viewer.appendToOutputLn("> Successfully stored with pixels id \""
                    + pixId + "\".");
            viewer.appendToOutputLn("> [" + index
                    + "] Image imported successfully!");

            if (archive == true)
            {
                qTable.setProgressArchiving(index);
                for (int i = 0; i < fileNameList.length; i++) 
                {
                    files[i] = new File(fileNameList[i]);
                    store.writeFilesToFileStore(files, pixId);   
                }
            }
}
        
        qTable.setProgressDone(index);
        
//        System.err.println(iInfo.getFreeSpaceInKilobytes());
        
        return pixList;
        
    }

    /** Opens the given file using the ImageReader. */
    public void open(String fileName)
    {
        try
        {
            library.open(fileName);
        } catch (Exception exc)
        {
            exc.printStackTrace();
            viewer.appendToDebugLn(exc.toString());
            return;
        }
    }
}
