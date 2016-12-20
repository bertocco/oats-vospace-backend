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

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import ca.nrc.cadc.net.TransientException;
import ca.nrc.cadc.util.RsaSignatureGenerator;
import ca.nrc.cadc.uws.Job;
import ca.nrc.cadc.uws.Parameter;
import ca.nrc.cadc.vos.Protocol;
import ca.nrc.cadc.vos.VOSURI;
import ca.nrc.cadc.vos.View;
import ca.nrc.cadc.vos.server.transfers.TransferGenerator;
import it.inaf.oats.vospacebackend.exceptions.ExceptionMessage;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.utils.ConfigReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
   
import java.security.InvalidKeyException;
import java.text.MessageFormat;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

/**
 *
 * @author bertocco
 */
public class TransferGeneratorImpl implements TransferGenerator{
    
    Logger log = Logger.getLogger(TransferGeneratorImpl.class);
    
    HashMap myResult = new <String, Object>HashMap();
    
    public List<URL> getURLs(VOSURI target, 
                      Protocol protocol, 
                      View view, 
                      Job job, 
                      List<Parameter> additionalParams)
                                    throws FileNotFoundException, TransientException {
        
        
        
        log.debug("VOSURI received: " + target.toString());
        
        List<URL> result = new ArrayList();
        
        // Get the node path, file name, vosuri
        String path = target.getPath();
        String fileName = target.getName();
        String vosuri = target.toString();                      
        String jobID = job.getID();
        
        
        try {
            String parametersToEncode = vosuri;
            String encodedParams = manageParametersEncoding(parametersToEncode);
            ConfigReader myConf = new ConfigReader("VOSpace.properties");
            String serviceBaseURL= myConf.getProperty("vospacebackend.service.base.url"); 
            
            result.add(new URL(serviceBaseURL + encodedParams + "/" + jobID));
        } catch (MalformedURLException e) {
            log.debug("Error parsing target");
            return null;
        } catch (VOSpaceBackendException ex) {
            ExceptionMessage exMsg = new ExceptionMessage();
            log.debug(MessageFormat.format(
                      exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), "VOSpace.properties"));
            log.debug(MessageFormat.format(
                             exMsg.getMessage("PROPERTY_NOT_FOUND"), "fs.posix.tmp.storage.root", "VOSpace.properties"));
            return null;
        }
    
        return result;
    }
    
    
    private String manageParametersEncoding(String toBeSigned) {
        
        log.debug("toBeSignedInput  = " + toBeSigned);
                
        // Encription
        RsaSignatureGenerator signatureGen = null;
        try {
            signatureGen = new RsaSignatureGenerator();
        } catch (Exception e) {
            log.debug("Exception creating   RsaSignatureGenerator" + e.getMessage() + "##############");  
            e.printStackTrace();
        }
        
        ByteArrayInputStream bais = null;
        try {   
            bais = new ByteArrayInputStream(toBeSigned.getBytes());
        } catch (Exception e) {
            log.debug("Exception creating   ByteArrayInputStream");       
        }
        
        StringBuilder sb = new StringBuilder();
        try { 
            byte[] sig = signatureGen.sign(bais);
            sb.append(new String(DatatypeConverter.printBase64Binary(sig)));
        } catch (IOException ioe) {
            log.debug("1111111111111");
            log.debug(ioe.getMessage());
        } catch (InvalidKeyException ike) {
            log.debug("222222222222222");
            log.debug(ike.getMessage());        
        }
        
        log.debug("url encripted = " + sb.toString());
        String signature = sb.toString();
        
        // Validation
        /*
        RsaSignatureVerifier su = new RsaSignatureVerifier();
        boolean valid = false;
        try {
        valid = su.verify(new ByteArrayInputStream(toBeSigned.getBytes()),
                    DatatypeConverter.parseBase64Binary(signature));
        } catch (IOException ioe) {
            log.debug("2");
            log.debug(ioe.getMessage());
        } catch (InvalidKeyException ike) {
            log.debug("2");           
            log.debug(ike.getMessage());       
        }
        if (valid)
            log.debug("VALID");
        else
            log.debug("BUGGY WORK!");
        */
        String finalStr = toBeSigned + "|" + signature;
        log.debug("originalParams = " + finalStr); 
        log.debug("Message = " + toBeSigned);
        log.debug("Signature = " + signature);
        finalStr = DatatypeConverter.printBase64Binary(finalStr.getBytes());
        
        return finalStr;
        
    }


}
