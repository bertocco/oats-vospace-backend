/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.inaf.oats.vospacebackend.implementation;
import it.inaf.oats.vospacebackend.exceptions.VOSpaceBackendException;
import it.inaf.oats.vospacebackend.utils.ConfigReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
//import org.openstack4j.openstack.logging.Logger;
//import org.openstack4j.openstack.logging.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.types.Facing;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.model.common.ActionResponse;


/**
 *
 * @author bertocco
 */
public class VOSpaceBackendSwift  extends it.inaf.oats.vospacebackend.implementation.VOSpaceBackend {
        
    private static final Logger log = LoggerFactory.getLogger(VOSpaceBackendSwift.class);

    private static final String CONFIG_FILE_NAME = "VOSpace.properties";   
    private static final String default_keystone_auth_url = "https://XXXXXXXXX.YYYYYYY.ZZZ:5000/v2.0";
    private static final String default_keystone_username = "XXXXXXX";
    private static final String default_keystone_password = "XXXXXXXXXXXXXX";
    private static final String default_user_domain = "XXXXXXXXXXXX";
    private static final String default_tmpStorageRoot = "/tmp/vospace";
    private static final String default_swift_supported_api_version = "V2";
    private static String keystone_auth_url;
    private static String keystone_username;
    private static String keystone_password;
    private static String user_domain; 
    private static String swift_supported_api_version = new String("V2");  // Can be V2 or V3
    
       
    private static OSClient os;
      
    public VOSpaceBackendSwift() throws VOSpaceBackendException {
        
        OSFactory.enableHttpLoggingFilter(true);
                
        try {
            
            ConfigReader myConf = new ConfigReader(CONFIG_FILE_NAME);
        
            keystone_auth_url = myConf.getProperty("backend.swift.keystone.auth.url");     
            keystone_username = myConf.getProperty("backend.swift.keystone.username"); 
            keystone_password = myConf.getProperty("backend.swift.keystone.password"); 
            user_domain = myConf.getProperty("backend.swift.user.domain");    
            tmpStorageRoot = myConf.getProperty("fs.posix.tmp.storage.root");  
            swift_supported_api_version = myConf.getProperty("swift.supported.api.version");
                    
        } catch (Exception e) {
            log.debug("Unable to read properties file: " + CONFIG_FILE_NAME);
            log.debug("Use default values");
            keystone_auth_url = default_keystone_auth_url;
            keystone_username = default_keystone_username;
            keystone_password = default_keystone_password;
            user_domain = default_user_domain;
            tmpStorageRoot = default_tmpStorageRoot;
            swift_supported_api_version = default_swift_supported_api_version;
        }
        
        log.debug("Swift VOSpace Backend keystone_auth_url = " + keystone_auth_url);
        log.debug("Swift VOSpace Backend keystone_username = " + keystone_username);
        log.debug("Swift VOSpace Backend user_domain = " + user_domain);
        log.debug("Swift VOSpace Backend api version = " + swift_supported_api_version);
        
        if (swift_supported_api_version.compareToIgnoreCase("V3") == 0) {
        //if (this.swift_supported_api_version == "V3") {
            Identifier domainIdentifier = Identifier.byName("Default");
            try {
                log.debug("API V3 version");
                os = (OSClientV3)OSFactory.builderV3().withConfig(Config.newConfig().withSSLVerificationDisabled()) 
                       .endpoint(keystone_auth_url)
                       .credentials(keystone_username, keystone_password, domainIdentifier)
                       .perspective(Facing.ADMIN)
                       .authenticate();
            } catch (Exception e) {
                log.debug("ERROR!!!");               
                log.debug(e.getMessage());
                throw new VOSpaceBackendException("Unable to authenticate to swift service!");
            }            
        } else if (this.swift_supported_api_version.compareToIgnoreCase("V2") == 0) {
        //} else if (this.swift_supported_api_version == "V2") {
            
            try {
                log.debug("API V2 version");
                os = (OSClientV2)OSFactory.builderV2().withConfig(Config.newConfig().withSSLVerificationDisabled()) 
                  .endpoint(keystone_auth_url)
                  .credentials(keystone_username, keystone_password)
                  .tenantName("admin")
                  .authenticate();
            } catch (Exception e) {
                log.debug("API version not recognized. ERROR!!!");               
                log.debug(e.getMessage());
                throw new VOSpaceBackendException("Unable to authenticate to swift service!");
            }
            log.debug("Swift authentication successful. Happy end!");
             
        } else {
            
            throw new VOSpaceBackendException("Check api version. Can be only V2 or V3");
            
        }
        
    }
    
    public void fileFromTmpToFinalStorageArea(String tmpfile, String relPath) throws VOSpaceBackendException {
        
        File tmpFile = new File(this.getTmpPath() + tmpfile); 
        log.debug("tmp file received = " + tmpfile);
        log.debug("relative path received = " + relPath);

        String containerName = createRelativePathFromMd5checksum(relPath);
        containerName = containerName.replace("/", "_");
        String objectName = tmpfile;
       
        log.debug("object name = " + objectName);   
        log.debug("container name= " + containerName); 
        
        if (swift_supported_api_version.compareToIgnoreCase("V3") == 0) {
            
            throw new VOSpaceBackendException(" V3 api not supported yet");
           
        } else if (swift_supported_api_version.compareToIgnoreCase("V2") == 0) {        
            
            log.debug(" V2 api version");
            /*
            String etag = os.objectStorage().objects().put(containerName, objectName, 
                                               Payloads.create(tmpFile), 
                                               ObjectPutOptions.create()
                                               .path(relPath)
                                               );
            */
            ActionResponse resp = os.objectStorage().containers().create(containerName);
            //try {
            //    Thread.sleep(8000);
            //} catch (InterruptedException ex) {
            //    java.util.logging.Logger.getLogger(VOSpaceBackendSwift.class.getName()).log(Level.SEVERE, null, ex);
            // }
            if (resp.isSuccess()) {
                String etag = os.objectStorage().objects().put(containerName, objectName, Payloads.create(tmpFile));
                log.debug("Object creation successful");
            } else {
                System.out.println("Object creation failed");
                log.debug("Object creation failed");
            }
            
            
        } else {
            
            throw new VOSpaceBackendException("Problem with openstack Swift api version. Can be only V2 or V3");
            
        }

    }
    
    public String fileFromStorageAreaToTmp(String md5_sum, String storedFileName) throws VOSpaceBackendException {
        
        File tmpFile = new File(this.getTmpPath() + storedFileName); 
        String relPath = createRelativePathFromMd5checksum(md5_sum);
        String containerName = relPath.replace("/", "_");
        String objectName = storedFileName;
        
        if (swift_supported_api_version.compareToIgnoreCase("V3") == 0) {
            
           throw new VOSpaceBackendException(" V3 api not supported yet");
                   
        } else if (swift_supported_api_version.compareToIgnoreCase("V2") == 0) {           

            try {
                
                os.objectStorage().objects().download(containerName, objectName).writeToFile(tmpFile);
                
            } catch (IOException ioe) {
                
                throw new VOSpaceBackendException("IO Exception writing file in temporary storage area");
                
            }
            
        } else {
            
            throw new VOSpaceBackendException("Problem with openstack Swift api version. Can be only V2 or V3");
            
        }

        return tmpFile.getAbsolutePath();
    }
    
    
}
