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

import java.util.HashMap;

import org.apache.log4j.Logger;

import ca.nrc.cadc.vos.VOSURI;

/**
 *
 * @author bertocco
 */
public class VOSURIParser {
    
    private String scheme;
    private String namingAuthority;
    private String query;
    private String path;
    private HashMap content;
    
    protected Logger log;
    
    public VOSURIParser() {
        
        log = Logger.getLogger(VOSURIParser.class); 
        
        scheme = new String("");
        namingAuthority = new String("");
        query = new String("");
        path = new String("");
        content = new <String, String>HashMap();  
        content.put("scheme", "");   
        content.put("namingAuthority", "");
        content.put("query", "");
        content.put("path", "");
        
    }
    
    public VOSURIParser(VOSURI vosuri) throws Exception {
        
        log = Logger.getLogger(VOSURIParser.class); 
        
        content = this.parse(vosuri);
        
        scheme = (String)content.get("scheme");
        namingAuthority = (String)content.get("namingAuthority");
        query = (String)content.get("query");
        path = (String)content.get("path");
        
               
    }
    
    public HashMap parse(VOSURI vosuri) throws Exception {
        
        String myScheme = new String("");
        String myNamingAuthority = new String("");
        String myQuery = new String("");
        String myPath = new String("");
        
        String vosuriStr = vosuri.toString();
        String mainSeparator = "://";
        String exclamationMark = "!";
        String tilde = "~";
        boolean useExclamationMark = false;
        boolean useTilde = false;
        
        if(vosuriStr != null && vosuriStr.length() > 0 ) {
            
            if (vosuriStr.contains(mainSeparator) &&
                   vosuriStr.contains(exclamationMark) || vosuriStr.contains(tilde)) {
                log.debug("VOSURI " + vosuriStr + "well formed");
                if (vosuriStr.contains(exclamationMark))
                   useExclamationMark = true;
              if (vosuriStr.contains(tilde))
                   useTilde = true;
            
            } else {
               throw new Exception("Malformed URI received: " + vosuriStr);
            }
                
            myScheme = vosuriStr.substring(0, vosuriStr.indexOf(mainSeparator));
        
            if(useExclamationMark)
                myNamingAuthority = 
                      vosuriStr.substring(vosuriStr.indexOf(mainSeparator)+3, vosuriStr.indexOf(exclamationMark));
    
            if(useTilde)
                myNamingAuthority = 
                      vosuriStr.substring(vosuriStr.indexOf(mainSeparator)+3, vosuriStr.indexOf(tilde));
        
            // Remove fragment if present
            String vosuriWithoutFragment;
            int endIndex = vosuriStr.lastIndexOf("#"); 
            if (endIndex != -1) {
                vosuriWithoutFragment = vosuriStr.substring(0, endIndex);
            } else {
                log.debug("URI without fragment received: " + vosuriStr); 
                vosuriWithoutFragment = vosuriStr;
            }
            
            // get path as the part of the string after the last occurrence of "/"      
            if (vosuriWithoutFragment != null && vosuriWithoutFragment.length() > 0 ) {
                endIndex = vosuriWithoutFragment.lastIndexOf("/");
                if (endIndex != -1) {
                    myPath = vosuriWithoutFragment.substring(endIndex+1, vosuriWithoutFragment.length()); 
                }
            } else {             
                throw new Exception("Malformed URI received: " + vosuriStr);
            }
         
        } else {            
            throw new Exception("Malformed URI received: " + vosuriStr);           
        }
        
        content.put("scheme", myScheme);   
        content.put("namingAuthority", myNamingAuthority);
        content.put("query", "");
        content.put("path", myPath);
        
        return content;
    }
    
}
