
# Object Memory Server (OMS)
Object Memories allow physical artefacts to keep a history of their statuses, interactions and changes over a lifetime. With the growing availability of easy-to-deploy and cheap data storages such as RFID or NFC chips such a refinement of products has become comparatively affordable and possible applications are starting to arise. Whether a product's temperature is to be monitored, yielded emission, changing components or manipulations issued by other artefacts, in an object memory all relevant data can be stored and accessed at will. 

This repository contains all necessary Java source files for a working Object Memory Server. 

---

1 [How to use this code](#ch1)

2 [How to use the OMS](#ch2)

---

## 1 <a name="ch1"> How to use this code </a>
The repository requires sources which can be found in the **libOMM** project, freely available in the [libOMM repository](https://github.com/dfki-iui/libomm). 

In **libomm** you will find classes modelling object memories and their contents as defined in the Incubator Group's [Final Report](http://www.w3.org/2005/Incubator/omm/XGR-omm-20111026/). These are imported in many classes of the **oms** project which, in turn, provides classes to run and access the Object Memory Server.

### 1.1 Requirements
Apart from the Java JDK Version 8 Update 45 or higher (downloadable at http://java.com/) the projects depend on some external libraries to run the OMS. The project utilises Maven, so everything necessary can be found in the POM files. However, the following table provides an overview of all libraries needed to use the object memory projects.

| Library | Version| 
| :------- | :------: |
| [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/download_cli.cgi) | 1.2 |
| [Apache Commons Codec]( https://commons.apache.org/proper/commons-codec/download_codec.cgi) | 20041127.091804|
| [Apache Commons IO](https://commons.apache.org/proper/commons-io/download_io.cgi) | 2.4 |
| [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/download_lang.cgi) | 3.3.2|
| [Apache Commons Validator](https://commons.apache.org/proper/commons-validator/download_validator.cgi) | 1.4.0|
| [Apache Log4j]( https://logging.apache.org/log4j/1.2/download.html) | 1.2.17|
| [Apache Santuario](http://santuario.apache.org/download.html) | 2.0.3|
| [Eclipse Jetty](http://download.eclipse.org/jetty/) | 9.3.0.M2|
| [ez-vcard](https://code.google.com/p/ez-vcard/wiki/Downloads) | 0.9.6 |
| [Jackson Core ASL](http://grepcode.com/snapshot/repo1.maven.org/maven2/org.codehaus.jackson/jackson-core-asl/1.9.13) | 1.9.13 |
| [Jackson Mapper ASL](http://grepcode.com/snapshot/repo1.maven.org/maven2/org.codehaus.jackson/jackson-mapper-asl/1.9.13) | 1.9.13 |
| [Jetty ALPN](https://github.com/jetty-project/jetty-alpn) | 8.1.3.v20150130 |
| [JSON](https://github.com/douglascrockford/JSON-java) | 20141113 |
| [JSoup](http://jsoup.org/download) | 1.7.3 |
| [JUnit]( https://github.com/junit-team/junit) | 4.12 |
| [Restlet]( http://restlet.com/downloads/current/) | 3.0-M1 |
| [SLF4J](http://www.slf4j.org/download.html) | 1.7.10 |
Make sure to download and import all necessary libraries manually if you are not using Maven to handle dependencies.

### 1.2 Getting started
The classes contained in this repository are capable of running an Object Memory Server that starts empty but can be filled with object memories via the interfaces described in [How to use the OMS](#ch3). You may use the graphical web interface to accustom yourself to the object memory format and possibilities manually, or just dive in using the REST interface, provided snippets and your own code. 

In order to fully capitalise on these libraries, however, you will most likely need to write your own clients and other applications in a language and environment of your choice to map your real world circumstances to digital memories and vice versa, thus creating an elaborate cyberphysical environment.  

## 2 <a name="ch2">How to use the OMS</a>
### 2.1 Starting the server
In order to work with object memories the server has to be available for access. It is started by running or creating a new instance of `de.dfki.oms.main.OMSStarter`. The server then displays some basic output about its status which should look similar to this (paths and memories may vary) and includes the current XML files for all memories and the started web interfaces:
```
Setting up ADOM instances.
Creating adom for memory: sample
CurrentFile: C:\Users\myuser\OMS\resources\memories\sample\v1.xml
* OMS Web Application ('/web')
* OMS Query ('/query')
* RESTlet ('/rest')
* Management ('/mgmt')
Memory path: C:\Users\myuser\OMS\resources\memories
```
If started locally, the OMS will be available on your local machine. It listens on port `10082` by default. This ports can be changed by adding the required port number as a parameter to the call. The table below shows all parameters that may be used when calling the OMSStarter.

| Parameter | Short Paramter | Effect | 
| :------- | :------ | :------ |
| --port [port] | -p [port]  | the port on which this instance will listen | 
| --disable-history | -dh  | disables OMS history feature (no backup of old memories) | 
| --ip-address | -ip | displays and outputs all OMS-URLs with IP addresses instead of DNS names | 
| --upnp | | enables the UPnP service for memory detection | 
| --additionalServer [server-name] | -as [server-name] | adds additional server names (e.g., the server is accessible with an address other than the server name) |

### 2.2 OMS interfaces
Data on the Object Memory Server can be accessed in various ways. First, it is possible to use Java code, utilising the classes provided in the **libOMM** repository to create and manipulate object memory implementations. Its own README.md explores the possibilities in depth. 

Additionally there are OMS interfaces which may be accessed with external software such as web browsers or making use of other programming languages than Java. The most common of these interfaces are a **web application** and a **RESTful node collection** representing the server's contents. 

The **web** application can be accessed by a standard web browser by browsing to *http://localhost:10082/web/* (assuming the OMS runs locally and on the default port 10082). It lets you create and edit memories and their contents providing a simple graphical interface based on HTML. It is an easy and quick way to view and manage data manually, but not convenient for non-human agents who want to manipulate the server's contents. 

The **REST** interface is a more versatile way to manage data on the OMS. Any application which can send HTTP commands to an URL can use it, exposing the server to other applications, whether they already exist or are tailored to use with object memories. In fact, the Java classes themselves essentially make use of this interface by utilising [ClientResources](http://restlet.com/technical-resources/restlet-framework/javadocs/2.1/jee/api/org/restlet/resource/ClientResource.html?is-external=true) for most OMS actions internally. 

The REST nodes form a tree that follows the OMS' hierarchical structure, with its root being the server's full address, followed by the REST node itself. A RESTful call to the OMS may look like this: ` http://localhost:10082/rest/myMemory/st/block/2/payload`. 

The following list depicts the REST tree and possibilities to access its nodes. There is mostly either reading (GET) or writing access (PUT and POST are almost always used interchangeably) but some nodes support both methods or none. The DELETE command will work on certain nodes as well, as mentioned in the list, but return 405 (Not Allowed) everywhere else.

* __/rest__ The REST interface's root node. Any HTTP command sent here will return a 404 error.
    * __/{memoryName}__ A specific memory's root. One such node exists for every object memory on the server. GET will return a JSON description of the requested memory, POST and PUT are not allowed (405). DELETE will remove the memory from the OMS if possible (disabled deletion can prevent this, leading to a 403 error) and return a short message about the operation's success. 
        * __/st__ The memory's Storage node. No requests allowed (405). See below for a description of the full subtree under this node.
        * __/mgmt__ The memory's Management node. No requests yield anything here, though they return OK (200), only PUT is not allowed (405). See below for a description of the full subtree under this node.


The Storage subtree contains all the memory's contents. It is structured as follows:

* __/st__ 
    * __/toc__ The Table Of Contents for this memory. GET returns the TOC as an XML document, POST and PUT are not allowed (405). 
    * __/block__ An access points to the memory's blocks. GET and PUT are not allowed (405), POST can be used to add new blocks to the memory which have to be sent in XML format (See [appendix 1](#app1)).
        * __/{id}__ Each block has a certain ID through which it can be reached. No HTTP commands are supported by these nodes, as they only serve as entry points.
            * __/meta__ Grants access to the block's meta information (such as primary ID, namespace or title). GET returns the meta information in XML format (if the request's header's accept field contains an entry for JSON, the meta information will be returned in JSON format). POST and PUT are not allowed (405).
                * __/json__ A GET on this node will return the meta information in JSON format, regardless of any header information. POST and PUT are not allowed (405).
                * __/xml__ A GET on this node will return the meta information in XML format, regardless of any header information. POST and PUT are not allowed (405).
            * __/payload__ Grants access to the block's payload. GET returns the payload as a plain text, POST and PUT add a new or overwrite an existing payload. DELETE removes the payload from this block.
                * __/append__ Can be used to add to a payload that is not to be overwritten. GET returns OK (200) but does not do anything, POST with a plain text argument appends this argument to an existing payload or creates a new one containing the posted text, PUT is not allowed (405).
    * __/block_ids__ This node serves as an overview over the existing blocks. GET returns the IDs as a specifically formatted JSON object, POST and PUT are not allowed (405).
    * __/header__ Grants access to the memory's header. GET returns the header in XML format, POST and PUT are not allowed (405).


The Management subtree organises the memory's ownership. The owner is usually the entity who created the memory, although another owner may be assigned later.

* __/mgmt__
    * __/owner__ A representation of this memory's owner entity. GET returns a cleartext name, PUT, with a [well-formed owner string](#app2), updates the owner to a new value. POST is not allowed (405).


Besides the /rest node, there is also a management node to be accessed directly beneath the host address. Here information about the OMS can be queried as well as new memories created. 

* __/mgmt/cloneMemory__ GET and PUT are not supported; by POSTing an XML string describing a new memory this object memory will be recreated exactly as given on the OMS. The XML string must contain a header and an ownership block, other blocks will be added to the resulting object memory if they are well-formed. See the appendix for an [example](#app3) of such a string.
* __/mgmt/createMemory__ GET and PUT are not supported; by POSTing an XML string describing a new memory an initialised and empty version of the object memory will be created and added to the OMS. The XML string must contain a header and an ownership block, other blocks will be ignored. See the appendix for an [example](#app4) of such a string.
* __/mgmt/memoryList__ GET returns a JSON representation of the known object memories on the server, POST and PUT are not supported. A bulk deletion of all memories is not possible, they have to be deleted separately.


## <a name="app1"> Appendix 1: Example XML String representing an object memory block</a>
A single object memory block in XML format that can be sent to the REST interface in order to create a new block. Must contain a creation entry. 

```
<omm:block omm:id="2">
    <omm:primaryID omm:type="url">http://localhost:10082/rest/webtest1</omm:primaryID>
    <omm:creation>
        <omm:creator omm:type="email">name@domain.em</omm:creator>
        <omm:date omm:encoding="ISO8601">2015-04-01T10:00:00+02:00</omm:date>
    </omm:creation>
    <omm:title xml:lang="en">Title of this block</omm:title>
    <omm:description xml:lang="en">Description of this block</omm:description>
    <omm:format omm:encryption="">text</omm:format>
    <omm:payload omm:encoding="none">Payload of this block</omm:payload>
</omm:block>
```

## <a name="app2"> Appendix 2: Example for a well-formed owner String</a>
A well-formed owner string to be sent to the REST interface looks like this:

`cleartext name|||username@@@password`

## <a name="app3"> Appendix 3: Example for a well-formed XML String to clone a memory</a>
```xml
<omm:omm xmlns:omm="http://www.w3.org/2005/Incubator/omm/elements/1.0/">

  <omm:header>
    <omm:version>1</omm:version>
    <omm:primaryID omm:type="url">http://localhost:10082/rest/exampleMemory</omm:primaryID>
  </omm:header>

  <omm:block omm:id="@@@???OWNER_BLOCK???@@@">
    <omm:primaryID omm:type="url">http://localhost:10082/rest/exampleMemory</omm:primaryID>
    <omm:namespace>urn:omm:ownerBlock</omm:namespace>
    <omm:creation>
      <omm:creator omm:type="name">John Doe|||owner@@@hunter2</omm:creator>
      <omm:date omm:encoding="ISO8601">2015-06-25T09:40:19+00:00</omm:date>
    </omm:creation>
    <omm:title xml:lang="en">owner block</omm:title>
    <omm:format>text/plain</omm:format>
    <omm:payload omm:encoding="none">John Doe|||owner@@@hunter2</omm:payload>
  </omm:block>

  <omm:block omm:id="1">
    <omm:primaryID omm:type="url">http://localhost:10082/rest/exampleMemory</omm:primaryID>
    <omm:namespace>urn:omm:block:indentifications</omm:namespace>
    <omm:creation>
      <omm:creator omm:type="email">john.doe@example.com</omm:creator>
      <omm:date omm:encoding="ISO8601">2015-07-03T10:00:00+02:00</omm:date>
    </omm:creation>
    <omm:title xml:lang="de">IDs Block</omm:title>
    <omm:title xml:lang="en">IDs block</omm:title>
    <omm:type>http://purl.org/dc/dcmitype/Dataset</omm:type>
    <omm:payload omm:encoding="none">sample text</omm:payload>
  </omm:block>

</omm:omm>
```

## <a name="app4"> Appendix 4: Example for a well-formed XML String to create a new memory</a>
```xml
<omm:omm xmlns:omm="http://www.w3.org/2005/Incubator/omm/elements/1.0/">

  <omm:header>
    <omm:version>1</omm:version>
    <omm:primaryID omm:type="url">http://localhost:10082/rest/exampleMemory</omm:primaryID>
  </omm:header>

  <omm:block omm:id="@@@???OWNER_BLOCK???@@@">
    <omm:primaryID omm:type="url">http://localhost:10082/rest/exampleMemory</omm:primaryID>
    <omm:namespace>urn:omm:ownerBlock</omm:namespace>
    <omm:creation>
      <omm:creator omm:type="name">John Doe|||owner@@@hunter2</omm:creator>
      <omm:date omm:encoding="ISO8601">2015-06-25T09:40:19+00:00</omm:date>
    </omm:creation>
    <omm:title xml:lang="en">owner block</omm:title>
    <omm:format>text/plain</omm:format>
    <omm:payload omm:encoding="none">John Doe|||owner@@@hunter2</omm:payload>
  </omm:block>

</omm:omm>
```

To find out if the memory has been created successfully it can be asked to return a short informative string. By sending a HTTP GET request to the object memory’s root node (…/rest/memoyName) a response is triggered. If the creation was not successful the OMS will return a 404 error as the memory cannot be found, otherwise the response will be 200 and contain a JSON String that might look like the following:
```javascript
{
	"STORAGE":
	{
		"CAPACITY":"150.0G",
		"FREE_SPACE":"27.0G",
		"LINK":"http://my-computer.net:10082/rest/sample/st",
		"DISTRIBUTED":false,
		"DELETE_DISABLED":false
	},
	"MANAGEMENT":
	{
		"LINK":"http:// my-computer.net:10082/rest/sample/mgmt","FLUSH":false
	},
	"VERSION":1
}
```
