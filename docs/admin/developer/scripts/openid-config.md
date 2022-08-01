# OpenID Script Guide

## Overview



## Interface



### Methods

The introspection interception script extends the base script type with the methods:-
- `init`, 
- `destroy`
- `getApiVersion` 

It also adds the `modifyResponse` method.

| Method | Method description |
|:-----|:------|
| `def init(self, customScript, configurationAttributes)` | **Inherited Method** This method is only called once during the script initialization. It can be used for global script initialization, initiate objects etc |
| `def destroy(self, configurationAttributes)` | **Inherited Method** This method is called once to destroy events. It can be used to free resource and objects created in the `init()` method |
| `def getApiVersion(self, configurationAttributes, customScript)` | **Inherited Method** The `getApiVersion` method allows API changes in order to do transparent migration from an old script to a new API. **NOTE**: - Only include the customScript variable if the value for `getApiVersion` is greater than 10 |
| `def modifyResponse(self, responseAsJsonObject, context)` | This method is called after the introspection response is ready. This method can modify the introspection response.<br/>`responseAsJsonObject` is `org.codehaus.jettison.json.JSONObject`<br/> `context` is `io.jans.as.service.external.context.ExternalIntrospectionContext` |

The `configurationAttributes` parameter is `java.util.Map<String, SimpleCustomProperty>`.

### Snippet

    # Returns boolean, true - apply introspection method, false - ignore it.
    # This method is called after introspection response is ready. This method can modify the introspection response.
    # Note :
    # responseAsJsonObject - is org.codehaus.jettison.json.JSONObject, you can use any method to manipulate json
    # context is reference of io.jans.as.server.service.external.context.ExternalIntrospectionContext (in https://github.com/JanssenProject project)
    def modifyResponse(self, responseAsJsonObject, context):
        responseAsJsonObject.accumulate("key_from_script", "value_from_script")
        return True
        
        
## Common Use Cases

## Script Type: Python

### Retrieve Grant, Session and User Details from Access Token

The following sample code snippet shows how to work backwards from an AccessToken to Grant, Session and User information.

    

## Script Type: Java

### Retrieve Grant, Session and User Details from Access Token

The following sample code snippet shows how to work backwards from an AccessToken to Grant, Session and User information.

    
