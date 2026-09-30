<div align="center">

# HBM's Nuclear Tech — Fabric

**An unofficial Fabric port of HBM's Nuclear Tech Mod**

[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-62a650?logo=minecraft&logoColor=white)](#compatibility)
[![Fabric](https://img.shields.io/badge/Mod%20loader-Fabric-dbd0b4?logo=fabric&logoColor=black)](#compatibility)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](#building-from-source)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

</div>

Bring the world of HBM's Nuclear Tech to Fabric. This repository contains the mod source and a ready-to-use development build.

> This is an unofficial community port. It is not affiliated with or endorsed by the original mod authors.

## Download

The checked-in build is [`hbm-ntm-fabric-0.1.0.jar`](artifacts/hbm-ntm-fabric-0.1.0.jar). Install it in the `mods` folder alongside Fabric Loader and Fabric API versions compatible with your Minecraft installation.

## Compatibility

| Component | Version |
| --- | --- |
| Minecraft | 26.3 |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.161.0+26.3 |
| Java | 25 or newer |
| Mod ID | `hbm` |

## Building from source

You will need Java 25. From the repository root, run:

```bash
./gradlew build
```

The distributable mod JAR is created in `build/libs/`. On Windows, use `gradlew.bat build`.

## Project layout

- `src/main` — shared mod code and resources
- `src/client` — client-side code and resources
- `src/gametest` — game tests
- `src/test` — unit tests
- `artifacts/` — checked-in mod build

## Credits and license

This is an unofficial port of [Hbm's Nuclear Tech Mod](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT), originally made for Minecraft 1.7.10. The original mod's identity and creative work belong to its authors. See [LICENSE](LICENSE) and [LICENSE.LESSER](LICENSE.LESSER) for the licenses included with this project.

For the original project and its authors, visit [HbmMods/Hbm-s-Nuclear-Tech-GIT](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT).
