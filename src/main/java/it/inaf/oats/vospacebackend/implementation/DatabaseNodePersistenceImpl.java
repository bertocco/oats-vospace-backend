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

import ca.nrc.cadc.auth.ACIdentityManager;
import ca.nrc.cadc.auth.IdentityManager;
import ca.nrc.cadc.vos.server.DatabaseNodePersistence;
import ca.nrc.cadc.vos.server.NodeDAO;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


public class DatabaseNodePersistenceImpl extends DatabaseNodePersistence {

    /**
     * Constructor
     */
    public DatabaseNodePersistenceImpl() {
        super(new NodeDAO.NodeSchema("Node", "NodeProperty", false), "/deleted_nodes");
    }

    @Override
    protected IdentityManager getIdentityManager()
    {
        return new ACIdentityManager();
    }
    
    @Override    
    /**
     * Providing a data source.
     */
    protected DataSource getDataSource() {
        
        return DBConnectionMng.getDataSource();
    }
    
}