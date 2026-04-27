# Solar System Explorer

## Prerequisites
- Git
- Internet connection (first run downloads dependencies automatically)
- Java 21 is handled automatically by Gradle toolchain

## Setup & Run

### 1. Clone the repository
```bash
git clone <repo-url>
cd system-solar-explorer
```

### 2. Give permission to Gradle wrapper (Mac/Linux only)
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

> First run may take a few minutes to download dependencies.

## Branch Rules
- Never push directly to `main`
- Create a feature branch then open a PR to merge
