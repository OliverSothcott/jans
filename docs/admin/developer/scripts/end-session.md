# End Session (Logout) Guide

## Overview

End Session scripts allows to modify HTML response for Frontchannel logout [(spec)](https://openid.net/specs/openid-connect-frontchannel-1_0.html)

## Interface

### Methods

The End Session interception script extends the base script type with the `init`, `destroy` and `getApiVersion` methods but also adds the following method(s):

|Method|def getFrontchannelHtml(self, context)|
|:---|:---|
|Method Parameter|`context` is `org.gluu.oxauth.service.external.context.EndSessionContext`|

### Snippet

    # Returns string, it must be valid HTML (with iframes according to spec http://openid.net/specs/openid-connect-frontchannel-1_0.html)
    # This method is called on `/end_session` after actual session is killed and oxauth construct HTML to return to RP.
    # Note :
    # context is reference of org.gluu.oxauth.service.external.context.EndSessionContext (in https://github.com/GluuFederation/oxauth project, )
    def getFrontchannelHtml(self, context):
        return ""

Full version of the script example can be found [here.](https://github.com/GluuFederation/community-edition-setup/blob/version_4.3.0/static/extension/end_session/end_session.py)

### Objects

Definitions of all objects used in the script

## Common Use Cases

Descriptions of common use cases for this script, including a code snippet for each
