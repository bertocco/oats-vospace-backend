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

package it.inaf.oats.vospacebackend.exceptions;

import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class ExceptionMessage {
    
    private static HashMap myMessagesProperties = new HashMap<String, String>();
    protected static Logger log = Logger.getLogger(ExceptionMessage.class);
    
    public ExceptionMessage() {
        
        ResourceBundle rb = null; 
        try {
            ResourceBundle.getBundle("it.inaf.oats.vospacebackend.exceptions.exceptionMessages");
        } catch (Exception e) { 
            log.debug("Super unluky! Unable to read exception messages");
            log.debug(e);           
        }
        Enumeration <String> keys = rb.getKeys();
	while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = rb.getString(key);
            myMessagesProperties.put(key, value);
            log.debug("Reading VOSpaceBackend exception messages:");
            log.debug(key + " = " + value);
        }
    }
    
    
    public static String getMessage(String msgKey) {
        
        String excMsg = (String)myMessagesProperties.get(msgKey);
        log.debug("Getting message:");
        log.debug(excMsg);
        return excMsg;
    }
}
