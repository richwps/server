# RichWPS Server #

The RichWPS Server is based on the [52°North Web Processing Service](http://github.com/52north/wps) implementation. It is able to receive and execute workflow models represented in [ROLA](http://github.com/richwps/dsl) scripts within an integrated orchestration engine. The server provides the following HTTP interfaces:

* WPS 1.0.0: OpenGIS specification 05-007r7 offering the following operations:
  * GetCapabilities
  * DescribeProcess
  * Execute
* RichWPS interface, offering the following operations
  * Deploy ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsDeployProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsDeployProcess_response.xsd))
  * UnDeploy ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsUndeployProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsUndeployProcess_response.xsd))
  * GetSupportedTypes ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsGetSupportedTypes_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsGetSupportedTypes_response.xsd))
  * TestProcess ([request](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsTestProcess_request.xsd) / [response](https://github.com/richwps/commons/blob/master/common-xml/52n-ogc-schema/src/main/resources/META-INF/xml/wps/1.0.0/wpsTestProcess_response.xsd))
  * ProfileProcess ([request]() / [response]())

## WPS interface ##

#### Server Capabilities ####
...

#### Describe a WPS process ####
...

#### Execute a WPS process ####
...

## RichWPS interface ##

#### Transactional WPS ####
...

#### Discover supported types ####
...

#### Test a WPS process ####
...

#### Profile a WPS process ####
...


## Orchestration engine ##

The orchestration engine interprets deployed ROLA scripts and executes the contained workflow in order to provide processing results withing the servers WPS 1.0.0 interface.

The interpreter has the following capabilities:
* keep an actual processing context to interpret references
* create process bindings according to the appropriate dsl elements
* do assignments
* execute local processes
* execute external processes making use of the RichWPS client API

## Development ##

See https://github.com/richwps/commons for shared libraries first.

Use git to clone the WPS repository:

git clone https://github.com/richwps/52n-wps.git

Then just run `mvn clean install -P with-geotools -Dwps.config.file=./52n-wps-webapp/src/main/webapp/config/wps_config.xml` on the repositories root directory.

__Eclipse WTP__

To deploy and run the server within Eclipse WTP refer to the hints at:
* https://github.com/52North/WPS#geotools
* https://github.com/52North/WPS#configure-at-runtime
