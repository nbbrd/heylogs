# Forge peculiarities

A forge doesn't only host the source code but also provides additional services such as issue tracking, pull/merge requests, CI/CD pipelines, etc.
Heylogs is able to validate and, in some cases, modify the reference links of these services.

Heylogs supports the following forge references:

|             | Commit | Compare | Issue | Request | Mention |
|:-----------:|:------:|:-------:|:-----:|:-------:|:-------:|
| **GitHub**  |   ✔    |    ✔    |   ✔   |    ✔    |    ✔    |
| **GitLab**  |   ✔    |    ✔    |   ✔   |    ✔    |    ✔    |
| **Forgejo** |   ✔    |    ✔    |   ✔   |    ✔    |    ✔    |

> [!TIP]
> Forge links are detected by using a list of default known hosts (e.g. `github.com`, `gitlab.com`, `forgejo.org`) but 
> the detection can also be customized using the `--domain` option. For example: `$ heylogs check --domain mygit.company.com:gitlab`

---

[← Back to Features](features.md)

