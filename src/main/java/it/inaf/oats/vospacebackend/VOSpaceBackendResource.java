/**
 * _____________________________________________________________________________
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
 *
 */
package it.inaf.oats.vospacebackend;

import it.inaf.oats.vospacebackend.utils.ResourceManager;
import ca.nrc.cadc.uws.ExecutionPhase;
import ca.nrc.cadc.uws.Job;
import it.inaf.oats.vospacebackend.utils.JobUtils;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.Request;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Variant;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

import org.apache.http.HttpStatus;

/**
 *
 * @author bertocco
 */
public class VOSpaceBackendResource extends ServerResource implements org.apache.http.HttpStatus {

    protected Logger log = Logger.getLogger(VOSpaceBackendResource.class);

    @Put
    public Representation doPut(Representation entity, Variant variant) throws Exception {

        Representation result = null;
        int opResult = 0;

        log.info("Entering in PUT operation");

        String jobID = null;
        String vosuri;
        String encodedParameters;
        InputStream is;
        if (entity != null) {
            log.info("Received good entity");
            try {

                vosuri = readParameters();

                jobID = (String) getRequest().getAttributes().get("jobid");
                log.debug("Received jobid = " + jobID);

            } catch (MalformedURLException e) {

                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return this.printMessage("Malformed URL received. Unable to read parameters from URL");

            }

            try {

                is = entity.getStream();
                log.debug("Input stream get");
                opResult = ResourceManager.readAndSaveFile(vosuri, is);

            } catch (Exception e) {

                log.debug("Exception in readAndSaveFile");
                return this.printMessage("File NOT Uploaded! Something went wrong.");

            }

        }

        if (opResult == HttpStatus.SC_OK) {

            log.debug("File upload successful!");
            
            JobUtils jobUtil = new JobUtils();
            if(jobUtil.setJobPhase(jobID, ExecutionPhase.EXECUTING, ExecutionPhase.COMPLETED) != 0) {
                log.debug("Execution phase not correctly updated");
                this.printMessage("File Uploaded but operation not successfully completed"); 
            }
            setStatus(Status.SUCCESS_OK);
            return this.printMessage("File SUCCESSFULLY Uploaded!");

        } else {
            
            log.debug("Internal server error. Operation result code: " + String.valueOf(opResult));
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return this.printMessage("File NOT Uploaded! Something went wrong.");
            
        }

    }

    @Get
    public Representation doGet() {

        Representation result = null;
        String vosuri;
        String jobID;

        log.info("Entering in GET operation");
        try {

            vosuri = readParameters();

            jobID = (String) getRequest().getAttributes().get("jobid");
            log.debug("Received jobid = " + jobID);

        } catch (MalformedURLException e) {

            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return this.printMessage("Malformed URL received. Unable to read parameters from URL");

        }

        try {
            result = ResourceManager.downloadFile(vosuri);
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            result = this.printMessage("GET request: failed to download file.");
        }

        if (result != null) {
            
            setStatus(Status.SUCCESS_OK);
            JobUtils jobUtil = new JobUtils();
            if(jobUtil.setJobPhase(jobID, ExecutionPhase.EXECUTING, ExecutionPhase.COMPLETED) != 0) {
                log.debug("Execution phase not correctly updated");
                this.printMessage("File Get but operation not successfully completed"); 
            }
            return result;
        } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return this.printMessage("GET request: failed to download file.");
        }

    }

    private String readParameters() throws MalformedURLException {

        String vosuri;
        try {
            log.debug("Trying to read attributes");
            Request request = getRequest();
            String encodedParameters = (String) getRequest().getAttributes().get("parameters");
            log.debug("Received encoded parameters : " + encodedParameters);
            vosuri = ResourceManager.manageParametersDecoding(encodedParameters);
            log.debug("Received parameters decoded = " + vosuri);
        } catch (Exception e) {
            log.debug("Exception reading string parameters");
            log.debug(e);
            throw new MalformedURLException("Exception reading string parameters from URL");
        }

        return vosuri;

    }

    public static Representation printMessage(String error) {

        StringBuilder sb = new StringBuilder("");
        sb.append(error);
        sb.append("\n");
        return new StringRepresentation(sb.toString(), MediaType.TEXT_PLAIN);
    }

}
