# Properties

## Commandline properties

* `runjar.verbose` - print all diagnostic info to stderr
* `runjar.keep` - do not remove basedir before finishing
* `runjar.basedir` - where to (temporarily) extract the archive
* `runjar.jvmopts` - options to add to forked executions (including shutdown) - should allow debugging

## Packaged properties (in `META-INF/runjar.properties`)

* `runjar.class`         - which class to boot from
* `runjar.preargs`       - arguments to prepend before commandline args; first char defines the delimiter. Expansion of output properties occurs.
* `runjar.shutdown.file` - pointer to propertyfile indicating how to shutdown gracefully; shutdownhook will use it for graceful application shutdown

## Shutdown properties

* `runjar.shutdown.class` - which class to call for shutdown; conflicts with script.* options
* `runjar.shutdown.args`  - arguments to be passed to shutdown execution

## Application properties

These are passed on to contained application so that it can locate its resource on filesystem.
It is however preferred that the app accesses all its files as java resources, rather than directly depending on internals of *Runjar*.

* `runjar.file` - file pointer to the original runjar archive; it is safe to assume that it exists and is in ZIP format
* `runjar.basedir` - directory where the runjar archive has been (temporarily) extracted; can be null if execution is
                    handled within original JVM with a sophisticated classloader
