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


import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bertocco
 */
public interface VOSpaceBackendInterface {
    
    
    
    // The md5 checksum is needed only for posix backend, but it is passed here 
    // as parameter to avoid to re-read the file (to calculate the checksum) or
    // to set it previously in the DB. The metadata set operation frees the Node 
    // so a new setBusyState will be needed
    public void fileFromTmpToFinalStorageArea(String storedFileID, String md5_sum) throws VOSpaceBackendException ;
    
    /**
     * 
     * @param vosuri
     * @param storedFileID
     * @return the complete file name (path+name) of the temporary location where 
     * the file is available)
     * @throws VOSpaceBackendException 
     */
    public String fileFromStorageAreaToTmp(String vosuri, String storedFileID) throws VOSpaceBackendException ;

}
