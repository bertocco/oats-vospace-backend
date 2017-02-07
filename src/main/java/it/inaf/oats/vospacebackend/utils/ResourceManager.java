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
package it.inaf.oats.vospacebackend.utils;

import ca.nrc.cadc.net.TransientException;
import org.apache.log4j.Logger;

import ca.nrc.cadc.util.RsaSignatureVerifier;
import ca.nrc.cadc.uws.ExecutionPhase;
import ca.nrc.cadc.uws.Job;
import ca.nrc.cadc.uws.server.DatabaseJobPersistence;
import ca.nrc.cadc.uws.server.JobDAO;
import ca.nrc.cadc.uws.server.JobNotFoundException;
import ca.nrc.cadc.uws.server.JobPersistenceException;

import it.inaf.oats.vospacebackend.VOSpaceBackendResource;
import it.inaf.oats.vospacebackend.exceptions.ExceptionMessage;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.implementation.DatabaseJobPersistenceImpl;
import it.inaf.oats.vospacebackend.implementation.VOSpaceBackendImplFactory;
import it.inaf.oats.vospacebackend.implementation.VOSpaceBackend;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.HttpStatus;

import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;

/**
 *
 * @author bertocco
 */
public class ResourceManager implements org.apache.http.HttpStatus {

    private static Logger log = Logger.getLogger(VOSpaceBackendResource.class);
    
    
    private static int readAndSaveFile(String vosuri, FileItem fi)
            throws IOException, VOSpaceBackendException {

        Representation result;
        log.debug("Entering in readAndSaveFile");

        InputStream is = fi.getInputStream();
        log.debug("Input stream get");

        return readAndSaveFile(vosuri, is);

    }

    public static int readAndSaveFile(String vosuri, InputStream is)
            throws IOException, VOSpaceBackendException {

        int result;
        String md5sum = null;
        
        // Get temporary document root from configuration file
        String tmpStorageRoot = new String();
        try {
            ConfigReader myConf = new ConfigReader("VOSpace.properties");
            tmpStorageRoot = myConf.getProperty("fs.posix.tmp.storage.root");
        } catch (Exception e) {
            ExceptionMessage exMsg = new ExceptionMessage();
            log.debug(MessageFormat.format(
                    exMsg.getMessage("UNABLE_TO_READ_PROPERTIES"), "VOSpace.properties"));
            throw new VOSpaceBackendException(MessageFormat.format(
                    exMsg.getMessage("PROPERTY_NOT_FOUND"), "fs.posix.tmp.storage.root", "VOSpace.properties"));
        }
        // Create the temporary directory, if needed
        File path = new File(tmpStorageRoot);
        if (!path.exists()) {
            boolean statusOK = path.mkdirs();
            if (!statusOK)
               throw new VOSpaceBackendException("Unable to create VOSpace Backend Temporary Document Root");
        }  
        
        

        // Generate the unique file identifier (storageFileID)
        String unique_file_id_str = UUID.randomUUID().toString();
        log.debug("Unique file identifyer " + unique_file_id_str);


        // Seve the temporary file in temporary location with the new unique name
        File savedUploadedFile = new File(tmpStorageRoot + File.separator + unique_file_id_str);
        
        log.debug("Writing " + savedUploadedFile + File.separator + unique_file_id_str);
        FileOutputStream outStream = new FileOutputStream(savedUploadedFile);
        try {
            md5sum = readWriteAndGetChecksum(is, outStream);
        } catch (VOSpaceBackendReadInputException e) {
            log.debug("File to upload received is a zero-length file");
            log.debug("File NOT Uploaded! Something went wrong in readWriteAndGetChecksum." + e.getMessage());
            result = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } catch (IOException e) {
            log.debug("Error reading file to upload");
            log.debug("File NOT Uploaded! Something went wrong in readWriteAndGetChecksum." + e.getMessage());
            result = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } catch (NoSuchAlgorithmException e) {
            log.debug("Exception in MD5 checksum generation");
            log.debug("File NOT Uploaded! Something went wrong in readWriteAndGetChecksum." + e.getMessage());
            result = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } catch (VOSpaceBackendTransferAbortedException e) {
            log.debug("Exception in reading file to upload");
            log.debug("File NOT Uploaded! Something went wrong in readWriteAndGetChecksum." + e.getMessage());
            result = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        try {
            if (storeUploadedFile(vosuri, unique_file_id_str, md5sum, savedUploadedFile.length())) {
                log.debug("File successfully uploaded");
                result = HttpStatus.SC_OK;
            } else {
                log.debug("File NOT Uploaded! Something went wrong in storeUploadedFile.");
                result = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        } catch (Exception e) {

            log.debug("File NOT Uploaded! Something went wrong in readWriteAndGetChecksum." + e.getMessage());
            result = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        return result;
    }

    public static boolean storeUploadedFile(String vosuri, String tmp_file_name, String md5_sum, Long fileLength) {
        boolean stored = false;
        log.debug("Entering in storeUploadedFile");
        try {
            VOSpaceBackendImplFactory myVOSpaceFactory = new VOSpaceBackendImplFactory();
            log.debug("myVOSpaceFactory created");
            VOSpaceBackend myVOSpace = myVOSpaceFactory.getVOSpaceBackImpl();
            log.debug("myVOSpace get");
            stored = myVOSpace.createFile(vosuri, tmp_file_name, md5_sum, fileLength);
            log.debug("File stored: " + stored);
        } catch (Exception e) {
            log.debug("Unrecoverable exception storing file.");
        }
        return stored;

    }

    /* Return the file representation if ok, null elsewhere */
    public static Representation downloadFile(String vosuri) throws Exception {

        Representation result;

        log.debug("Entering in downloadFile");
        VOSpaceBackendImplFactory myVOSpaceFactory = new VOSpaceBackendImplFactory();
        log.debug("myVOSpaceFactory created");
        VOSpaceBackend myVOSpace = myVOSpaceFactory.getVOSpaceBackImpl();
        log.debug("myVOSpace get");
        File fileToDownload = myVOSpace.returnFile(vosuri);
        if (fileToDownload != null) {
            log.debug("File found, fileToDownload is not null");
            FileRepresentation fr = new FileRepresentation(fileToDownload.getAbsolutePath(),
                    MediaType.APPLICATION_OCTET_STREAM);
            result = fr;

        } else {
            log.debug("File NOT found, fileToDownload is null");
            result = null;
        }

        return result;
    }

    public static String manageParametersDecoding(String toBeVerified) throws VOSpaceBackendException {

        String vosuri;
        int count = 0;

        log.debug("manageParametersEncoding BEGIN");
        String urlStr = new String(DatatypeConverter.parseBase64Binary(toBeVerified));
        log.debug("urlStr = " + urlStr);
        vosuri = urlStr.substring(0, urlStr.indexOf("|"));
        log.debug("vosuri = " + vosuri);
        String remaining = urlStr.substring(urlStr.indexOf("|") + 1, urlStr.length());
        log.debug("Remaining after get vosuri = " + remaining);
        String signature = remaining.substring(remaining.indexOf("|") + 1, remaining.length());
        log.debug("signature = \n" + signature);

        // Validation
        log.debug("I am going to create RsaSignatureVerifier");
        RsaSignatureVerifier su = new RsaSignatureVerifier();
        log.debug("Created");

        boolean valid = false;
        try {
            valid = su.verify(new ByteArrayInputStream(vosuri.getBytes()),
                    DatatypeConverter.parseBase64Binary(signature));
        } catch (IOException ioe) {
            log.debug("IOException in valid = su.verify(new ByteArrayInputStream(vosuri.getBytes()),\n"
                    + "DatatypeConverter.parseBase64Binary(signature))");
            throw new VOSpaceBackendException(ioe.getMessage());
        } catch (InvalidKeyException ike) {
            log.debug("InvalidKeyException in valid = su.verify(new ByteArrayInputStream(vosuri.getBytes()),\n"
                    + "DatatypeConverter.parseBase64Binary(signature))");
            log.debug("InvalidKeyException");
            throw new VOSpaceBackendException(ike.getMessage());
        }

        return vosuri;

    }

    /* Read a file from the InputStream parameter,
       write the file in the FileOutputStream parameter.
       Returns the MD5 checksum of the stored file.    
     */
    public static String readWriteAndGetChecksum(InputStream istream, FileOutputStream ostream)
            throws VOSpaceBackendReadInputException, IOException, NoSuchAlgorithmException,
            VOSpaceBackendTransferAbortedException {

        log.debug("Entering in readWriteAndGetChecksum(InputStream, FileOutputStream)");

        int DEFAULT_BUFFER_SIZE = 1024 * 4;
        String checksum = null;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;

        MessageDigest msgDigest = MessageDigest.getInstance("MD5");

        // do an initial read to ensure there are bytes in the stream
        try {
            bytesRead = istream.read(buffer, 0, DEFAULT_BUFFER_SIZE);
            if (bytesRead <= 0) {
                // do not allow the creation of zero-length files
                log.debug("Cannot write a zero-length file.");
                throw new VOSpaceBackendReadInputException("Cannot write a zero-length file.");
            } else {
                log.debug("First bytes read OK. Input stream available and readable");
            }
        } catch (IOException ex) {
            String errorMsg = "Upstream exception while reading from "
                    + istream.getClass().getName() + ": "
                    + ex.getMessage();
            log.debug("IOException in the first byte reading of the incoming file");
            log.debug(errorMsg);
            throw new VOSpaceBackendTransferAbortedException(errorMsg);
        }

        // Loop reading and writing data.
        msgDigest.update(buffer, 0, bytesRead);
        log.debug("First msgDigest.update OK");

        while (bytesRead >= 0) {
            ostream.write(buffer, 0, bytesRead);
            try {
                bytesRead = istream.read(buffer, 0, DEFAULT_BUFFER_SIZE);
                if (bytesRead > 0) {
                    msgDigest.update(buffer, 0, bytesRead);
                }
            } catch (IOException ex) {
                String errorMsg = "Upstream exception while reading from "
                        + istream.getClass().getName() + ": "
                        + ex.getMessage();
                log.debug(errorMsg);
                log.debug("A Exception in reading/writing file" + ex.getMessage());
                throw new VOSpaceBackendTransferAbortedException(errorMsg);
            }
        }
        ostream.flush();
        ostream.close();

        //Get the hash's bytes
        byte[] bytes = msgDigest.digest();

        String md5_checksum = new String(Hex.encodeHex(bytes));
        log.debug("MD5 checksum calculated: " + md5_checksum);

        return md5_checksum;

    
    }
}
