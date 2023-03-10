# Demo application for testing DD4J

DD4J Test Demo is Java application for testing DigiDoc4j Java library - https://github.com/open-eid/digidoc4j.

It contains following test classes:
* SignatureCreationTest - for general signature creation.
* SignatureCustomConfTest - for signature creation with custom configuration examples.
* SignatureValidationTest - for validating ASiC-E, BDOC, ASIC-S and DDOC containers.

## How to use it

### Preconditions
1. Java 8 or higher. 
2. Tested DD4J version is in Maven repository.
3. In POM file tested DD4J is assigned.

### Setting up
1. DD4J version is in (local) Maven repository. To test unpublished version, there are two options:
   2. **Build DD4J locally** -> https://confluence.ria.ee/display/IB/How-To#HowTo-KuidaslokaalseltDD4Jkokkuehitada.
   3. **Install JAR file from Jenkins build** -> Before installing new JAR, delete previously installed instance of DD4J with same version and _maven-metadata-local.xml_ file containing reference to it. 
```
mvn install:install-file -Dfile="<path-to-digidoc4j-JAR-file>" -DpomFile="<path-to-digidoc4j-pom-file>"      
```

### Examples
Examples can be found in DD4J wiki https://github.com/open-eid/digidoc4j/wiki and in test classes.
