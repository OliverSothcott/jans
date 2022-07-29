# Introspection Script Guide

## Overview

OAuth reference tokens don't empirically convey any information. They are unguessable strings that meet the prescribed OAuth security guidelines for entropy. However, there is still a large amount of data that may be attached to a token, such as its current validity, approved scopes, and information about the context in which the token was issued. Access token data is 
essential for the resource server to evaluate policies that determine whether to allow the request. Token introspection enables a client to trade an OAuth reference token for its JSON equivalent by making a request per [RFC 7662](https://datatracker.ietf.org/doc/html/rfc7662). Introspection scripts allows to modify response of Introspection Endpoint [spec](https://datatracker.ietf.org/doc/html/rfc7662) and provide additional information in the JSON response.

## Interface

Introspection script should be associated with an OpenID Client (used for obtaining the token) in order to be run. Otherwise it's possible to set `introspectionScriptBackwardCompatibility` global Auth Server JSON Configuration Property to true, in this case Auth Server will run all scripts (ignoring client configuration).

### Methods

The introspection interception script extends the base script type with the `init`, `destroy` and `getApiVersion` methods and also adds the `modifyResponse` method:

| Method | Method description |
|:-----|:------|
| `def init(self, customScript, configurationAttributes)` | **Inherited Method** This method is only called once during the script initialization. It can be used for global script initialization, initiate objects etc |
| `def destroy(self, configurationAttributes)` | **Inherited Method** This method is called once to destroy events. It can be used to free resource and objects created in the `init()` method |
| `def getApiVersion(self, configurationAttributes, customScript)` | **Inherited Method** The getApiVersion method allows API changes in order to do transparent migration from an old script to a new API. Only include the customScript variable if the value for getApiVersion is greater than 10 |
| `def modifyResponse(self, responseAsJsonObject, context)` | This method is called after introspection response is ready. This method can modify introspection response.<br/>`responseAsJsonObject` is `org.codehaus.jettison.json.JSONObject`<br/> `context` is `io.jans.as.service.external.context.ExternalIntrospectionContext` |

The `configurationAttributes` parameter is `java.util.Map<String, SimpleCustomProperty>`.

### Snippet

    # Returns boolean, true - apply introspection method, false - ignore it.
    # This method is called after introspection response is ready. This method can modify introspection response.
    # Note :
    # responseAsJsonObject - is org.codehaus.jettison.json.JSONObject, you can use any method to manipulate json
    # context is reference of io.jans.as.server.service.external.context.ExternalIntrospectionContext (in https://github.com/JanssenProject project)
    def modifyResponse(self, responseAsJsonObject, context):
        responseAsJsonObject.accumulate("key_from_script", "value_from_script")
        return True
        
**Note - The preferred way to modify an access token is with the Update Token script.**

It is also possible to run introspection script during `access_token` creation as JWT. It can be controlled by `run_introspection_script_before_access_token_as_jwt_creation_and_include_claims` OpenID Client property which is set to false by default. 

If OpenID Client properties `run_introspection_script_before_access_token_as_jwt_creation_and_include_claims` and `access_token_as_jwt` are set to true then introspection script will be ran before JWT (`access_token`) is created and all JSON values will be transfered to JWT. Also `context` inside script has additional method which allows to cancel transfering of claims if needed `context.setTranferIntrospectionPropertiesIntoJwtClaims(false)`
        
## Common Use Cases

## Script Type: Python

### Retrieve Grant, Session and User Details from Access Token

Following sample code snippet shows how to work backwards from an AccessToken to Grant, Session and User information.

    from io.jans.model.custom.script.type.introspection import IntrospectionType
    from io.jans.as.server.service import SessionIdService
    from io.jans.service.cdi.util import CdiUtil

    class Introspection(IntrospectionType):
        def __init__(self, currentTimeMillis):
            self.currentTimeMillis = currentTimeMillis

        def init(self, customScript, configurationAttributes):
            print "Introspection script. Initializing ..."
            print "Introspection script. Initialized successfully"

            return True

        def destroy(self, configurationAttributes):
            print "Introspection script. Destroying ..."
            print "Introspection script. Destroyed successfully"
            return True

        def getApiVersion(self):
            return 11

        # Returns boolean, true - apply introspection method, false - ignore it.
        # This method is called after introspection response is ready. This method can modify introspection response.
        # Note :
        # responseAsJsonObject - is org.codehaus.jettison.json.JSONObject, you can use any method to manipulate json
        # context is reference of io.jans.as.server.service.external.context.ExternalIntrospectionContext (in https://github.com/JanssenProject project, )
        def modifyResponse(self, responseAsJsonObject, context):
            authorizationGrant = context.getTokenGrant()
            if authorizationGrant is None:
                print "Introspection. Failed to load authorization grant by context"
                return False

            # Put user_id into response
            responseAsJsonObject.accumulate("user_id", authorizationGrant.getUser().getUserId())

            # Put custom parameters into response
            sessionDn = authorizationGrant.getSessionDn()
            if sessionDn is None:
                # There is no session
                print "Introspection. Failed to load session DN"
                return True

            # Return session_id
            responseAsJsonObject.accumulate("session_id", sessionDn)

            sessionIdService = CdiUtil.bean(SessionIdService)
            session = sessionIdService.getSessionById(sessionDn, False)
            if session is None:
                print "Introspection. Failed to load session '%s'" % sessionDn
                return True

            sessionAttributes = session.getSessionAttributes()
            if sessionAttributes is None:
                # There is no session attributes
                return True

            # Append custom claims
            if sessionAttributes.containsKey("custom1"):
                responseAsJsonObject.accumulate("custom1", sessionAttributes.get("custom1"))
            if sessionAttributes.containsKey("custom2"):
                responseAsJsonObject.accumulate("custom2", sessionAttributes.get("custom2"))

            return True

## Script Type: Java

### Retrieve Grant, Session and User Details from Access Token

Following sample code snippet shows how to work backwards from an AccessToken to Grant, Session and User information.

    import io.jans.model.SimpleCustomProperty;
    import io.jans.model.custom.script.model.CustomScript;
    import io.jans.model.custom.script.type.introspection.IntrospectionType;
    import io.jans.service.custom.script.CustomScriptManager;
    import io.jans.as.server.service.external.context.ExternalIntrospectionContext;
    import io.jans.as.server.model.common.AuthorizationGrant;
    import io.jans.as.server.service.SessionIdService;
    import io.jans.service.cdi.util.CdiUtil;
    import io.jans.as.server.model.common.SessionId;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.json.JSONObject;

    import java.util.Map;


    public class Introspection implements IntrospectionType {

        private static final Logger log = LoggerFactory.getLogger(Introspection.class);
        private static final Logger scriptLogger = LoggerFactory.getLogger(CustomScriptManager.class);

        @Override
        public boolean init(Map<String, SimpleCustomProperty> configurationAttributes) {
            log.info("Init of Introspection Java custom script");
            return true;
        }

        @Override
        public boolean init(CustomScript customScript, Map<String, SimpleCustomProperty> configurationAttributes) {
            log.info("Init of Introspection Java custom script");
            scriptLogger.info("Introspection Java script. Initializing ...");
            scriptLogger.info("Introspection Java script. Initialized successfully");
            return true;
        }

        @Override
        public boolean destroy(Map<String, SimpleCustomProperty> configurationAttributes) {
            log.info("Destroy of Introspection Java custom script");
            scriptLogger.info("Introspection Java script. Destroying ...");
            scriptLogger.info("Introspection Java script. Destroyed successfully");
            return true;
        }

        @Override
        public int getApiVersion() {
            return 11;
        }

        // Returns boolean, true - apply introspection method, false - ignore it.
        // This method is called after introspection response is ready. This method can modify introspection response.
        // Note :
        // responseAsJsonObject - is org.json.JSONObject, you can use any method to manipulate json
        // context is reference of io.jans.as.server.service.external.context.ExternalIntrospectionContext (in https://github.com/JanssenProject project, )

        @Override
        public boolean modifyResponse(Object responseAsJsonObject, Object context) {

            JSONObject response = (JSONObject) responseAsJsonObject;
            ExternalIntrospectionContext ctx = (ExternalIntrospectionContext) context;

            response.accumulate("key_from_java", "value_from_script_on_java");

            AuthorizationGrant authorizationGrant = ctx.getTokenGrant();
            if(authorizationGrant == null) {
                scriptLogger.info("Introspection Java script. Failed to load authorization grant by context");
                return false;
            }

            // Put user_id into response
            response.accumulate("user_id", authorizationGrant.getUser().getUserId()); 

            // Put custom parameters into response
            String sessionDn = authorizationGrant.getSessionDn();
            if(sessionDn == null) {
                // There is no session
                scriptLogger.info("Introspection Java script. Failed to load session DN");
                return true;
            }

            // Return session_id
            response.accumulate("session_id", sessionDn);

            SessionIdService sessionIdService = CdiUtil.bean(SessionIdService.class);
            SessionId session = sessionIdService.getSessionById(sessionDn, false);
            if(session == null) {
                scriptLogger.info("Introspection Java script. Failed to load session");
                return true;
            }

            Map<String, String> sessionAttributes = session.getSessionAttributes();
            if(sessionAttributes == null) {
                // There is no session attributes
                return true;
            }

            // Append custom claims
            if(sessionAttributes.containsKey("custom1")) {
                response.accumulate("custom1", sessionAttributes.get("custom1"));
            }
            if(sessionAttributes.containsKey("custom2")) {
                response.accumulate("custom2", sessionAttributes.get("custom2"));
            }

            return true;
        }
    }
