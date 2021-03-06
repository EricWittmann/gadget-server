/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.gadgets.web.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.shindig.auth.AbstractSecurityToken;
import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Jeff Yu
 * @date: 12/04/12
 */
public class EncryptedBlobSecurityTokenService implements SecurityTokenService {
    private static Logger logger = LoggerFactory.getLogger(EncryptedBlobSecurityTokenService.class);

    public static final String EMBEDDED_KEY_PREFIX = "embedded:";
    public static final String CLASSPATH_KEY_PREFIX = "classpath:";

    private String container;
    private String domain;

    private BlobCrypter blobCrypter;


    public EncryptedBlobSecurityTokenService(String container, String domain, String encryptionKey) {
        this.container = container;
        this.domain = domain;

        try {
            File file = new File(encryptionKey);
            this.blobCrypter = new BasicBlobCrypter(FileUtils.readFileToString(file, "UTF-8"));
        } catch (IOException e) {
            throw new SecurityException("Unable to load encryption key from file: " + encryptionKey);
        }
    }

    public SecurityToken getSecurityToken(String appUrl, String moduleId, String userId) throws SecurityTokenException {
        return this.getBlobCrypterSecurityToken(appUrl, moduleId, userId);
    }

    
    public String getEncryptedSecurityToken(String appUrl, String moduleId, String userId) throws SecurityTokenException {
        String encryptedToken = null;

        try {
            BlobCrypterSecurityToken securityToken = this.getBlobCrypterSecurityToken(appUrl, moduleId, userId);
            encryptedToken = this.encryptSecurityToken(securityToken);
        } catch (Exception e) {
            throw new SecurityTokenException("Error creating security token from regionWidget", e);
        }

        return encryptedToken;
    }


    public SecurityToken decryptSecurityToken(String encryptedSecurityToken) throws SecurityTokenException {
        SecurityToken securityToken;

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Decrypting security token: " + encryptedSecurityToken);
            }

            //Remove the header container string and :
            encryptedSecurityToken = encryptedSecurityToken.substring((container + ":").length());

            //Decrypt
            Map<String, String> values = blobCrypter.unwrap(encryptedSecurityToken);
            securityToken = new BlobCrypterSecurityToken(container, domain, null, values);
        } catch (Exception e) {
            throw new SecurityTokenException("Error creating security token from encrypted string: " +
                    encryptedSecurityToken, e);
        }

        return securityToken;
    }

    public String refreshEncryptedSecurityToken(String encryptedSecurityToken) throws SecurityTokenException {
        //Decrypt the current token
        SecurityToken securityToken = this.decryptSecurityToken(encryptedSecurityToken);

        //Make sure the person is authorized to refresh this token
        String userId = null; //TODO: get userId from the Session
        if (!securityToken.getViewerId().equalsIgnoreCase(userId)) {
            throw new SecurityTokenException("Illegal attempt by user " + userId +
                    " to refresh security token with a viewerId of " + securityToken.getViewerId());
        }

        //Create and return the newly encrypted token
        return getEncryptedSecurityToken(securityToken.getAppUrl(), String.valueOf(securityToken.getModuleId()),
                securityToken.getViewerId());
    }

    private BlobCrypterSecurityToken getBlobCrypterSecurityToken(String appUrl, String moduleId, String userId)
            throws SecurityTokenException {

        Map<String, String> values = new HashMap<String, String>();
        values.put(AbstractSecurityToken.Keys.APP_URL.getKey(), appUrl);
        values.put(AbstractSecurityToken.Keys.MODULE_ID.getKey(), moduleId);
        values.put(AbstractSecurityToken.Keys.OWNER.getKey(), userId);
        values.put(AbstractSecurityToken.Keys.VIEWER.getKey(), userId);
        values.put(AbstractSecurityToken.Keys.TRUSTED_JSON.getKey(), "");

        BlobCrypterSecurityToken securityToken = new BlobCrypterSecurityToken(container, domain, null, values);

        if (logger.isTraceEnabled()) {
            logger.trace("Token created for regionWidget " + appUrl + " and user " + userId);
        }

        return securityToken;
    }

    private String encryptSecurityToken(BlobCrypterSecurityToken securityToken) throws SecurityTokenException {
        String encryptedToken = null;

        try {
            encryptedToken = container + ":" + blobCrypter.wrap(securityToken.toMap());
            if (logger.isTraceEnabled()) {
                logger.trace("Encrypted token created from security token: " + securityToken.toString() +
                        " -- encrypted token is: " + encryptedToken);
            }
        } catch (Exception e) {
            throw new SecurityTokenException("Error creating security token from person gadget", e);
        }

        return encryptedToken;
    }
}

