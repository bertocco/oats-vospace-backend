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

import org.apache.log4j.Logger;
import it.inaf.oats.vospacebackend.exceptions.ExceptionMessage;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.utils.ConfigReader;

import java.text.MessageFormat;

public class VOSpaceBackendImplFactory {
    
    private static final String CONFIG_FILE_NAME = "VOSpace.properties";
    private static final Logger log = Logger.getLogger(VOSpaceBackendImplFactory.class);
    private static String VOSpaceBackImplName = new String();
    
    public VOSpaceBackendImplFactory() throws VOSpaceBackendException {
        
        ConfigReader myConf = null;
        try {
            myConf = new ConfigReader(CONFIG_FILE_NAME);  
        } catch (Exception e) {
            ExceptionMessage exMsg = new ExceptionMessage();
            log.debug(MessageFormat.format(exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), CONFIG_FILE_NAME));
            throw new VOSpaceBackendException(                   
               MessageFormat.format(exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), CONFIG_FILE_NAME));
            
        }
        
        try {       
            this.VOSpaceBackImplName = myConf.getProperty("it.inaf.oats.vospacebackend.implementation.VOSpaceBackendImpl");     
        } catch (Exception e) {     
            ExceptionMessage exMsg = new ExceptionMessage();
            log.debug(MessageFormat.format(
                       exMsg.getMessage("PROPERTY_NOT_FOUND"), 
                                        "it.inaf.oats.vospacebackend.implementation.VOSpaceBackendImpl", 
                                        CONFIG_FILE_NAME));
            throw new VOSpaceBackendException(
               MessageFormat.format(
                       exMsg.getMessage("PROPERTY_NOT_FOUND"), 
                                        "it.inaf.oats.vospacebackend.implementation.VOSpaceBackendImpl", 
                                        CONFIG_FILE_NAME));            
        }
       
    }
    
    public VOSpaceBackend getVOSpaceBackImpl() throws VOSpaceBackendException {
        
        log.debug("Try to get object: " + this.VOSpaceBackImplName);
        return (VOSpaceBackend)this.buildObject(this.VOSpaceBackImplName);
        
    }
    
    private Object buildObject(String myBackendImpl) throws VOSpaceBackendException {
        
        Object result = null;
        try {
            //note that, with this style, the implementation needs to have a
            //no-argument constructor!
            Class implClass = Class.forName(myBackendImpl);
            result = implClass.newInstance();
        } catch (ClassNotFoundException ex) {
            log.debug("CLASS_NOT_FOUND");
            ExceptionMessage exMsg = new ExceptionMessage();
            throw new VOSpaceBackendException(MessageFormat.format(exMsg.getMessage("CLASS_NOT_FOUND"), myBackendImpl));
        } catch (InstantiationException ex) {
            log.debug("UNABLE_TO_INSTANTIATE");
            ExceptionMessage exMsg = new ExceptionMessage();
            throw new VOSpaceBackendException(MessageFormat.format(exMsg.getMessage("UNABLE_TO_INSTANTIATE"), myBackendImpl));
        } catch (IllegalAccessException ex) {
            log.debug("UNABLE_TO_ACCESS");
            ExceptionMessage exMsg = new ExceptionMessage();
            throw new VOSpaceBackendException(MessageFormat.format(exMsg.getMessage("UNABLE_TO_ACCESS"), myBackendImpl));
        }
        return result;
        
    }    
    
}
