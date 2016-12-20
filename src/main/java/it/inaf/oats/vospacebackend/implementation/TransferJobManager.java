/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.inaf.oats.vospacebackend.implementation;

import ca.nrc.cadc.uws.server.JobDAO.JobSchema;
import ca.nrc.cadc.uws.server.JobExecutor;
import ca.nrc.cadc.uws.server.SimpleJobManager;
import ca.nrc.cadc.uws.server.ThreadPoolExecutor;
import ca.nrc.cadc.vos.server.transfers.TransferRunner;

/**
 *
 * @author bertocco
 */
public class TransferJobManager extends SimpleJobManager {
    
    private static final Long MAX_EXEC_DURATION = new Long(12*3600L); // 12 hours
    private static final Long MAX_DESTRUCTION = new Long(7*24*3600L); // 1 week
    private static final Long MAX_QUOTE = new Long(12*3600L); // same as exec
    private JobSchema config;
    
    public TransferJobManager() {
        
        super();
         
        DatabaseJobPersistenceImpl jobPersist = new DatabaseJobPersistenceImpl();
        this.config = jobPersist.getJobSchema();
        
        // exec jobs in in new thread using our custom TransferRunner
        JobExecutor jobExec = new ThreadPoolExecutor(jobPersist, TransferRunner.class, 6);

        super.setJobPersistence(jobPersist);
        super.setJobExecutor(jobExec);
        super.setMaxExecDuration(MAX_EXEC_DURATION);
        super.setMaxDestruction(MAX_DESTRUCTION);
        super.setMaxQuote(MAX_QUOTE);
    }
    
    public JobSchema getConfig() {
        
        return config;
        
    }
    
}

