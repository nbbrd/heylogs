package nbbrd.heylogs;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Sample {

    public static final String HEADING_UNRELEASED = "## [Unreleased]";
    public static final String HEADING_V1_1_0 = "## [1.1.0] - 2019-02-15";
    private static final String HEADING_V1_0_0 = "## [1.0.0] - 2017-06-20";

    public static Node get() throws IOException {
        Parser parser = Parser.builder().build();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Sample.class.getResourceAsStream("/nbbrd/heylogs/CHANGELOG.md")))) {
            return parser.parseReader(reader);
        }
    }

}
