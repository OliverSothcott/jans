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
|Method|Description|Return Value|
|:---|:---|:---|
|`init`|Called when the (SCIM) service starts and every time the script properties or code changes|A boolean value describing success or failure|
|`destroy`|Called every time the script properties or code changes (called before `init`)|A boolean value describing success or failure|
|`getApiVersion`|Determines what methods are effectively called when SCIM endpoints are invoked|A positive integer|



### Objects

Definitions of all objects used in the script

## Common Use Cases

Descriptions of common use cases for this script, including a code snippet for each
