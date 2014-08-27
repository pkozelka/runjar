Title: Overview


# RUNJAR

helps create runnable java archives containing complete application.

You can use simple ant tasks (`zip`, `unzip`, etc) to build such a runnable jar, 
with [runjar-boot](runjar-boot/index.html) to handle the application booting stuff.

Or you can use the [runjar-maven-plugin](runjar-maven-plugin/index.html) which takes care of this for you.

Have a look at our plugin integration tests demonstrating the usage:

* [This ANT-based one](https://github.com/pkozelka/runjar/blob/master/runjar-maven-plugin/src/it/test-enhance-ant/pom.xml)
  makes [the ant script](https://github.com/pkozelka/runjar/blob/master/runjar-maven-plugin/src/it/test-enhance-ant/src/main/scripts/main.ant.xml)
  executable by adding ANT artifacts and the runjar stuff.
  
* [This Java-based one](https://github.com/pkozelka/runjar/blob/master/runjar-maven-plugin/src/it/test-enhance-jar/pom.xml)
  takes standard runnable jar and equips it with all dependencies and runjar stuff to produce runnable application.
  
Both these samples demonstrate the *shutdown* functionality, which, in simple words, allows the application to instruct `runjar-boot` 
to perform specific action when the JVM is about to terminate.
