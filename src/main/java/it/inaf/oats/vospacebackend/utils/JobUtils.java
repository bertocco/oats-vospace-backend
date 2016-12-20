/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.inaf.oats.vospacebackend.utils;

import ca.nrc.cadc.net.TransientException;
import ca.nrc.cadc.uws.ExecutionPhase;
import ca.nrc.cadc.uws.server.DatabaseJobPersistence;
import ca.nrc.cadc.uws.server.JobNotFoundException;
import ca.nrc.cadc.uws.server.JobPersistenceException;
import it.inaf.oats.vospacebackend.implementation.DatabaseJobPersistenceImpl;
import org.apache.log4j.Logger;

/**
 *
 * @author bertocco
 */
public class JobUtils {
    
    public static DatabaseJobPersistence dbJobPers;
    
    
    private static final Logger log = Logger.getLogger(JobUtils.class);
    
    
    public JobUtils() {
        
        dbJobPers = new DatabaseJobPersistenceImpl();
        
    }
    
    
    public int setJobPhase(String jobID, ExecutionPhase startExecPhase, ExecutionPhase endExecPhase) {

        int res = 0;
        
        try {
            log.debug(" I am going to set the execution phase to completed");
            dbJobPers.setPhase(jobID, startExecPhase, endExecPhase);
            log.debug("Execution phase set!");
        } catch (JobNotFoundException jnfe) {
            log.debug("JobNotFoundException in getting job persistence or setting");
            res = -1;
        } catch (JobPersistenceException jpe) {
            log.debug("JobPersistenceException in getting job persistence or setting");
            res = -1;
        } catch (TransientException te) {
            log.debug("TransientException in getting job persistence or setting");
            res = -1;
        }

        return res;

    }
}
