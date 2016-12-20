/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.inaf.oats.vospacebackend.implementation;

import ca.nrc.cadc.uws.server.DatabaseJobPersistence;
import ca.nrc.cadc.uws.server.JobDAO;
import ca.nrc.cadc.uws.server.JobDAO.JobSchema;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
/**
 *
 * @author bertocco
 */
public class DatabaseJobPersistenceImpl extends DatabaseJobPersistence {
 
    

    protected JobDAO.JobSchema getJobSchema() {
        
        Map<String,Integer> jobTabLimits = new HashMap<String,Integer>();
        jobTabLimits.put("jobInfo_content", 1024);
        Map<String,Integer> detailTabLimits = new HashMap<String,Integer>();
        JobSchema jobSchema  = new JobSchema("vospace.dbo.Job", "vospace.dbo.JobDetail", true, jobTabLimits, detailTabLimits);
        
        return new JobDAO.JobSchema("Job", "JobDetail", false);
        
    }
    
    /**
     * Providing a data source.
     */
    protected DataSource getDataSource() {
        
        return DBConnectionMng.getDataSource();
    }
    
}
