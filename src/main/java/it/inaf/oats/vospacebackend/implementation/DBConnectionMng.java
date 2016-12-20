/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.inaf.oats.vospacebackend.implementation;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import it.inaf.oats.vospacebackend.exceptions.ExceptionMessage;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author bertocco
 */
public class DBConnectionMng {
       
    private static final Logger log = Logger.getLogger(DBConnectionMng.class);
    
    public static Connection getDBConnection() throws SQLException, VOSpaceBackendException {

        DataSource ds = getDataSource();
        Connection dbConnection = ds.getConnection();
        			               
	return dbConnection;

    }
    
    
    public static DataSource getDataSource() {
        
        DataSource ds = null;
        
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            String vosNodesDataSourceName = "jdbc/cadctest";
            ds = (DataSource) envContext.lookup(vosNodesDataSourceName);
        } catch (NamingException ex) {
            log.fatal(ex);
            ds = null;
        }
        
        return ds;
    }
    
    
    
    /**
     * Providing a data source.
     */
    /*
    protected DataSource getDataSource() {
    
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setDatabaseName("mydatabasename");
        dataSource.setUser("mydatabaseuser");
        dataSource.setPassword("mydbuserpassword");
        dataSource.setServerName("mydatabasehost");
        
        return dataSource;
    }
    */
    
}
