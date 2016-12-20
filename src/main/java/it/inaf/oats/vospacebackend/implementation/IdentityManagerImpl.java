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

import ca.nrc.cadc.auth.HttpPrincipal;
import ca.nrc.cadc.auth.IdentityManager;
import java.security.Principal;
import java.sql.Types;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.Subject;


/**
 *
 * @author bertocco
 */
public class IdentityManagerImpl implements IdentityManager {

    @Override
    /**
     * Create a subject from the specified owner object. This is the reverse
     * of toOwner(Subject). The returned subject must include at least one
     * Principal but need not contain any credentials.
     *
     * @param owner
     * @return
     */
    /**
     * Override
     * Return subject containing the owner as an HttpPrincipal
     */
    public Subject toSubject(Object owner) {
        
        Subject subject = new Subject();
        Principal httpPrincipal = new HttpPrincipal(owner.toString());
        Set<Principal> principals = subject.getPrincipals();
        principals.add(httpPrincipal);
        
        return subject;
    }

    @Override
    /**
     * Convert the specified subject into an arbitrary object. This is the reverse
     * of toSubject(owner). The persistable object must capture the identity (the
     * principal from the subject) but generally does not capture the credentials.
     *
     * @param subject
     * @return arbitary owner object to be persisted
     */
    /**
     * Overrride
     * Return simple hardcoded owner name from subject's HttpPrincipal
     */
    public Object toOwner(Subject subject) {
        
        String owner = null;
        Set<Principal> principals = subject.getPrincipals();
        Iterator<Principal> principalsIterator = principals.iterator();
        while (principalsIterator.hasNext()) {
            Principal next = principalsIterator.next();
            if (next instanceof HttpPrincipal) {
                String HttpName = next.getName();
                if (HttpName.equals("Marco")) {
                    owner = HttpName;
                } else {
                    owner = "whois:" + HttpName + "?";
                }
            }
        }
        
        return owner;
    }

    @Override
    /**
     * Get the SQL TYPE for the column that stores the object
     * returned by toOwner(Subject);
     *
     * @see java.sql.Types
     * @see java.sql.PreparedStatement.setObject(int,Object,int)
     * @return a valid SQL type for use with a PreparedStatement
     */
    /**
     * Override
     * Hardcode VARCHAR sql Type for the ownerID column in Node table.
     */
    public int getOwnerType() {
        return Types.VARCHAR;
    }

    @Override
    /**
     * Convert the specified subject to a suitable string representation of the
     * owner. This should normally be an X509 distinguished name if IVOA
     * single-sign-on has been implemented.
     *
     * @see VOS.PROPERTY_URI_CREATOR
     * @param subject
     * @return string representation of the owner (principal)
     */
    /**
     * Override
     * Returns ownerID String, i.e. itself.
     * NOTE: code pasted directly from the toOwner method.
     */
    public String toOwnerString(Subject subject) {
        
        String owner = null;
        Set<Principal> principals = subject.getPrincipals();
        Iterator<Principal> principalsIterator = principals.iterator();
        while (principalsIterator.hasNext()) {
            Principal next = principalsIterator.next();
            if (next instanceof HttpPrincipal) {
                String HttpName = next.getName();
                if (HttpName.equals("Marco")) {
                    owner = HttpName;
                } else {
                    owner = "whois:" + HttpName + "?";
                }
            }
        }
        
        return owner;
    }
    
}
