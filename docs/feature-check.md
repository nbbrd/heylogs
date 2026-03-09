# Check command

The check command checks the format against an extensive [set of rules](feature-list.md).

```bash
$ heylogs check
CHANGELOG.md
  No problem
```

> [!TIP]
> A rule configuration can be modified by using the `--rule` option.
> 
> For example, upgrading the severity of the `dot-space-link-style` rule
> from `OFF` to `WARN` and disabling the `no-empty-group` rule
> can be done with the following command:  
> `$ heylogs check --rule dot-space-link-style:WARN --rule no-empty-group:OFF`

---

[← Back to Features](features.md)

