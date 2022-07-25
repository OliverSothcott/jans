# Revoke Token Script Guide

## Overview

The Token Revocation extension defines a mechanism for clients to indicate to the authorization server that an access token is no longer needed. This is used to enable a "log out" feature in clients, allowing the authorization server to clean up any security credentials associated with the authorization.

 A token is a string representing an authorization grant issued by the resource owner to the client.  A revocation request will invalidate the actual token and, if applicable, other tokens based on the same authorization grant and the authorization grant itself.

   From an end-user's perspective, OAuth is often used to log into a certain site or application.  This revocation mechanism allows a client to invalidate its tokens if the end-user logs out, changes identity, or uninstalls the respective application.  Notifying the authorization server that the token is no longer needed allows the authorization server to clean up data associated with that token (e.g., session data) and the underlying authorization grant.  This behavior prevents a situation in which there is still a valid authorization grant for a particular client of which the end-user is not aware.  This way, token revocation prevents abuse of abandoned tokens and facilitates a better end-user experience since invalidated authorization grants will no longer turn up in a list of authorization grants the authorization server might present to the end-user.
   
**Token Revocations**

  Implementations MUST support the revocation of refresh tokens and SHOULD support the revocation of access tokens.

   The client requests the revocation of a particular token by making an HTTP POST request to the token revocation endpoint URL.  This URL MUST conform to the rules given in RFC6749, Section 3.1. Clients MUST verify that the URL is an HTTPS URL.

   The means to obtain the location of the revocation endpoint is out of the scope of this specification.  For example, the client developer may consult the server's documentation or automatic discovery may be used.  As this endpoint is handling security credentials, the endpoint location needs to be obtained from a trustworthy source.

Since requests to the token revocation endpoint result in the transmission of plaintext credentials in the HTTP request, URLs for token revocation endpoints MUST be HTTPS URLs.  The authorization server MUST use Transport Layer Security (TLS) [RFC5246] in a version compliant with [RFC6749], Section 1.6.  Implementations MAY also support additional transport-layer security mechanisms that meet their security requirements.

   If the host of the token revocation endpoint can also be reached over HTTP, then the server SHOULD also offer a revocation service at the corresponding HTTP URI, but it MUST NOT publish this URI as a token revocation endpoint.  This ensures that tokens accidentally sent over HTTP will be revoked.

## Interface

### Methods

    # This method is called during Revoke Token call.
    # If True is returned, token is revoked. If False is returned, revoking is skipped.
    # Note :
    # context is reference of org.gluu.oxauth.service.external.context.RevokeTokenContext(in https://github.com/GluuFederation/oxauth project, )
    def revoke(self, context):
        return True
        
Full version of the script example can be found [here.](https://github.com/GluuFederation/community-edition-setup/blob/version_4.3.0/static/extension/revoke_token/revoke_token.py)

**Note** `RevokeTokenContext` allows to access response builder (`context.getResponseBuilder()`) which allows to customer response if needed.        

### Objects

## Common Use Cases

Descriptions of common use cases for this script, including a code snippet for each
