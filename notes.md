# RunJar - toolkit to make a Runnable Java Archive

This toolkit helps creating executable java archives easily.


## Inspiration

* Simon Tuffs' one-jar
* dstovall's one-jar-maven-plugin
* commandline tooling
* Old good SWAR (Eurotel)

## TODO

* one-jar

   - explore existing features
   - minimize them to minimum
   - release first version that after having combined with runjar
   - add extension points allowing to re-introduce some of the original features in optional manner

* maven plugin

   - explore current goals and params
   - try to design my plugin in a compatible way, or think about compatible goals

## Use-cases

### Enhancing Ant script with runtime

* POM type: pom
* main file: main.ant.xml
* own classes: exploded

Libraries:

* ant.jar, ant-launcher.jar, ant-*.jar
* custom extensions like ant-contrib.jar

* specific tasks 

### Enhancing a JAR module with dependencies

* POM type: jar
* side artifact
* own classes: exploded/packed; exploded can be extensions for runjar

### Packaging set of dependencies as an executable jar

* POM type: pom
* main artifact

### Enhance WAR with a webserver

* POM type: war

* jetty/tomcat/jboss/winstone/...
