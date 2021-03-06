Title: Overview


# runjar-boot

This is the core library of the suite. It takes care of the booting into the application.

Then the user can execute like this:

```
java -jar my-cool-application.jar param1 param2
```

## Usage

The jar needs to be expanded on top of the target jar's content.

After that, a special file `META-INF/runjar.properties` needs to be filled with [properties](properties.html) specifying what to run.


## Limitations

Currently the `runjar-boot` supports only one way of invoking the nested application - spawning a nester (forked) JVM.

However, the design is ready to use alternate invokers, and we plan to support at least one alternative once there is an explicit need for it.

