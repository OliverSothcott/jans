# SCIM Guide

## Overview

SCIM script allows you to execute custom logic when certain SCIM API operations are invoked.

With SCIM scripts, custom business logic can be executed when several of the SCIM API operations are invoked. This is useful in many situations, for example:

- Trigger notifications to external systems when certain resources are deleted
- Alter the actual responses the service produces
- Attach additional information to resources when they are created or updated
- Implement a fine-grained level of access to resources so different callers have permission to handle only a restricted number of resources

### Notes:

In this document, the term resources refer to those "entities" the service can manage, for instance, users or groups
The term operation refers to any SCIM functionality accessible through its HTTP endpoints
Basic development skills are assumed. Some grasp of Java and Python are required as well as understanding of the SCIM protocol.


## Interface

### Methods

**API Overview**

Custom scripts adhere to a simple API (ie. a well-defined collection of methods/routines) that is described in the following. It is advised to check the dummy script provided [here](https://raw.githubusercontent.com/GluuFederation/community-edition-setup/version_4.3.0/static/extension/scim/SampleScript.py) as you read this section.

**Scripts' config properties**

All methods contain a `configurationAttributes parameter`, this gives access to the configuration properties of the script itself. This is a relevant aspect of Gluu scripts: they are all parameterizable!.

`configurationAttributes` is a `java.util.Map<String, SimpleCustomProperty>` and [here](https://github.com/GluuFederation/oxCore/blob/version_4.3.0/oxUtil/src/main/java/org/gluu/model/SimpleCustomProperty.java) is how `SimpleCustomProperty` looks.

**Basic Methods**

These are methods not related to SCIM operations but still play key roles:
|Method Name|Description|Return Value|
|:---|:---|:---|
|`init`|Called when the (SCIM) service starts and every time the script properties or code changes|A boolean value describing success or failure|
|`destroy`|Called every time the script properties or code changes (called before `init`)|A boolean value describing success or failure|
|`getApiVersion`|Determines what methods are effectively called when SCIM endpoints are invoked|A positive integer|

**Pre-resource Modification**

They are called when the resource is about to be persisted. The second parameter in these methods hold the object that will be persisted to permanent storage, thus any change or manipulation upon the object will be reflected in the underlying database (as well as in the output of the SCIM operation call itself).

These methods are called regardless of the API version used. Names are self explanatory:

|Methods|2nd param|2nd param Class/Link|
|:---|:---|:---|
|`createUser`, `updateUser`, `deleteUser`|`user`|[ScimCustomPerson](https://github.com/GluuFederation/scim/blob/version_4.3.0/scim-model/src/main/java/org/gluu/oxtrust/model/scim/ScimCustomPerson.java)|
|`createGroup`, `updateGroup`, `deleteGroup`|`group`|[GluuGroup](https://github.com/GluuFederation/oxTrust/blob/version_4.3.0/model/src/main/java/org/gluu/oxtrust/model/GluuGroup.java)|

Pre-resource modification methods return a boolean. A `False` value aborts the corresponding SCIM operation and a 500 error is returned. The same applies if the method execution crashes at runtime.

Note that `update*` methods are called for both SCIM PUT and PATCH operations.

**Post-resource Modification**

They are called after the resource is persisted. The second parameter in these methods hold the object that was saved. Any change or manipulation upon the object will not be reflected in the underlying database, but may still modify the service response.

These methods are called if `getApiVersion` returns a number >= 2.

|Methods| 2nd param|2nd param Class/Link|
|:---|:---|:---|
|`postCreateUser`, `postUpdateUser`, `postDeleteUser`|`user`|[ScimCustomPerson](https://github.com/GluuFederation/scim/blob/version_4.3.0/scim-model/src/main/java/org/gluu/oxtrust/model/scim/ScimCustomPerson.java)|
|`postCreateGroup`, `postUpdateGroup`, `postDeleteGroup`|`group`|[GluuGroup](https://github.com/GluuFederation/oxTrust/blob/version_4.3.0/model/src/main/java/org/gluu/oxtrust/model/GluuGroup.java)|

Post-resource modification methods return a boolean. A `False` value aborts the corresponding SCIM operation and a 500 error is returned. The same applies if the method execution crashes at runtime.

Note that `postUpdate*` methods are called for both SCIM PUT and PATCH operations.

**Single Resource Retrieval**

These apply for SCIM operations that retrieve a resource by ID. They are called after the resource has been obtained from the database. The second parameter in these methods hold a reference to such object.

Any change or manipulation upon the object will not be reflected in the underlying database, but may still modify the service response.

These methods are called if `getApiVersion` returns a number >= 3 (available in Gluu 4.1 onwards).

|Methods|2nd param|2nd param Class/Link|
|:---|:---|:---|
|`getUser`|`user`|[ScimCustomPerson](https://github.com/GluuFederation/scim/blob/version_4.3.0/scim-model/src/main/java/org/gluu/oxtrust/model/scim/ScimCustomPerson.java)|
|`getGroup`|`group`|[GluuGroup](https://github.com/GluuFederation/oxTrust/blob/version_4.3.0/model/src/main/java/org/gluu/oxtrust/model/GluuGroup.java)|

Single resource retrieval methods return a boolean. A `False` value aborts the whole SCIM operation and a 500 error is returned. The same applies if the method execution crashes at runtime.

**Multiple Resources Retrieval**

These apply for SCIM search operations. They are called after the results have been obtained from the database. The second parameter in these methods hold a reference to such result set.

Any change or manipulation upon the object will not be reflected in the underlying database, but may still modify the service response.

These methods are called if `getApiVersion` returns a number >= 4 (available in Gluu 4.2 onwards).

|Methods|2nd param|2nd param Class/Link|
|:---|:---|:---|
|`postSearchUsers`|`results`|[PagedResult](https://github.com/GluuFederation/oxOrm/blob/version_4.3.0/core/src/main/java/org/gluu/persist/model/PagedResult.java)|
|`postSearchGroups`|`results`|[PagedResult](https://github.com/GluuFederation/oxOrm/blob/version_4.3.0/core/src/main/java/org/gluu/persist/model/PagedResult.java)|

Multiple resources retrieval methods return a boolean. A `False` value aborts the whole SCIM operation and a 500 error is returned. The same applies if the method execution crashes at runtime.

Note that searching using the root `.search` SCIM endpoint will trigger calls to both of the methods listed.

**Advanced control**

These are alternative methods that allow to tweak the response the service produces. They can be employed to introduce complex business rules when operations are executed.

These methods are called if getApiVersion returns a number >= 5 (available in Gluu 4.3 onwards).


### Objects

Definitions of all objects used in the script

## Common Use Cases

Descriptions of common use cases for this script, including a code snippet for each
