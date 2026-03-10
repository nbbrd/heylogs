# Badges

Heylogs makes it possible to generate badges for the unreleased changes of a changelog using a [GitHub workflow](https://github.com/nbbrd/heylogs/blob/develop/.github/workflows/heylogs.yml) and the [shields.io API](https://shields.io/badges/endpoint-badge).  

This workflow has the following steps:
1. it [summarizes](usage-maven-plugin.md) the changelog content into a json file
2. it converts this json file to a format that [shields.io](https://shields.io/badges/endpoint-badge) can understand
3. it pushes the result to a [dedicated branch](https://github.com/nbbrd/heylogs/tree/badges)

## Examples

![default](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json)  
![with custom label](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=changelog)  
![without icon](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=changelog&logo=none)  
![without label](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fheylogs%2Fbadges%2Funreleased-changes.json&label=%20)

---

[← Back to README](../README.md)
