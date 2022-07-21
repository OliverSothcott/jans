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

These methods are called if `getApiVersion` returns a number >= 5 (available in Gluu 4.3 onwards).

|Methods|
|:---|
|`manageResourceOperation`
|`manageSearchOperation`

[Later](#controlling-execution-of-scim-operations) we'll revisit the topic of access and provide concrete examples.

## Developer's First Steps

As with any other custom script in Gluu, SCIM scripts are run by the Jython engine. This allows usage of Python-style coding and leverages all the Java classes available in the SCIM web application classpath.

To start, ensure SCIM service is running and a protection mode has been configured. Then in oxTrust enable the sample script:

1. Navigate to `Configuration` > `Other custom Scripts` > `SCIM`

2. Expand the only row (labeled `scim_event_handler`)

3. Click on the `Enabled` check box

4. Click on `update` at the bottom of the page

<DIAGRAM GOES HERE>

Inspect (`tail`) `scim_script.log`. After some seconds you'll see some lines related to script initialization, ie. method init being called. This means your script is active and properly running. Click [here](https://gluu.org/docs/gluu-server/4.3/user-management/scim2/#where-to-locate-scim-related-logs) to learn more about SCIM logs.

Learning by doing is a good approach to scripting in Gluu. To start, edit the script by adding some `print` statements to the following methods: `createUser`, `postCreateUser`, `manageResourceOperation`. Save the script and check the log, wait until destroy and init are called.

Send a user creation request to the service. [Here](https://gluu.org/docs/gluu-server/4.3/user-management/scim2/#creating-resources) is an example. Note the order in which your prints appear in the log.

Delete the user created (whether via oxTrust or SCIM itself). Edit the `return` value for each of the three methods one by one and issue a creation request after every script update. Inspect the log output as well as the service outputs, namely, the HTTP responses.

Now that you have got some acquaintance with the edit/test cycle, we can proceed with an example.
  
### Example: Modifying Search Results

SCIM spec defines the concept of attribute returnability where some attributes should be never be part of a response (like passwords), always be returned (like resource identifiers), or be returned by default unless otherwise stated by the `excludedAttributes` parameter.

Assume you are maintaining a user base of secret agents that work for your company and need to avoid exposing information such as their physical addresses for safety reasons. To keep it simple let's restrict the scope to user searches only. In practice you should take steps to hide this data on user retrieval and update.

Let's alter `postSearchUsers`'s second parameter (`results`) to ensure addresses are not leaked:  
  
`  for user in results.getEntries():
    user.setAttribute("oxTrustAddresses", None)`
  
This is very straightforward code except for the usage of `oxTrustAddresses`. Shouldn't it be simply `addresses` as the known SCIM attribute?

Scripts work with entities that are about to be persisted or have already been saved so they kind of resemble the database structure (schema in LDAP terms). It turns out that database attribute names rarely match with SCIM names. An explanation of this fact can be found [here](https://gluu.org/docs/gluu-server/4.3/user-management/scim2/#how-is-scim-data-stored).

While it is easy to know the SCIM name of a database attribute, the converse requires checking the code, however since you already have the skill this shouldn't be a problem: in [this](https://github.com/GluuFederation/scim/blob/version_4.3.0/scim-model/src/main/java/org/gluu/oxtrust/model/scim2/user/UserResource.java) Java class you'll find the representation of a user resource in SCIM spec terms. Pay attention to the `addresses` field and its associated `StoreReference` annotation that contains the attribute where addresses are actually stored.

With that said, save your modifications. You may like the idea of adding some prints for enlightment like:
  
`print "%d entries returned of %d" % (results.getEntriesCount(), results.getTotalEntriesCount())
for user in results.getEntries():
    print "Flushing addresses for user %s" % getUid() 
    user.setAttribute("oxTrustAddresses", None)`
  
Ensure no addresses are returned anymore in your SCIM user searches. Happy testing!

### Controlling execution of SCIM operations

### Objects

Definitions of all objects used in the script

## Common Use Cases

Descriptions of common use cases for this script, including a code snippet for each
