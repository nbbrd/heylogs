# Gitflow

It is possible to automate the changelog release by linking it to a Gitflow release.  
To do so, simply use the `preReleaseGoals` parameter of the [`gitflow-maven-plugin`](https://github.com/aleksandr-m/gitflow-maven-plugin) during a release goal.

## Example:  
```shell 
mvn -B gitflow:release -D preReleaseGoals="heylogs:release -D heylogs.ref=1.2.3" -D releaseVersion=1.2.3
```

Note that you will need to properly configure the gitflow plugin to align it to heylogs config:

## Configuration:
```xml
<plugin>
    <groupId>com.amashchenko.maven.plugin</groupId>
    <artifactId>gitflow-maven-plugin</artifactId>
    <configuration>
        <gitFlowConfig>
            <versionTagPrefix>v</versionTagPrefix>
        </gitFlowConfig>
    </configuration>
</plugin>
```

---

[← Back to README](../README.md)
