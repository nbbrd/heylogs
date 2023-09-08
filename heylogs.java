///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS com.github.nbbrd.heylogs:heylogs-cli:0.6.0

public class heylogs {
    public static void main(String... args) {
        nbbrd.heylogs.cli.HeylogsCommand.main(args);
    }
}
