# RichWPS Server

The RichWPS Server is based on the [52°North Web Processing Service](http://github.com/52north/wps) implementation. It is able to receive and execute workflow models represented in [ROLA](http://github.com/richwps/dsl) scripts within an integrated orchestration engine. The server provides the following interfaces to the RichWPS ModelBuilder:

* WPS 1.0.0: OpenGIS specification 05-007r7
* WPS-T: deploy and undeploy operation
* WPS-Debug (under development)
* WPS-Profile (under development)

## WPS-T interface

The integrated WPS-T interfaces is based on a former 52north implementation by Schäffer. It is extended by a specific ROLA Deployment Profile with the following XML schema description: (under development).

Deployment of a ROLA process:
{code}

Undeployment of a previously deployed ROLA process:
{code}

## Orchestration engine

The orchestration engine interprets deployed ROLA scripts and executes the contained workflow in order to provide processing results withing the servers WPS 1.0.0 interface.

The interpreter has the following capabilities:
* keep an actual processing context to interpret references
* create process bindings according to the appropriate dsl elements
* do assignments
* execute local processes
* execute external processes making use of the RichWPS client API

## Development

See https://github.com/richwps/commons for shared libraries first.

Use git to clone the WPS repository:

git clone https://github.com/richwps/52n-wps.git

Then just run `mvn clean install -P with-geotools -Dwps.config.file=./52n-wps-webapp/src/main/webapp/config/wps_config.xml` on the repositories root directory.

__Eclipse WTP__

To deploy and run the server within Eclipse WTP refer to the hints at:
* https://github.com/52North/WPS#geotools
* https://github.com/52North/WPS#configure-at-runtime
