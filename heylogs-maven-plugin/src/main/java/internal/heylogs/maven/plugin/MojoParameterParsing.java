package internal.heylogs.maven.plugin;

import nbbrd.design.MightBePromoted;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@MightBePromoted
@Target(ElementType.METHOD)
public @interface MojoParameterParsing {
}
