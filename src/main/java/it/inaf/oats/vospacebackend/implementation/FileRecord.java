/**
 * _____________________________________________________________________________
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
 *
 */
package it.inaf.oats.vospacebackend.implementation;

import org.apache.log4j.Logger;

public class FileRecord {

    private Logger log = Logger.getLogger(FileRecord.class);
    private  String stored_file_name_value;
    private  String md5_checksum_value;
    private  String nodeID_value;
    private  boolean operationSuccessful_value;

    public FileRecord() { 
        
        stored_file_name_value = "";
        md5_checksum_value = "";
        nodeID_value = "";
        operationSuccessful_value = false;

    }

    public void setFileRecord(String stored_f_name, String md5Checksum, String fileProperty,
                              String nodeId, boolean ifSuccessful) {

        this.stored_file_name_value = stored_f_name;
        this.md5_checksum_value = md5Checksum;
        this.nodeID_value = nodeId;
        this.operationSuccessful_value = ifSuccessful;

    }

    public void setStoredfileName(String storedfName) {
        
        this.stored_file_name_value = storedfName;
        
    }

    public String getStoredfileName() {

        return this.stored_file_name_value;
        
    }

    public void setMD5Checksum(String md5checksum) {
        
        this.md5_checksum_value = md5checksum;
        
    }

    public String getMD5Checksum() {
        
        return this.md5_checksum_value;
        
    }

    public void setNodeId(String nodeId) {
        
        this.nodeID_value = nodeId;
        
    }

    public String getNodeId() {
        
        return this.nodeID_value;
        
    }

    public boolean getIfOperationSuccessful() {
            
        return this.operationSuccessful_value;
        
    }

    public void setIfOperationSuccessful(boolean ifSuccess) {
        
        this.operationSuccessful_value = ifSuccess;
    }
    
    
    public String get(String paramName) {
        
        switch (paramName){
            case "nodeID":
                return this.nodeID_value;
            case "storedFileID":
                return this.stored_file_name_value;
            default:
                log.debug("Parameter " + paramName + "value not available");
                return "";
        }
    }
    
    public String toString() {

        String res = "FileRecord:  \nstored_file_name   = " + getStoredfileName() + "\n"
                + "md5_checksum       = " + getMD5Checksum() + "\n"
                + "nodeID             = " + getNodeId() + "\n"
                + "operationSuccessful = " + getIfOperationSuccessful() + "\n";

        return res;

    }   

}
