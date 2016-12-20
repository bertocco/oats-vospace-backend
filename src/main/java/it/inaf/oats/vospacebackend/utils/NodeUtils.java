/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.inaf.oats.vospacebackend.utils;

import ca.nrc.cadc.net.TransientException;
import ca.nrc.cadc.vos.Node;
import ca.nrc.cadc.vos.NodeNotFoundException;
import ca.nrc.cadc.vos.VOSURI;
import ca.nrc.cadc.vos.server.NodeID;
import it.inaf.oats.vospacebackend.implementation.DatabaseNodePersistenceImpl;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;

/**
 *
 * @author bertocco
 */
public class NodeUtils {
    
    public DatabaseNodePersistenceImpl dbNodePers;
    private static final Logger log = Logger.getLogger(NodeUtils.class);
    
    
    public NodeUtils() {
        
        dbNodePers = new DatabaseNodePersistenceImpl();
        
    }

    public Long getNodeIdLongfromVosuriStr(String vosuri) {
        
        Node myNode = null;
        NodeID nodeID;
        
        myNode = getNodeFromVosuriStr(vosuri);
        if (myNode != null) {
            nodeID = (NodeID)myNode.appData;        
            return nodeID.getID();
        } else {
            return null;
        }
        
    }
    
    /* Returns the node or null if something went wrong */
    public Node getNodeFromVosuriStr(String vosuri) {
        
        Node myNode = null;
        
        try {
            myNode = dbNodePers.get(new VOSURI(vosuri));
        } catch (NodeNotFoundException e) {
            log.debug("NodeNotFoundException getting node from persistence.");
            log.debug(e.getMessage());
        } catch (TransientException e) {
            log.debug("TransientException getting node from persistence.");
            log.debug(e.getMessage());
        } catch (URISyntaxException e) {
            log.debug("URISyntaxException getting node from persistence.");
            log.debug(e.getMessage());
        }
        
        return myNode;
    }
    
}
