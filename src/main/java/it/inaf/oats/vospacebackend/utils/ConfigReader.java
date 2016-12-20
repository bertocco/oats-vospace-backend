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
package it.inaf.oats.vospacebackend.utils;

import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.exceptions.ExceptionMessage;
import java.util.HashMap;
import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

public class ConfigReader {
    
    private static final String DEFAULT_CONFIG_DIR = System.getProperty("user.home") 
                                                    + File.separator + "config"
                                                    + File.separator;
    private String fullPropFileName = new String();
    
    private static HashMap readProperties = new HashMap<String, String>();
    
    protected static Logger log = Logger.getLogger(ConfigReader.class);
    
    public ConfigReader(String propertiesFile) throws VOSpaceBackendException {
        
        fullPropFileName = DEFAULT_CONFIG_DIR + propertiesFile;
        
        try (InputStream in = new FileInputStream(fullPropFileName)) {

	    Properties prop = new Properties();
	    prop.load(in);

            for (String key : prop.stringPropertyNames()) {

                String value = prop.getProperty(key);
                readProperties.put(key, value);
                log.debug("Reading properties from " + fullPropFileName);
                log.debug(key + " = " + value);

	    }

        } catch (IOException e) {
            
            ExceptionMessage exMsg = new ExceptionMessage();
            log.fatal(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));           
            throw new VOSpaceBackendException(
               MessageFormat.format(exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), fullPropFileName));
	}
         
   }
    
   public String getProperty(String key) throws VOSpaceBackendException  {
        
       String property = "";
       try { 
           property = (String)readProperties.get(key);
           log.debug("Getting property:");
           log.debug(property);
       } catch (Exception e) {
            log.fatal(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));      
            ExceptionMessage exMsg = new ExceptionMessage();
            throw new VOSpaceBackendException(
               MessageFormat.format(exMsg.getMessage("PROPERTY_NOT_FOUND"), key, fullPropFileName));
           
       }
       
        return property;
        
    }
  
}
