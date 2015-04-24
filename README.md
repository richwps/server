# RichWPS Server
Based on the open-geospatial standards RichWPS aims at providing simple orchestration means to domain experts. Therefore, the three components [RichWPS Server](https://github.com/richwps/server), [RichWPS SemanticProxy](https://github.com/richwps/semanticproxy) and [RichWPS ModelBuilder](https://github.com/richwps/modelbuilder) are introduced for faciliating the OWS-compliant creation, description and provision of orchestrated geospatial processes. 

## What it is
It is more than just a simple WPS server implementation, especially in the sense of ...

### architecture
The RichWPS Server is based on the [52°North Web Processing Service](http://github.com/52north/wps) implementation. It is able to receive and execute process models represented in [ROLA](http://github.com/richwps/dsl) scripts within an integrated orchestration engine. A process model can be seen as a workflow of one or more processes which are arranged in a sequential or branched way and represent a specific use case.

### transactional WPS
Process models can be received within a transactional WPS interface. It allows to store and register new processes permanently and also to unregister and delete them again.

### service orchestration
The orchestration engine is able to perform an automated orchestration of previously deployed ROLA scripts representing WPS process models. It can handle the execution of both, local WPS processes which are registered on the RichWPS server as well as of WPS processes on external servers and thus a real web service orchestration of WPS processes. To communicate with foreign WPS servers, the orchestration engine makes use of an integrated WPS client API.


### handling of data types
While the WPS specification does not cover datatypes in detail, it is extremly important to deal with type definitions as soon as you want to make real use of WPS processes. The 52° North architecture therefor provides a solid architectural base to fix type definitions and to extend its data handling capabilities. On top of that, the RichWPS server can offer information about its supported data types which is necessary to model and execute process models successfully.

### extended support for professionals
Not only in the phase of deploying and executing process models, but also in the phase of modelling the server supports a user who composes WPS process models. Therefor it is able to perform a test run in between of a fast deploy- and undeployment. The purpose of profiling a process model, which gives detailed information about the runtimes of the whole model and single processes contained in it, can be performed in a similar way.

## Why you should make use of it
* Benefit from the reusability of your geoprocessing units to cover your areas of application
* WPS is not a set of simple processes any more, in fact it is a whole orchestra.
* Make use of the modelling support the server provides, such as testing and profiling of workflows
* Extend the capabilities in data type support to your needs


## How you can use it
The RichWPS ModelBuilder, as the servers main client, supports all its functionalities on from the perspective of a client and offers them in a user friendly interface.

Technically the server provides the following capabilities sperarated on two HTTP interfaces:

### WPS interface
  * GetCapabilities ([request](http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_request.xsd) / [response](http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_response.xsd))
  * DescribeProcess ([request](http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd) / [response](http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_response.xsd))
  * Execute
  ([request](http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd) / [response](http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd))

### RichWPS interface
  * Deploy ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsDeployProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsDeployProcess_response.xsd))
  * UnDeploy ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsUndeployProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsUndeployProcess_response.xsd))
  * GetSupportedTypes ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsGetSupportedTypes_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsGetSupportedTypes_response.xsd))
  * TestProcess ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsTestProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsTestProcess_response.xsd))
  * ProfileProcess ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsProfileProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsProfileProcess_response.xsd))

## Go on and contribute!

See https://github.com/richwps/commons for shared libraries first.

Use git to clone the WPS repository:

git clone https://github.com/richwps/52n-wps.git

Then just run `mvn clean install -P with-geotools -Dwps.config.file=./52n-wps-webapp/src/main/webapp/config/wps_config.xml` on the repositories root directory.

__Eclipse WTP__

To deploy and run the server within Eclipse WTP refer to the hints at:
* https://github.com/52North/WPS#geotools
* https://github.com/52North/WPS#configure-at-runtime
