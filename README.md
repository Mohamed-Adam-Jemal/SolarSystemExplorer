<div align="center">

# 🌌 Solar System Explorer

An interactive JavaFX desktop app for exploring our solar system — featuring real-time orbital animations, detailed planetary data, and side-by-side comparisons.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=flat-square&logo=gradle)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

</div>

---

## ✨ Features

- 🪐 **Orbit Animation** — Real-time animated planetary orbits
- 🔍 **Detail View** — Surface imagery, physical stats, and facts per body
- ⚖️ **Compare Mode** — Side-by-side comparison of any two celestial bodies
- 💾 **JSON-Driven Data** — Planet data is external and easy to extend
- 🌟 **Full Coverage** — Sun, 8 planets, and Pluto

---

## 🚀 Setup & Run

### Prerequisites

Install these **before** running any command:

| Tool | Version | Download |
|------|---------|----------|
| **JDK** | 21+ | [adoptium.net](https://adoptium.net/) |
| **Gradle** | 8.x | [gradle.org](https://gradle.org/install/) — or use the included wrapper |
| **Git** | Any | [git-scm.com](https://git-scm.com/) |

> ⚠️ **Important — JDK must be installed first.** Gradle depends on it. If `java -version` doesn't work in your terminal, Gradle will not run.

---

### ⚠️ Path Warning (Windows)

Where you clone the project **matters**. Gradle and JavaFX will fail silently or throw cryptic errors if the project path contains:

- **Spaces** — e.g. `C:\Users\My Projects\solar...` ❌
- **Special characters** — e.g. `é à ù ( ) & # %` ❌

**Use a short, clean path like:**

```
C:\dev\solar-system-explorer   ✅
C:\Users\john\projects\solar   ✅
```

If you're unsure, clone directly to `C:\dev\` and you'll be safe.

### 1. Clone the repository

```bash
git clone https://github.com/Mohamed-Adam-Jemal/solar-system-explorer.git
cd solar-system-explorer
```

### 2. Give permission to Gradle wrapper *(Mac/Linux only)*

```bash
chmod +x gradlew
```

### 3. Run the app

```bash
# Mac/Linux
./gradlew run

# Windows
gradlew.bat run
```

> ⏳ First run may take a few minutes to download dependencies.

---

## 🏛 Architecture

MVC pattern with a data layer and interface contracts:

```
View  (MainView · DetailView · CompareView · OrbitAnimationPane)
  │
Controller  (MainController)
  │
Model  (CelestialBody → Planet / DwarfPlanet / Star)
  │
Data & Utils  (PlanetDataLoader · OrbitCalculator · planets.json)
```

Interfaces `Describable`, `Observable`, and `Orbitable` enforce a clean contract across all celestial body types.

---

## 🔬 Extending Planet Data

All planetary data lives in `src/main/resources/data/planets.json`. To add a body, append an entry:

```json
{
  "name": "Earth",
  "type": "Planet",
  "distanceFromSun": 149.6,
  "orbitalPeriod": 365.25,
  "radius": 6371,
  "description": "The only known planet to harbour life.",
  "image": "earth.jpg"
}
```

Drop the matching texture into `src/main/resources/images/` and you're done.

---

## 🌿 Branch Rules

- `main` branch is protected so create a feature branch, then open a PR to merge

```bash
git checkout -b feature/your-feature-name
```

---

## 👥 Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/Mohamed-Adam-Jemal">
        <img src="https://github.com/Mohamed-Adam-Jemal.png" width="64" style="border-radius:50%"/><br/>
        <sub><b>Mohamed Adam Jemal</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/kiraaziz">
        <img src="https://github.com/kiraaziz.png" width="64" style="border-radius:50%"/><br/>
        <sub><b>kiraaziz</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/sakerfradi111-png">
        <img src="https://github.com/sakerfradi111-png.png" width="64" style="border-radius:50%"/><br/>
        <sub><b>sakerfradi111</b></sub>
      </a>
    </td>
  </tr>
</table>

---

<div align="center">
  Made with ☕ Java &nbsp;·&nbsp; ⭐ Star the repo if you find it useful!
</div>
