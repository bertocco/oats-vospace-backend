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

import it.inaf.oats.vospacebackend.utils.NodeUtils;
import ca.nrc.cadc.util.FileMetadata;
import ca.nrc.cadc.vos.DataNode;
import it.inaf.oats.vospacebackend.exceptions.ExceptionMessage;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.utils.ConfigReader;

import ca.nrc.cadc.vos.Node;
import ca.nrc.cadc.vos.server.NodeID;
import ca.nrc.cadc.vos.VOS;
import ca.nrc.cadc.vos.VOSURI;

import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @/author bertocco
 */
public class VOSpaceBackendPosix extends VOSpaceBackend {
    
    private static String documentRoot;
    private static String tmpStorageRoot;
    private static final String CONFIG_FILE_NAME = "VOSpace.properties";
    private static final Logger log = Logger.getLogger(VOSpaceBackendPosix.class);
    
    public VOSpaceBackendPosix() throws VOSpaceBackendException {
        
        try {
            ConfigReader myConf = new ConfigReader(CONFIG_FILE_NAME);
        
            this.documentRoot = myConf.getProperty("fs.posix.document.root");     
            this.tmpStorageRoot = myConf.getProperty("fs.posix.tmp.storage.root"); 
        } catch (Exception e) {
            ExceptionMessage exMsg = new ExceptionMessage();
            log.debug(MessageFormat.format(exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), CONFIG_FILE_NAME));
            throw new VOSpaceBackendException(                   
               MessageFormat.format(exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), CONFIG_FILE_NAME));
            
        }
        log.debug("VOSpace Backend Document Root = " + this.documentRoot);
        log.debug("VOSpace Backend Temporary Document Root = " + this.tmpStorageRoot);
        
    }
    
    protected String createRelativePathFromMd5checksum(String initialStr) throws VOSpaceBackendException{
        
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
    
    protected void fileFromTmpToFinalStorageArea(String tmpfile, String relPath)
                                 throws VOSpaceBackendException {
    
        
        File tmpFile = new File(this.getTmpPath() + tmpfile);  
        File finalStoredFile = new File(this.getStoragePath(relPath) + tmpfile);  
        
        log.debug("tmpStoredFile is: " + tmpFile);
        log.debug("finalStoredFile is: " + finalStoredFile);
        
        this.operateOnFiles(tmpFile, finalStoredFile, "MOVE");       
        //this.operateOnFiles(tmpFile, finalStoredFile, "COPY");
               
    }
    
    protected String fileFromStorageAreaToTmp(String md5_sum, String storedFileName)
                                 throws VOSpaceBackendException {        
               
        
        File storedFile = new File(this.getStoragePath(md5_sum) + storedFileName);
        File tmpFile = new File(this.getTmpPath() + storedFileName);
         
        log.debug("storedFile is: " + storedFile);
        log.debug("tmpFile is: " + tmpFile);

        this.operateOnFiles(storedFile, tmpFile, "COPY");
        
        return tmpFile.getAbsolutePath();

 
    }
    
           
    protected void operateOnFiles (File A, File B, String operation) throws VOSpaceBackendException {
                    
        log.debug("File A is: " + A);
        log.debug("File B is: " + B);
        log.debug("Operation required is " + operation);

        switch (operation) {
            case "MOVE":  
                this.moveFileAToFileB(A, B);
                break;
            case "COPY":  
                this.copyFileAToFileB(A, B);
                break;
            default: 
                log.debug("Error in operation required");
                throw new VOSpaceBackendException("Error in operation required");
        }
        
    }
        
        
    protected boolean checksBeforeCopyOrMove(File A, File B)
                                            throws VOSpaceBackendException {
        
        boolean checkOK = false;
    
        if (!A.exists()) {
            log.debug("Move operation impossible: source file" + A.getAbsolutePath() 
                                                          + "does not exists.");
            throw new VOSpaceBackendException("Operation impossible: source file" 
                                              + A.getAbsolutePath() + "does not exists.");             
        }       
        
        String absolutePathB = B.getAbsolutePath();
        String pathB = absolutePathB.substring(0,absolutePathB.lastIndexOf(File.separator)); 
        File pathBFile = new File(pathB);
        if (!pathBFile.exists()) {
            try {
                checkOK = pathBFile.mkdirs();
            } catch (Exception e) {
                log.debug("Exception creating the final destination directory of file "
                                                      + B.getAbsolutePath());
                throw new VOSpaceBackendException(e);                
            }
        } else if (pathBFile.isDirectory()){
            checkOK = true;
        } else {
            log.debug("File " + pathB + " already exsists, but is not a directory.");
            checkOK = false;
        }
        
        return checkOK;
    
    }

    protected void moveFileAToFileB (File A, File B) throws VOSpaceBackendException {
                    
        if (this.checksBeforeCopyOrMove(A, B)) {
            try {
                FileUtils.moveFile(A, B);
            } catch (Exception e) {
                log.debug("Exception moving temporary copy of uploaded file in its final destination directory");
                throw new VOSpaceBackendException(e);                
            }
        }
        
    }
    
    protected void copyFileAToFileB (File A, File B) throws VOSpaceBackendException {
                    
        if (this.checksBeforeCopyOrMove(A, B)) {
            try {
                FileUtils.copyFile(A, B);
            } catch (Exception e) {
                log.debug("Exception moving temporary copy of uploaded file in its final destination directory");
                throw new VOSpaceBackendException(e);                
            }
        }
        
    }
    
    protected String getStoragePath(String md5_sum) throws VOSpaceBackendException {
        
        String relativePath = createRelativePathFromMd5checksum(md5_sum);
        String storagePath = this.documentRoot + relativePath + File.separator;
        return storagePath;
        
    }
    
    protected String getTmpPath() {
        
        String tmpFilePath = this.tmpStorageRoot + File.separator;
        return tmpFilePath;
        
    }
        
}
