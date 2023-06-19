package nbbrd.heylogs.cli;

import internal.heylogs.cli.PrintAndLogExceptionHandler;
import internal.heylogs.cli.SpecialProperties;
import nbbrd.console.picocli.ConfigHelper;
import nbbrd.console.picocli.LoggerHelper;
import nbbrd.heylogs.About;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.jansi.graalvm.AnsiConsole;

import java.util.Properties;
import java.util.concurrent.Callable;

@Command(
        name = About.NAME,
        versionProvider = MainCommand.ManifestVersionProvider.class,
        scope = CommandLine.ScopeType.INHERIT,
        sortOptions = false,
        mixinStandardHelpOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        headerHeading = "%n",
        subcommands = {
                ScanCommand.class,
                CheckCommand.class,
                ExtractCommand.class,
                DebugCommand.class
        },
        description = {
                "Set of tools to deal with the @|bold keep-a-changelog|@ format.",
                "%nMore info at https://github.com/nbbrd/heylogs"
        }
)
public final class MainCommand implements Callable<Void> {

    public static void main(String[] args) {
        SpecialProperties specialProperties = SpecialProperties.parse(args);

        if (!specialProperties.isNoConfig()) {
            ConfigHelper.of(About.NAME).loadAll(System.getProperties());
        }

        LoggerHelper.disableDefaultConsoleLogger();

        System.exit(execMain(specialProperties, System.getProperties(), args));
    }

    private static int execMain(SpecialProperties specialProperties, Properties properties, String[] args) {
        specialProperties.apply(System.getProperties());

        try (AnsiConsole ignore = AnsiConsole.windowsInstall()) {
            CommandLine cmd = new CommandLine(new MainCommand());
            cmd.setCaseInsensitiveEnumValuesAllowed(true);
            cmd.setDefaultValueProvider(new CommandLine.PropertiesDefaultProvider(properties));
            cmd.setExecutionExceptionHandler(new PrintAndLogExceptionHandler(MainCommand.class, specialProperties.isDebugRequired()));
            return cmd.execute(args);
        }
    }

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }

    public static final class ManifestVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() {
            return new String[]{
                    "@|bold " + About.NAME + " " + About.VERSION + "|@",
                    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                    "OS: ${os.name} ${os.version} ${os.arch}"
            };
        }
    }
}
