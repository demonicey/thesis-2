# Sudoku Mobile App for Android

This is a basic Sudoku game mobile app developed for Android, incorporating location-based features, security analysis, and cloud database integration. The app is designed as an occasionally connected device (hybrid mode), allowing offline gameplay with online synchronization for scores and user data.

## Project Overview

The app aligns with the thesis "Location-Aware Cyber Threats and Defenses: Mobile Gaming and Drone Applications" by demonstrating location-based gaming, potential security vulnerabilities (GPS spoofing, cheating), and countermeasures.

### Key Features
- Basic 9x9 Sudoku puzzle generation and validation
- Location-based scoring with bonuses for specific locations (e.g., near Klaipeda University)
- Hybrid connectivity: Offline play, online sync
- Firebase cloud database for user records, scores, and locations
- Security analysis documenting hacks and preventions

## Development Steps

### Step 1: Set up Android Project Structure
- Created a new Android project using Gradle.
- Configured project-level and app-level build.gradle files with necessary dependencies (Firebase, Location services).
- Set up basic directory structure: src/main/java, src/main/res, etc.
- Added AndroidManifest.xml with permissions for location and internet.

### Step 2: Implement Basic Sudoku Game Logic
- Created SudokuGame.java class for puzzle generation, validation, and cell management.
- Implemented backtracking algorithm for generating valid Sudoku puzzles.
- Added methods for checking valid moves and solving puzzles.

### Step 3: Add Location Services for Location-Based Scoring
- Integrated Google Play Services Location API.
- Added location permission requests and GPS access.
- Implemented bonus scoring when user is within 1km of predefined locations (e.g., Klaipeda University coordinates).

### Step 4: Integrate Firebase for Cloud Database
- Set up Firebase dependencies in build.gradle.
- Created FirebaseHelper.java for database operations.
- Implemented user record saving with score, latitude, longitude, and timestamp.

### Step 5: Implement Hybrid Connectivity
- Designed app to work offline for gameplay.
- Added online sync for score submission and data retrieval.
- Used Firebase for real-time data synchronization.

### Step 6: Add UI Components
- Created activity_main.xml layout with Sudoku grid, score display, and location status.
- Implemented dynamic EditText grid for Sudoku input.
- Added buttons for new game and score submission.

### Step 7: Document Security Analysis
- Created SecurityAnalysis.md detailing potential hacks:
  - GPS spoofing to fake location for bonuses
  - Cheating via external solvers or bots
- Proposed preventions:
  - Server-side location verification
  - Puzzle validation on server
  - Rate limiting and anomaly detection

### Step 8: Build and Test
- Compiled the project using Gradle.
- Generated debug APK successfully.
- Verified code compilation and resource merging.

### Step 9: Final Sync and Validation
- Ensured all components integrate properly.
- Validated Firebase configuration and database schema.
- Confirmed security measures are in place.

## File Structure
```
SudokuApp/
├── app/
│   ├── build.gradle          # App-level dependencies
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/sudoku/
│   │   │   ├── MainActivity.java     # Main UI and logic
│   │   │   ├── SudokuGame.java       # Game logic
│   │   │   └── FirebaseHelper.java   # Database helper
│   │   └── res/
│   │       ├── layout/activity_main.xml
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── styles.xml
│   │       └── xml/
│   │           ├── data_extraction_rules.xml
│   │           └── backup_rules.xml
│   └── build/outputs/apk/debug/app-debug.apk  # Generated APK
├── build.gradle               # Project-level build
├── settings.gradle
├── gradle.properties
├── local.properties
└── SecurityAnalysis.md
```

## How to Build and Run
1. Ensure Android SDK and Gradle are installed.
2. Open the project in Android Studio.
3. Sync Gradle files.
4. Build the project (Build > Make Project).
5. Run on emulator or device (Run > Run 'app').

## Dependencies
- Android Gradle Plugin 8.1.0
- Firebase Database 20.3.0
- Firebase Auth 22.3.0
- Google Play Services Location 21.0.1
- AppCompat 1.6.1
- Material Components 1.9.0

## Security Considerations
See SecurityAnalysis.md for detailed analysis of potential vulnerabilities and mitigation strategies.

## Thesis Alignment
This app demonstrates practical implementation of concepts from the thesis, including:
- Location-based services in mobile gaming
- Hybrid connectivity modes
- Cyber threats like GPS spoofing and game cheating
- Defensive measures for secure location-aware applications
