# Tips and best practices

This page provides practical tips and best practices for using Heylogs effectively in your projects.

## General tips

- **Automate changelog checks:** Integrate Heylogs into your CI/CD pipeline to catch issues early.
- **Use configuration files:** Place a `heylogs.properties` file at the root of your repository for consistent rule enforcement.
- **Keep changelogs up to date:** Use the `push` command to add changes as you work, not just before releases.
- **Customize rules:** Adjust rule severities and enable/disable rules to fit your workflow.
- **Validate before release:** Always run `heylogs check` before publishing a new release.
- **Leverage CLI composition:** Combine Heylogs with other tools (e.g., `curl`, `jq`, `bat`) for powerful workflows.

## Recover full changelog from release failure

If a release (for example `v1.2.0`) is impossible for whatever reason,
and you have to patch it and increment the version (for example `v1.2.1`) to solve the problem,
you can use this command to get the full release note into your clipboard and paste it where necessary:

```shell
heylogs extract --ref 1.2 | clip
```

This command will extract both 1.2.0 and 1.2.1 release notes and copy them to your clipboard.

---

[← Back to README](../README.md)
