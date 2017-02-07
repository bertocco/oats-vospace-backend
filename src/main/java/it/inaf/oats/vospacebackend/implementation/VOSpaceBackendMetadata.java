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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;

/*
 * FileRecord myParams is used as input record
 * HashMap myResult is used as output record
 * @author bertocco
 */
public class VOSpaceBackendMetadata {
    
    /**
     * This class has two attributes:
     * <p>
     * Logger to perform logging operations
     * <p>
     * HashMap which will contain the metadata set identifying a stored/to store file
     * This hashmap fields will are:
     * boolean ifSuccessfull if db query is successful or not
     * String "original_file_name"
     * String "stored_file_name"
     * String "md5_checksum"
     * String "relative_path"
     */
    
    protected Logger log;
    
    protected FileRecord myResult; 

    /** 
     * Create the log and the HashMap that will be used to store the database
     * query result to return. The field "ifSuccessful" will contain a boolean 
     * value true if operation successfully executed and false if not.
     * Other fields will be added containing the query result set fields, 
     * depending on the executed query.
     */
    public VOSpaceBackendMetadata() {
        
        log = Logger.getLogger(VOSpaceBackendMetadata.class);  
        myResult = new FileRecord();
        myResult.setIfOperationSuccessful(false);
        
    }
    
    
    public FileRecord getRequestByNode(String nodeID) 
                                  throws VOSpaceBackendException, SQLException {
        
        String myOp = "GET_REQ";
       
        String myQuery = "SELECT * FROM cadctest.StoredFileAndNode " +
                "WHERE nodeID=?";
        log.debug("query : " + myQuery);
        log.debug("where nodeID =" + nodeID);
        // Da risistemare, non so neanche piu` se la uso ancora
        FileRecord myParams = new FileRecord();
        myParams.setNodeId(nodeID);
        
        log.debug("Going to execute query with FileRecord = " + myParams.toString());
        
        return excuteQuery(myOp, myQuery, myParams, "nodeID");
        
    }
    
    
    public FileRecord getRequestByStorage(String storedFileID) 
                                  throws VOSpaceBackendException, SQLException {
        
        String myOp = "GET_REQ_BY_STORE_ID";
       
        String myQuery = "SELECT * FROM cadctest.StoredFileAndNode " +
                "WHERE storedFileID=?";
        log.debug("query : " + myQuery);
        // Da risistemare, non so neanche piu` se la uso ancora
        FileRecord myParams = new FileRecord();
        myParams.setStoredfileName(storedFileID);
        
        log.debug("Going to execute query with FileRecord = " + myParams.toString());
        
        return excuteQuery(myOp, myQuery, myParams, "storedFileID");
        
    }
    
      
    public FileRecord setRequest(String stored_f_name, String nodeID) 
                                  throws VOSpaceBackendException, SQLException {
       
        String myOp = "SET_REQ";
        FileRecord backendMetadata = getRequestByNode(nodeID);
        String myQuery = "";
        if (backendMetadata == null) {
            log.debug("Backend metadata not found");
            backendMetadata.setIfOperationSuccessful(false);
            return backendMetadata;
        }
        if (!backendMetadata.getIfOperationSuccessful()) {
            myQuery = "INSERT INTO cadctest.StoredFileAndNode " +
                "(nodeID, storedFileID)" +
                " VALUES (?, ?);";
            
        } else {
            myQuery = "UPDATE cadctest.StoredFileAndNode SET " +
                "nodeID = ?, storedFileID = ? WHERE nodeID = '" + nodeID + "';";
            
        }
        log.debug("query : " + myQuery);
        FileRecord myParams = new FileRecord();
        myParams.setStoredfileName(stored_f_name);
        myParams.setNodeId(nodeID);
        
        log.debug("Going to execute query with FileRecord = " + myParams.toString());
                
        return excuteQuery(myOp, myQuery, myParams, "");
        
    }
    
    public FileRecord getNodePropertiesFromStoredName(String stored_f_name) 
                                           throws VOSpaceBackendException, SQLException {
    
        String myOp = "GET_FROM_STORED_NAME";
        String myQuery = 
        "select * from Node join StoredFileAndNode on Node.nodeID=StoredFileAndNode.nodeID where storedFileID='?'";
        log.debug("query : " + myQuery);
        FileRecord myParams = new FileRecord();
        myParams.setStoredfileName(stored_f_name);
        
        log.debug("Going to execute query with FileRecord = " + myParams.toString());
        
        return excuteQuery(myOp, myQuery, myParams, "");
        
    }
    

    private FileRecord excuteQuery(String operation, String query, FileRecord fileToManage, String paramName) 
                                                throws VOSpaceBackendException, SQLException {
        
        PreparedStatement preparedStatementInsert = null;
       
        Connection dbConnection = null;
        
        try {
	    dbConnection = DBConnectionMng.getDBConnection();
            log.debug("Database connection get");
        } catch (SQLException e) {
            log.fatal(e);
            throw new VOSpaceBackendException("UNABLE_TO_GET_DB_CONNECTION");
	}

	try {
	    dbConnection.setAutoCommit(false);
            log.debug("Autocommit set false");
	} catch (SQLException e) {
            log.fatal(e);
            throw new VOSpaceBackendException("ERROR_DISABLING_DB_AUTOCOMMIT");
	}
        
	// Starts JDBC Transaction
        PreparedStatement preparedQuery = null;
        // boolean query result (success or not)
        log.debug("Input file record: " + fileToManage.toString());
        boolean ifQuerySuccessful = false;
	try {
            
            //HashMap fileToManageFields = fileToManage.getFileRecord();
            ResultSet rs = null;   
            int rowCounter = 0;
            switch (operation) {
                case "GET_REQ":
                    preparedQuery = dbConnection.prepareStatement(query);
                    preparedQuery.setString(1, fileToManage.get(paramName));
                    rs = preparedQuery.executeQuery();
                    rowCounter = 0;
                    while (rs.next()) {
                        rowCounter = rowCounter +1;
                        log.debug("storedFileID = " + rs.getString("storedFileID"));
                        log.debug("nodeID" + rs.getString("nodeID"));
                        myResult.setStoredfileName(rs.getString("storedFileID"));
                        myResult.setNodeId(rs.getString("nodeID"));
                    }
                    if (rowCounter == 0) {
                        log.debug("GET_REQ: File metadata not found in backend");                      
                        ifQuerySuccessful = false;
                    } else {
                        log.debug("GET_REQ: query successfully executed. File found in the backend");
                        ifQuerySuccessful = true;
                    }
                    break;
                case "SET_REQ":                   
                    log.debug("Going to prepare query for SET operation");
                    preparedQuery = dbConnection.prepareStatement(query);
                    preparedQuery.setString(1, fileToManage.getNodeId());
                    preparedQuery.setString(2, fileToManage.getStoredfileName());
                    log.debug("Parameters : nodeID = " + fileToManage.getNodeId() 
                            + " StoredfileName = " + fileToManage.getStoredfileName());
                    log.debug("Going to execute query");
                    preparedQuery.executeUpdate();
                    log.debug("Query executed");
                    dbConnection.commit();
                    log.debug("Query committed"); 
                    ifQuerySuccessful = true;
                    break;    
                case "GET_FROM_STORED_NAME":                     
                    log.debug("Going to prepare query for GET_FROM_STORED_NAME operation");  
                    preparedQuery = dbConnection.prepareStatement(query);
                    preparedQuery.setString(1, fileToManage.getStoredfileName());
                    rs = preparedQuery.executeQuery();
                    rowCounter = 0;
                    while (rs.next()) {
                        rowCounter = rowCounter +1;
                        myResult.setStoredfileName(rs.getString("storedFileID"));
                        myResult.setMD5Checksum(rs.getString("contentMD5"));
                        myResult.setNodeId(rs.getString("nodeID"));
                        
                    }
                    if (rowCounter == 0) {
                        log.debug("GET_REQ: query NOT executed. File metadata not found in backend");                      
                        ifQuerySuccessful = false;
                    } else {
                        log.debug("GET_REQ: query successfully executed. File found in the backend");
                        ifQuerySuccessful = true;
                    }
                    break;                        
                    
                default:
                    ifQuerySuccessful = false;
                    log.fatal("DB_OPERATION_NOT_RECOGNIZED");
                    throw new VOSpaceBackendException("DB_OPERATION_NOT_RECOGNIZED");                    
                }
            
            dbConnection.setAutoCommit(true);
                        
            dbConnection.close();
            
        } catch (SQLException e) {

            log.error("SQLException exception executing query: " + query);
            log.error(e.getMessage());
	    dbConnection.rollback();

	} finally {

            if (preparedQuery != null) {
		preparedQuery.close();
            }

            if (dbConnection != null) {
		dbConnection.close();
            }
            
	} 
        
        myResult.setIfOperationSuccessful(ifQuerySuccessful);
        
        log.debug("Returning " + operation + " result:\n" + myResult.toString());
        
        return myResult;
           
    }
    
}
