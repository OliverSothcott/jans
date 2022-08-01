# ID Generator Guide

## Overview

The ID generation script enables an admin to implement custom ID generation rules.

## Interface

### Methods

By default oxAuth/oxTrust uses an internal method to generate unique identifiers for new person/client, etc. entries. In most cases the format of the ID is:

`'!' + idType.getInum() + '!' + four_random_HEX_characters + '.' + four_random_HEX_characters.`

This script type adds only one method to the base script type:

|Method|def generateId(self, appId, idType, idPrefix, configurationAttributes)|
|:---|:---|
|Method Parameter|`appId` is application ID <br/>`idType` is ID Type <br/> `idPrefix` is ID Prefix <br/>`user` is `org.gluu.oxtrust.model.GluuCustomPerson` <br/> `configurationAttributes` is `java.util.Map<String, SimpleCustomProperty>`

**Note:** This script can be used in an oxTrust application only.

- [Sample ID Generation Script](https://gluu.org/docs/gluu-server/4.3/admin-guide/sample-id-generation-script.py)


### Objects

Definitions of all objects used in the script

## Common Use Cases

Descriptions of common use cases for this script, including a code snippet for each
