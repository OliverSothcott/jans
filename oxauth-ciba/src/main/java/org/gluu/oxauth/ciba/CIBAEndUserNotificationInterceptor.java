/*
 * oxAuth-CIBA is available under the Gluu Enterprise License (2019).
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.ciba;

import org.gluu.oxauth.client.fcm.FirebaseCloudMessagingClient;
import org.gluu.oxauth.client.fcm.FirebaseCloudMessagingRequest;
import org.gluu.oxauth.client.fcm.FirebaseCloudMessagingResponse;
import org.gluu.oxauth.interception.CIBAEndUserNotificationInterception;
import org.gluu.oxauth.interception.CIBAEndUserNotificationInterceptionInterface;
import org.gluu.oxauth.model.configuration.AppConfiguration;
import org.gluu.oxauth.service.EncryptionService;
import org.gluu.oxauth.util.RedirectUri;
import org.gluu.util.security.StringEncrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.UUID;

import static org.gluu.oxauth.model.authorize.AuthorizeRequestParam.*;

/**
 * @author Javier Rojas Blum
 * @version March 4, 2020
 */
@Interceptor
@CIBAEndUserNotificationInterception
@Priority(Interceptor.Priority.APPLICATION)
public class CIBAEndUserNotificationInterceptor implements CIBAEndUserNotificationInterceptionInterface, Serializable {

    private final static Logger log = LoggerFactory.getLogger(CIBAEndUserNotificationInterceptor.class);

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private EncryptionService encryptionService;


    public CIBAEndUserNotificationInterceptor() {
        log.info("CIBA End-User Notification Interceptor loaded.");
    }

    @AroundInvoke
    public Object notifyEndUser(InvocationContext ctx) {
        log.debug("CIBA: notifying end-user...");

        try {
            String scope = (String) ctx.getParameters()[0];
            String acrValues = (String) ctx.getParameters()[1];
            String authReqId = (String) ctx.getParameters()[2];
            String deviceRegistrationToken = (String) ctx.getParameters()[3];
            notifyEndUser(scope, acrValues, authReqId, deviceRegistrationToken);
            ctx.proceed();
        } catch (Exception e) {
            log.error("Failed to process CIBA support.", e);
        }

        return true;
    }

    @Override
    public void notifyEndUser(String scope, String acrValues, String authReqId, String deviceRegistrationToken) {
        String clientId = appConfiguration.getBackchannelClientId();
        String redirectUri = appConfiguration.getBackchannelRedirectUri();
        String url = appConfiguration.getCibaEndUserNotificationConfig().getNotificationUrl();
        String key = encryptionService.decrypt(appConfiguration.getCibaEndUserNotificationConfig()
                .getNotificationKey(), true);
        String to = deviceRegistrationToken;
        String title = "oxAuth Authentication Request";
        String body = "Client Initiated Backchannel Authentication (CIBA)";

        RedirectUri authorizationRequestUri = new RedirectUri(appConfiguration.getAuthorizationEndpoint());
        authorizationRequestUri.addResponseParameter(CLIENT_ID, clientId);
        authorizationRequestUri.addResponseParameter(RESPONSE_TYPE, "id_token");
        authorizationRequestUri.addResponseParameter(SCOPE, scope);
        authorizationRequestUri.addResponseParameter(ACR_VALUES, acrValues);
        authorizationRequestUri.addResponseParameter(REDIRECT_URI, redirectUri);
        authorizationRequestUri.addResponseParameter(STATE, UUID.randomUUID().toString());
        authorizationRequestUri.addResponseParameter(NONCE, UUID.randomUUID().toString());
        authorizationRequestUri.addResponseParameter(PROMPT, "consent");
        authorizationRequestUri.addResponseParameter(AUTH_REQ_ID, authReqId);

        String clickAction = authorizationRequestUri.toString();

        FirebaseCloudMessagingRequest firebaseCloudMessagingRequest = new FirebaseCloudMessagingRequest(key, to, title, body, clickAction);
        FirebaseCloudMessagingClient firebaseCloudMessagingClient = new FirebaseCloudMessagingClient(url);
        firebaseCloudMessagingClient.setRequest(firebaseCloudMessagingRequest);
        FirebaseCloudMessagingResponse firebaseCloudMessagingResponse = firebaseCloudMessagingClient.exec();

        log.debug("CIBA: firebase cloud messaging result status " + firebaseCloudMessagingResponse.getStatus());
    }
}