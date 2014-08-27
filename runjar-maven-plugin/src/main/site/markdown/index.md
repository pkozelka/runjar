Title: Overview


    # runjar-maven-plugin

The `runjar-maven-plugin` helps to create a runnable jar application.

It packages all project dependencies in the `runtime` scope as the application libraries.

Additionally, it expands the [runjar-boot](../runjar-boot/index.html) on the top - that's the booting logic.

And of course, creates `META-INF/runjar.properties` so that the booter knows the application's entry point.

Look at [goals](plugin-info.html) for more details.

