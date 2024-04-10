/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.heylogs.cli;

import nbbrd.console.picocli.MultiFileInput;
import nbbrd.console.picocli.Profilable;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class MultiChangelogInputOptions implements MultiFileInput, Profilable {

    @CommandLine.Parameters(
            paramLabel = "<file>",
            description = "Input file(s) (default: CHANGELOG.md).",
            arity = "1..*",
            defaultValue = "CHANGELOG.md"
    )
    private List<Path> files;

    @CommandLine.Option(
            names = {"-r", "--recursive"},
            description = "Recursive walking.",
            defaultValue = "false"
    )
    private boolean recursive;

    public boolean isSkipErrors() {
        return false;
    }
}
