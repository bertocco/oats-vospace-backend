/**_____________________________________________________________________________
 *
 *                                 OATS - INAF
 *  Osservatorio Astronomico di Tireste - Istituto Nazionale di Astrofisica
 *  Astronomical Observatory of Trieste - National Institute for Astrophysics
 * ____________________________________________________________________________
 *
 * Copyright (C) 20016  Istituto Nazionale di Astrofisica
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * _____________________________________________________________________________
 **/
package it.inaf.oats.vospacebackend.implementation;

import ca.nrc.cadc.net.TransientException;
import ca.nrc.cadc.util.FileMetadata;
import ca.nrc.cadc.vos.DataNode;
import ca.nrc.cadc.vos.Node;
import ca.nrc.cadc.vos.VOS;
import ca.nrc.cadc.vos.VOSURI;
import ca.nrc.cadc.vos.server.NodeID;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.utils.ConfigReader;
import it.inaf.oats.vospacebackend.utils.NodeUtils;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author bertocco
 */
public abstract class VOSpaceBackend implements VOSpaceBackendInterface {
//public abstract class VOSpaceBackend {
    

    private static final Logger log = Logger.getLogger(VOSpaceBackend.class);
    
    private static final String CONFIG_FILE_NAME = "VOSpace.properties";      
    private static final String DEFAULT_TMP_STOARGE_ROOT = "/tmp/vospace";
    protected static String tmpStorageRoot;     
    
    public VOSpaceBackend() {
    
        try {
            
            ConfigReader myConf = new ConfigReader(CONFIG_FILE_NAME);
        
            this.tmpStorageRoot = myConf.getProperty("fs.posix.tmp.storage.root"); 
            log.debug("tmpStorageRoot : " + this.tmpStorageRoot); 
            
        } catch (Exception e) {
            log.debug("Unable to read properties file: " + CONFIG_FILE_NAME);
            log.debug("Use defaults values : ");
            log.debug("this.DEFAULT_TMP_STOARGE_ROOT");
            this.tmpStorageRoot = this.DEFAULT_TMP_STOARGE_ROOT;
                        
        }
        log.debug("Swift VOSpace Backend keystone_auth_url = " + this.tmpStorageRoot); 
    }
    
    /**
     * Stores the uploaded file. 
     * Steps:
     * <ul>
     *  <li>
     *     Creates VOSpaceBackendMetadata to manage back-end meta-data
     *  </li>
     *  <li>
     *     Creates a DatabaseNodePersistence to manage front-end meta-data
     *  </li>
     *  <li>
     *     Gets the Node (from the vosuri) 
     *  </li>
     *  <li>
     *     Locks the Node
     *  </li>
     *  <li>
     *     Sets the back-end meta-data (relation Node-StoredFileID)
     *  </li>
     *  <li>
     *     Stores the uploaded file in the back-end
     *  </li>
     *  <li>
     *     Prepares the Node front-end meta-data (md5_checksum and node length/size)
     *  </li>
     *  <li>
     *     Sets the Node front-end meta-data. The set operation contains the free
     *     of the Node (sets NodeBusy=false).
     *  </li>
     * </ul>
     * @param vosuri
     * @param storedFileID
     * @param md5_sum
     * @param fileLength is file size in bytes.
     * double bytes = file.length();
     * double kilobytes = (bytes / 1024);
     * double megabytes = (kilobytes / 1024);
     * double gigabytes = (megabytes / 1024);
     * double terabytes = (gigabytes / 1024);
     * double petabytes = (terabytes / 1024);
     * double exabytes = (petabytes / 1024);
     * double zettabytes = (exabytes / 1024);
     * double yottabytes = (zettabytes / 1024);
     * @return 
     * <ul>
     *  <li>
     * true if operation successful
     * </li>
     *  <li>
     * false if operation failure 
     *  </li>
     * </ul>
     * @throws VOSpaceBackendException
     * @throws SQLException
     * @throws IOException 
     */
    public boolean createFile(String vosuri, String storedFileID, String md5_sum, Long fileLength) 
                           throws VOSpaceBackendException, SQLException, IOException {
        
        log.debug("Entering in vospacebackend.createfile");
        
        // Creates VOSpaceBackendMetadata to manage back-end meta-data
        VOSpaceBackendMetadata metadata = new VOSpaceBackendMetadata();
        
        // Creates a DatabaseNodePersistence to manage front-end meta-data
        DatabaseNodePersistenceImpl dbNodePers = new DatabaseNodePersistenceImpl();
        
        // Gets the Node (from the vosuri)
        Node myNode = null;
        try {
            myNode = dbNodePers.get(new VOSURI(vosuri));
            log.debug("SBE: Checkpint 3 -> after dbNodePers.get(new VOSURI(vosuri)); vosuri = #" + vosuri + "#");
        } catch (Exception e) {
            log.debug("Exception getting node from persistence.");
            log.debug("Exception message : " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            log.debug(exceptionAsString);
        }
        NodeID nodeID = (NodeID)myNode.appData;
        log.debug("SBE: Checkpint 4 -> nodeID = " + nodeID.toString());
        Long myNodeID = nodeID.getID();
        log.debug("SBE: Checkpint 5  -> nodeID.getID() = " + myNodeID);

        if (!(myNode instanceof DataNode)) {
            log.debug("Node instance NOT found");
            throw new VOSpaceBackendException("Node instance NOT found");
        }
        DataNode myDataNode = (DataNode)myNode;
        
        // Needs syncronisation BEGIN
        boolean result = false;
        try {
            synchronized (this) {
             while ((myDataNode.getBusy() != null) && myDataNode.getBusy().getValue() == "W")
                wait();
             // Locks the Node
             try {     
                  dbNodePers.setBusyState(myDataNode, VOS.NodeBusyState.notBusy, VOS.NodeBusyState.busyWithWrite);
                  log.debug("SBE: Checkpint 7 -> after  dbNodePers.setBusyState");
                } catch (Exception ex) {
                    log.debug("Exception in dbNodePers.setBusyState");
                    throw new VOSpaceBackendException("TransientException in dbNodePers.setBusyState");
                }
            
                // Set backend metadata
                log.debug("SBE: Checkpint 8 -> storedFileID = " + storedFileID + " --- myNodeID.toString() = " + myNodeID.toString());
                FileRecord putReqData = metadata.setRequest(storedFileID, myNodeID.toString());
                log.debug("SBE: Checkpint 9  -> after metadata.setRequest(storedFileID, myNodeID.toString())");
                result = putReqData.getIfOperationSuccessful();
                log.debug("SBE: Checkpint 9A -> myresult = " + result);
                if(!result) {
                    log.debug("Fails updating table NodeStoredFileAndNode");
                    FileMetadata fakeNodeMetadata = new FileMetadata();
                    fakeNodeMetadata.setMd5Sum(md5_sum);
                    try {
                       dbNodePers.setFileMetadata(myDataNode, fakeNodeMetadata, false);
                    } catch(TransientException e) {
                        throw new VOSpaceBackendException("Unable to update node data and free node");
                    }
                    throw new VOSpaceBackendException("Node instance NOT found");
                }   
                log.debug("SBE: Checkpint 10  -> operation successful");
                log.debug("SBE: Checkpint 10  -> storedFileID = " + storedFileID);

                // Prepare front-end metadata 
                FileMetadata nodeMetadata = new FileMetadata();
                nodeMetadata.setMd5Sum(md5_sum);
 
                log.debug("File size: = " + fileLength);
                nodeMetadata.setContentLength(fileLength);

                log.debug("File metadata successfully saved. I'm going to store the file content");
                fileFromTmpToFinalStorageArea(storedFileID, md5_sum);             
                // Store the front-end meta-data 
                // This operation free the node: Set not busy
                log.debug("SBE: Checkpint 12 ->  After nodeMetadata.setMd5Sum(md5_sum)");
      
                try {
                    log.debug("SBE: Checkpint 13 ->  myDataNode = " + myDataNode + " --- nodeMetadata = " + nodeMetadata);
                    dbNodePers.setFileMetadata(myDataNode, nodeMetadata, false);
                    log.debug("SBE: Checkpint 13 ->  After dbNodePers.setFileMetadata(myDataNode, nodeMetadata, false)");
                } catch (Exception ex) {
                    log.debug("Exception doing databasePersistence.setFileMetadata.");
                    throw new VOSpaceBackendException("Node instance NOT found");
                }
                result = true;
                log.debug("SBE: Checkpint 14");
                notifyAll();
                log.debug("SBE: Checkpint 15");
            } 
            
        } catch (InterruptedException e) {
            log.debug("SBE: Checkpint 16: catch executed");
            log.debug("Exception (A) in dbNodePers.setBusyState(myDataNode, VOS.NodeBusyState.busyWithWrite, VOS.NodeBusyState.notBusy)");
        } finally {
            log.debug("SBE: Checkpint 17: finally executed");
            if ((myDataNode.getBusy() != null) && myDataNode.getBusy().getValue() == "W") {
                try {               
                  dbNodePers.setBusyState(myDataNode, VOS.NodeBusyState.busyWithWrite, VOS.NodeBusyState.notBusy);
                } catch (Exception ex) {
                    log.debug("Exception (B) in setBusyState(myDataNode, VOS.NodeBusyState.busyWithWrite, VOS.NodeBusyState.notBusy)");
                    throw new VOSpaceBackendException("Unrecoverable error managing the Node");
                }
            }
        }
        // Needs syncronisation END
        
        return result;
                   
    }
    
    
    /* Retrieve the file: the backends copy it in the temporary location, 
    return the File */
    public File returnFile(String vosuri) 
               throws VOSpaceBackendException, SQLException, IOException {
    
        // Gets (from frontend metadata) NodeID from vosuri 
        NodeUtils nodeUtil = new NodeUtils();
        log.debug("SB: Going to get NodeIdLongfromVosuriStr");
        Long myNodeID = nodeUtil.getNodeIdLongfromVosuriStr(vosuri);
        if (myNodeID == null) {
            log.debug("Problem encountered reading backend node metadata");
            return null;
        }
         
        log.debug("myNodeID = " + myNodeID.toString());
        
        // Gets storageFileID from NodeID
        VOSpaceBackendMetadata metadataDB = new VOSpaceBackendMetadata();
        
        FileRecord backendMetadata = null;
        try {
            backendMetadata = metadataDB.getRequestByNode(myNodeID.toString());
        } catch (SQLException ex) {
            
        }
        if (backendMetadata == null | !backendMetadata.getIfOperationSuccessful()) {
            log.debug("Backend metadata not found");
            return null;
        }
        
        log.debug("SB: Received backendMetadata record = " + backendMetadata.toString());
        
        Node myNode = nodeUtil.getNodeFromVosuriStr(vosuri);
        DataNode myDataNode = (DataNode)myNode;
        
        log.debug("SB: Retrieved node given metadata = " + myNode.toString());
        String md5_sum = myNode.getPropertyValue(VOS.PROPERTY_URI_CONTENTMD5); 
        

        String storedFileName = backendMetadata.getStoredfileName(); 
        log.debug("SB: Stored file name in backend metadata = " + storedFileName);
        
        // Needs syncronisation BEGIN
        // Get operation must be synchronized because no-one can modify the file 
        // until reading to not have inconsistencies       
        String outFileName = "";
        try {
            synchronized (this) {
             while ((myDataNode.getBusy() != null) && myDataNode.getBusy().getValue() == "W")
                wait();
             // Locks the Node
             try {     
                  nodeUtil.dbNodePers.setBusyState(myDataNode, VOS.NodeBusyState.notBusy, VOS.NodeBusyState.busyWithWrite);
                  log.debug("SBE: returnFile -> after  dbNodePers.setBusyState");
                } catch (Exception ex) {
                    log.debug("Exception in dbNodePers.setBusyState");
                    throw new VOSpaceBackendException("TransientException in dbNodePers.setBusyState");
                }
             
                outFileName = this.fileFromStorageAreaToTmp(md5_sum, storedFileName);
             
                // Prepare front-end metadata 
                FileMetadata nodeMetadata = new FileMetadata();
                nodeMetadata.setMd5Sum(md5_sum);
                try {
                    log.debug("SBE: returnFile -> myDataNode = " + myDataNode + " --- nodeMetadata = " + nodeMetadata);
                    nodeUtil.dbNodePers.setFileMetadata(myDataNode, nodeMetadata, false);
                    log.debug("SBE: returnFile -> After dbNodePers.setFileMetadata(myDataNode, nodeMetadata, false)");
                } catch (Exception ex) {
                    log.debug("Exception doing databasePersistence.setFileMetadata.");
                    throw new VOSpaceBackendException("Node instance NOT found");
                }
            }
        } catch (InterruptedException e) {
            log.debug("SBE: returnFile -> catch executed");
            log.debug("returnFile -> Exception (A) in dbNodePers.setBusyState(myDataNode, VOS.NodeBusyState.busyWithWrite, VOS.NodeBusyState.notBusy)");
        } finally {
            log.debug("SBE: returnFile -> finally executed");
            if ((myDataNode.getBusy() != null) && myDataNode.getBusy().getValue() == "W") {
                try {               
                  nodeUtil.dbNodePers.setBusyState(myDataNode, VOS.NodeBusyState.busyWithWrite, VOS.NodeBusyState.notBusy);
                } catch (Exception ex) {
                    log.debug("returnFile -> Exception (B) in setBusyState(myDataNode, VOS.NodeBusyState.busyWithWrite, VOS.NodeBusyState.notBusy)");
                    throw new VOSpaceBackendException("Unrecoverable error managing the Node");
                }
            }
        }
        // Needs syncronisation END
        
        File outFile = null;
        outFile = new File(outFileName);
        
        return outFile;
    
    }
    
    
    public String createRelativePathFromMd5checksum(String initialStr) throws VOSpaceBackendException{
        
        log.debug("initialStr = " + initialStr);
        log.debug("initialStr.substring(initialStr.length()-2, initialStr.length())" + initialStr.substring(initialStr.length()-2));
        log.debug("initialStr.length()-4, initialStr.length()-2)" + initialStr.substring(initialStr.length()-4, initialStr.length()-2)); 
        
        String relativePath = null;
        try{
            relativePath = new String(File.separator + 
                              initialStr.substring(initialStr.length()-4, initialStr.length()-2) +                              
                              File.separator +
                              initialStr.substring(initialStr.length()-2, initialStr.length()));
        } catch (Exception e) {
            log.debug("Exception creating partial path from string " + initialStr);
            throw new VOSpaceBackendException(e);
        }
        log.debug("relative path = " + relativePath);
        
        return relativePath;
        
    }
    
    
    protected String getTmpPath() {
        
        String tmpFilePath = this.tmpStorageRoot + File.separator;
        return tmpFilePath;
        
    }

   // The md5 checksum is needed only for posix backend, but it is passed here 
    // as parameter to avoid to re-read the file (to calculate the checksum) or
    // to set it previously in the DB. The metadata set operation frees the Node 
    // so a new setBusyState will be needed
    //public abstract void fileFromTmpToFinalStorageArea(String storedFileID, String md5_sum) throws VOSpaceBackendException ;
    
    /**
     * 
     * @param vosuri
     * @param storedFileID
     * @return the complete file name (path+name) of the temporary location where 
     * the file is available)
     * @throws VOSpaceBackendException 
     */
    //public abstract String fileFromStorageAreaToTmp(String vosuri, String storedFileID) throws VOSpaceBackendException ;
}
