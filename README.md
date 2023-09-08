# heylogs

`heylogs` is a set of tools to deal with the keep-a-changelog format.

You can quickly start working with it by following these steps:

1. [Install jbang](https://www.jbang.dev/download/)
2. Create `heylogs.java`

    ```java
    ///usr/bin/env jbang "$0" "$@" ; exit $?

    //DEPS com.github.nbbrd.heylogs:heylogs-cli:0.6.0

    public class heylogs {
    public static void main(String... args) {
        nbbrd.heylogs.cli.HeylogsCommand.main(args);
        }
    }
    ```

3. Execute `jbang heylogs.java`
