# System Requirements Specification (SRS)
## Sudoku Mobile Application for Android

**Version:** 1.0  
**Date:** December 2024  
**Project:** Location-Aware Cyber Threats and Defenses: Mobile Gaming Application  

---

## Table of Contents
1. [Introduction](#1-introduction)
2. [Overall Description](#2-overall-description)
3. [System Features and Requirements](#3-system-features-and-requirements)
4. [External Interface Requirements](#4-external-interface-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Security Requirements](#6-security-requirements)
7. [System Architecture](#7-system-architecture)
8. [Data Requirements](#8-data-requirements)
9. [Appendices](#9-appendices)

---

## 1. Introduction

### 1.1 Purpose
This document specifies the system requirements for a location-aware Sudoku mobile application for Android. The application demonstrates location-based gaming features, hybrid connectivity modes, and security considerations related to GPS spoofing and game cheating as part of the thesis "Location-Aware Cyber Threats and Defenses: Mobile Gaming and Drone Applications."

### 1.2 Scope
The Sudoku Mobile Application is an Android-based puzzle game that:
- Generates and validates 9x9 Sudoku puzzles
- Implements location-based scoring with bonuses for specific geographic locations
- Operates in hybrid mode (offline gameplay with online synchronization)
- Integrates with Firebase cloud database for data persistence
- Implements security measures against GPS spoofing and cheating
- Encrypts sensitive user data using Android Keystore

### 1.3 Definitions, Acronyms, and Abbreviations
- **SRS**: System Requirements Specification
- **GPS**: Global Positioning System
- **API**: Application Programming Interface
- **UI**: User Interface
- **SDK**: Software Development Kit
- **APK**: Android Package Kit
- **Firebase**: Google's mobile and web application development platform
- **Room**: Android's SQLite database abstraction library
- **OCD**: Occasionally Connected Device

### 1.4 References
- Android Developer Documentation: https://developer.android.com
- Firebase Documentation: https://firebase.google.com/docs
- IEEE Standard 830-1998 for Software Requirements Specifications
- Thesis: "Location-Aware Cyber Threats and Defenses: Mobile Gaming and Drone Applications"

### 1.5 Overview
This document is organized into sections covering system description, functional requirements, non-functional requirements, security requirements, and technical specifications.

---

## 2. Overall Description

### 2.1 Product Perspective
The Sudoku Mobile Application is a standalone Android application that integrates with:
- Google Play Services Location API for GPS functionality
- Firebase Realtime Database for cloud data storage
- Android Room Database for local data persistence
- Android Keystore for cryptographic operations

### 2.2 Product Functions
The major functions include:
1. **Puzzle Generation**: Generate valid 9x9 Sudoku puzzles with varying difficulty
2. **Gameplay**: Allow users to fill in puzzle cells with validation
3. **Location Services**: Detect user location and provide location-based bonuses
4. **Score Management**: Calculate and track user scores
5. **Data Synchronization**: Sync local data with Firebase cloud database
6. **Security**: Encrypt sensitive data and implement anti-cheating measures

### 2.3 User Characteristics
**Target Users:**
- Mobile game players interested in puzzle games
- Users with Android devices (API level 23+)
- Users willing to grant location permissions for bonus features
- Research participants for security analysis studies

**Technical Expertise:** Basic smartphone usage skills required

### 2.4 Constraints
- **Platform**: Android OS only (minimum SDK 23, target SDK 34)
- **Connectivity**: Requires internet connection for Firebase synchronization
- **Permissions**: Requires location and internet permissions
- **Hardware**: Requires GPS-enabled Android device
- **Storage**: Requires local storage for Room database

### 2.5 Assumptions and Dependencies
**Assumptions:**
- Users have Android devices with GPS capability
- Users have internet connectivity for cloud synchronization
- Firebase services are available and operational
- Google Play Services are installed on the device

**Dependencies:**
- Android SDK 34
- Firebase Realtime Database
- Google Play Services Location API
- Room Database Library
- OkHttp for network operations

---

## 3. System Features and Requirements

### 3.1 Functional Requirements

#### 3.1.1 Puzzle Generation and Management

**FR-1.1: Generate Sudoku Puzzle**
- **Description**: The system shall generate a valid 9x9 Sudoku puzzle
- **Priority**: High
- **Input**: User request (New Game button)
- **Process**: 
  - Clear the board
  - Fill diagonal 3x3 boxes with random valid numbers
  - Use backtracking algorithm to fill remaining cells
  - Remove 40 numbers to create puzzle (easy difficulty)
- **Output**: 9x9 grid with partially filled cells
- **Validation**: Generated puzzle must have a unique solution

**FR-1.2: Validate Moves**
- **Description**: The system shall validate each user input against Sudoku rules
- **Priority**: High
- **Rules**:
  - Number must be unique in its row
  - Number must be unique in its column
  - Number must be unique in its 3x3 box
- **Input**: Cell position (row, col) and number (1-9)
- **Output**: Boolean indicating validity

**FR-1.3: Store Original Puzzle**
- **Description**: The system shall maintain a copy of the original puzzle state
- **Priority**: Medium
- **Purpose**: Distinguish between pre-filled and user-filled cells

**FR-1.4: Check Puzzle Completion**
- **Description**: The system shall detect when the puzzle is completely and correctly solved
- **Priority**: Medium
- **Validation**: All cells filled with valid numbers

#### 3.1.2 User Interface

**FR-2.1: Display Sudoku Grid**
- **Description**: The system shall display a 9x9 grid of editable cells
- **Priority**: High
- **Layout**: GridLayout with 81 EditText cells
- **Behavior**: 
  - Pre-filled cells should be distinguishable
  - User can input numbers 1-9
  - Empty cells accept user input

**FR-2.2: Display Score**
- **Description**: The system shall display the current score in real-time
- **Priority**: High
- **Calculation**: (Correct cells × 10) + Location bonus
- **Update**: Automatically update on each valid cell entry

**FR-2.3: Display Location Status**
- **Description**: The system shall display current location coordinates
- **Priority**: Medium
- **Format**: "Location: latitude, longitude"
- **States**: Unknown, coordinates, permission denied, error

**FR-2.4: New Game Button**
- **Description**: The system shall provide a button to start a new game
- **Priority**: High
- **Action**: Generate new puzzle and reset score

**FR-2.5: Submit Score Button**
- **Description**: The system shall provide a button to save score locally
- **Priority**: High
- **Action**: Encrypt and store user record in local database

**FR-2.6: Sync to Firebase Button**
- **Description**: The system shall provide a button to synchronize local data to cloud
- **Priority**: Medium
- **Action**: Upload all local records to Firebase

#### 3.1.3 Location Services

**FR-3.1: Request Location Permission**
- **Description**: The system shall request location permissions from the user
- **Priority**: High
- **Permissions**: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
- **Timing**: On application startup

**FR-3.2: Obtain Current Location**
- **Description**: The system shall obtain the user's current GPS coordinates
- **Priority**: High
- **Method**: FusedLocationProviderClient with high accuracy
- **Frequency**: On demand (when needed for scoring)

**FR-3.3: Calculate Location Bonus**
- **Description**: The system shall award bonus points for specific locations
- **Priority**: Medium
- **Target Location**: Klaipeda University (55.7033°N, 21.1443°E)
- **Radius**: Within 1000 meters
- **Bonus**: +50 points
- **Notification**: Toast message when bonus is awarded

**FR-3.4: Handle Location Errors**
- **Description**: The system shall gracefully handle location service failures
- **Priority**: Medium
- **Scenarios**:
  - Location services disabled
  - Permission denied
  - GPS unavailable
  - Timeout
- **Action**: Display appropriate error message, continue without location bonus

#### 3.1.4 Data Persistence

**FR-4.1: Local Database Storage**
- **Description**: The system shall store user records in local SQLite database
- **Priority**: High
- **Technology**: Room Database
- **Schema**: UserRecord entity with encrypted fields
- **Operations**: Insert, query all records

**FR-4.2: Encrypt Sensitive Data**
- **Description**: The system shall encrypt sensitive data before storage
- **Priority**: High
- **Fields to Encrypt**:
  - User ID
  - Score
  - Location coordinates
  - Timestamp
- **Method**: Android Keystore with AES encryption

**FR-4.3: Firebase Cloud Synchronization**
- **Description**: The system shall synchronize local records to Firebase
- **Priority**: Medium
- **Trigger**: Manual (Sync button)
- **Process**: Upload all local records to Firebase Realtime Database
- **Path**: /userRecords/{recordId}

**FR-4.4: Offline Functionality**
- **Description**: The system shall function without internet connectivity
- **Priority**: High
- **Capabilities**:
  - Generate and play puzzles
  - Calculate scores
  - Store records locally
- **Limitation**: Cannot sync to cloud until connectivity restored

#### 3.1.5 Score Management

**FR-5.1: Calculate Base Score**
- **Description**: The system shall calculate score based on correct cells
- **Priority**: High
- **Formula**: Correct filled cells × 10 points
- **Update**: Real-time on each cell change

**FR-5.2: Apply Location Bonus**
- **Description**: The system shall add location bonus to base score
- **Priority**: Medium
- **Condition**: User within bonus location radius
- **Amount**: +50 points

**FR-5.3: Store Score with Metadata**
- **Description**: The system shall store score with associated metadata
- **Priority**: High
- **Metadata**:
  - User ID
  - Score value
  - Location coordinates
  - Timestamp
  - All encrypted

---

## 4. External Interface Requirements

### 4.1 User Interfaces

**UI-1: Main Activity Screen**
- **Components**:
  - Score display (TextView)
  - Location display (TextView)
  - 9×9 Sudoku grid (GridLayout with 81 EditText cells)
  - New Game button
  - Submit Score button
  - Sync to Firebase button
- **Layout**: ConstraintLayout
- **Orientation**: Portrait (primary)
- **Resolution**: Adaptive to device screen size

**UI-2: Input Mechanism**
- **Type**: Numeric keyboard for cell input
- **Range**: 1-9 only
- **Validation**: Real-time validation on input

**UI-3: Feedback Mechanisms**
- **Toast Messages**: For user notifications
- **Text Updates**: For score and location status
- **Visual Cues**: Cell highlighting (future enhancement)

### 4.2 Hardware Interfaces

**HW-1: GPS Receiver**
- **Purpose**: Obtain device location coordinates
- **Accuracy**: High accuracy mode preferred
- **Fallback**: Coarse location if fine location unavailable

**HW-2: Network Interface**
- **Purpose**: Internet connectivity for Firebase synchronization
- **Type**: WiFi or cellular data
- **Requirement**: Optional (app works offline)

**HW-3: Storage**
- **Purpose**: Local database storage
- **Type**: Internal device storage
- **Requirement**: Sufficient space for SQLite database

### 4.3 Software Interfaces

**SW-1: Android Operating System**
- **Minimum Version**: Android 6.0 (API 23)
- **Target Version**: Android 14 (API 34)
- **Compile SDK**: 34

**SW-2: Google Play Services Location API**
- **Version**: 21.0.1
- **Purpose**: Location services
- **Interface**: FusedLocationProviderClient

**SW-3: Firebase Realtime Database**
- **Version**: 20.3.0
- **Purpose**: Cloud data storage
- **Authentication**: Firebase Auth 22.3.0
- **Data Format**: JSON

**SW-4: Room Database**
- **Version**: 2.6.1
- **Purpose**: Local SQLite database abstraction
- **Components**: Database, DAO, Entity

**SW-5: OkHttp Client**
- **Version**: 4.12.0
- **Purpose**: HTTP networking (future API calls)
- **Protocol**: HTTP/HTTPS

**SW-6: Android Keystore**
- **Purpose**: Cryptographic key storage
- **Algorithm**: AES encryption
- **Key Size**: 256-bit

### 4.4 Communication Interfaces

**COM-1: Firebase Realtime Database Protocol**
- **Protocol**: WebSocket over HTTPS
- **Port**: 443
- **Data Format**: JSON
- **Authentication**: Firebase authentication token

**COM-2: Location Services**
- **Protocol**: Google Play Services API
- **Data**: Latitude, longitude, accuracy
- **Update Frequency**: On-demand

---

## 5. Non-Functional Requirements

### 5.1 Performance Requirements

**PERF-1: Puzzle Generation Time**
- **Requirement**: Generate puzzle within 2 seconds
- **Measurement**: Time from button press to grid display
- **Priority**: High

**PERF-2: Move Validation Time**
- **Requirement**: Validate move within 100 milliseconds
- **Measurement**: Time from input to validation result
- **Priority**: High

**PERF-3: Location Acquisition Time**
- **Requirement**: Obtain location within 10 seconds
- **Measurement**: Time from request to location callback
- **Priority**: Medium
- **Fallback**: Continue without location if timeout

**PERF-4: Database Operations**
- **Requirement**: Insert/query operations within 500 milliseconds
- **Measurement**: Time for database transaction
- **Priority**: Medium

**PERF-5: UI Responsiveness**
- **Requirement**: UI updates within 100 milliseconds
- **Measurement**: Time from user action to visual feedback
- **Priority**: High

### 5.2 Safety Requirements

**SAFE-1: Data Loss Prevention**
- **Requirement**: Prevent data loss during app crashes
- **Implementation**: Atomic database transactions
- **Priority**: High

**SAFE-2: Location Privacy**
- **Requirement**: Protect user location data
- **Implementation**: Encryption before storage
- **Priority**: High

**SAFE-3: Graceful Degradation**
- **Requirement**: Continue operation when services unavailable
- **Implementation**: Offline mode, error handling
- **Priority**: High

### 5.3 Security Requirements
(See Section 6 for detailed security requirements)

### 5.4 Software Quality Attributes

**QUAL-1: Reliability**
- **Availability**: 99% uptime for local functionality
- **MTBF**: Mean time between failures > 100 hours
- **Recovery**: Automatic recovery from crashes

**QUAL-2: Maintainability**
- **Code Structure**: Modular design with separation of concerns
- **Documentation**: Inline comments and external documentation
- **Testing**: Unit tests for critical components

**QUAL-3: Usability**
- **Learning Curve**: Users should understand interface within 5 minutes
- **Error Messages**: Clear, actionable error messages
- **Accessibility**: Support for standard Android accessibility features

**QUAL-4: Portability**
- **Device Compatibility**: Support for devices with API 23+
- **Screen Sizes**: Adaptive layout for various screen sizes
- **Android Versions**: Compatible with Android 6.0 to 14+

**QUAL-5: Scalability**
- **User Records**: Support for unlimited local records
- **Firebase**: Scalable cloud storage
- **Performance**: Maintain performance with large datasets

---

## 6. Security Requirements

### 6.1 Authentication and Authorization

**SEC-1.1: User Identification**
- **Requirement**: Assign unique identifier to each user
- **Implementation**: Hardcoded user ID (demo) or Firebase Auth (production)
- **Priority**: Medium

**SEC-1.2: Firebase Authentication**
- **Requirement**: Authenticate with Firebase for cloud operations
- **Implementation**: Firebase Auth SDK
- **Priority**: Medium

### 6.2 Data Encryption

**SEC-2.1: Encryption at Rest**
- **Requirement**: Encrypt sensitive data in local database
- **Fields**: User ID, score, location, timestamp
- **Algorithm**: AES-256
- **Key Storage**: Android Keystore
- **Priority**: High

**SEC-2.2: Encryption in Transit**
- **Requirement**: Encrypt data during Firebase synchronization
- **Protocol**: HTTPS/TLS
- **Implementation**: Firebase SDK (built-in)
- **Priority**: High

**SEC-2.3: Key Management**
- **Requirement**: Securely generate and store encryption keys
- **Implementation**: Android Keystore System
- **Key Type**: AES symmetric key
- **Key Size**: 256 bits
- **Priority**: High

### 6.3 GPS Spoofing Prevention

**SEC-3.1: Location Validation**
- **Threat**: GPS spoofing to gain unfair location bonuses
- **Current Implementation**: Client-side location check
- **Recommended Enhancement**: Server-side validation
- **Methods**:
  - Cross-reference with Wi-Fi/cellular triangulation
  - Detect impossible location jumps
  - Rate limiting on bonus claims
  - Cryptographic location attestation
- **Priority**: High

**SEC-3.2: Anomaly Detection**
- **Requirement**: Detect suspicious location patterns
- **Indicators**:
  - Rapid location changes
  - Frequent bonus location visits
  - Location jumps exceeding physical possibility
- **Action**: Flag for review, limit bonuses
- **Priority**: Medium

### 6.4 Anti-Cheating Measures

**SEC-4.1: Puzzle Validation**
- **Threat**: External solvers or bots
- **Current Implementation**: Client-side validation
- **Recommended Enhancement**: Server-side puzzle verification
- **Method**: Submit completed puzzle to server for validation
- **Priority**: Medium

**SEC-4.2: Behavioral Analysis**
- **Requirement**: Detect automated gameplay
- **Indicators**:
  - Unrealistically fast completion times
  - Perfect accuracy
  - Consistent input patterns
- **Action**: Flag suspicious scores
- **Priority**: Low

**SEC-4.3: Rate Limiting**
- **Requirement**: Limit score submissions
- **Implementation**: Timestamp validation, cooldown periods
- **Purpose**: Prevent score flooding
- **Priority**: Low

### 6.5 Data Integrity

**SEC-5.1: Tamper Detection**
- **Requirement**: Detect unauthorized data modifications
- **Implementation**: Checksums, digital signatures (future)
- **Priority**: Medium

**SEC-5.2: Audit Trail**
- **Requirement**: Maintain logs of critical operations
- **Data**: Timestamps, user actions, location data
- **Purpose**: Security analysis and debugging
- **Priority**: Medium

### 6.6 Permission Management

**SEC-6.1: Minimal Permissions**
- **Requirement**: Request only necessary permissions
- **Permissions**:
  - ACCESS_FINE_LOCATION (for location bonuses)
  - ACCESS_COARSE_LOCATION (fallback)
  - INTERNET (for Firebase sync)
- **Priority**: High

**SEC-6.2: Runtime Permission Handling**
- **Requirement**: Request permissions at runtime (Android 6.0+)
- **Implementation**: ActivityCompat.requestPermissions
- **Fallback**: Graceful degradation if denied
- **Priority**: High

### 6.7 Secure Communication

**SEC-7.1: HTTPS Only**
- **Requirement**: All network communication over HTTPS
- **Implementation**: Firebase SDK, OkHttp with TLS
- **Priority**: High

**SEC-7.2: Certificate Pinning**
- **Requirement**: Validate server certificates (future enhancement)
- **Purpose**: Prevent man-in-the-middle attacks
- **Priority**: Low

---

## 7. System Architecture

### 7.1 Architectural Pattern
**Pattern**: Model-View-Controller (MVC) with Repository Pattern

**Components:**
1. **View Layer**: MainActivity (UI)
2. **Controller Layer**: MainActivity (event handling)
3. **Model Layer**: 
   - SudokuGame (game logic)
   - UserRecord (data model)
4. **Data Layer**:
   - DatabaseHelper (Room database)
   - UserRecordDao (data access)
   - Firebase (cloud storage)
5. **Service Layer**:
   - CryptoHelper (encryption)
   - FusedLocationProviderClient (location)

### 7.2 Component Diagram

```
┌─────────────────────────────────────────────────────────┐
│                     MainActivity                         │
│  (UI Controller + Event Handling)                       │
└────────┬────────────────────────────────────────────────┘
         │
         ├──────────────┬──────────────┬──────────────┬────────────
         │              │              │              │
         ▼              ▼              ▼              ▼
┌─────────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────────┐
│ SudokuGame  │ │ DatabaseHelper│ │ Firebase │ │ Location     │
│ (Logic)     │ │ (Local DB)   │ │ (Cloud)  │ │ Services     │
└─────────────┘ └──────┬───────┘ └──────────┘ └──────────────┘
                       │
                       ▼
                ┌──────────────┐
                │ UserRecordDao│
                │ (Data Access)│
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │  UserRecord  │
                │ (Data Model) │
                └──────────────┘
```

### 7.3 Data Flow

**Gameplay Flow:**
1. User starts new game → SudokuGame generates puzzle
2. User inputs number → Validation → Update score
3. Score calculation includes location bonus if applicable

**Data Persistence Flow:**
1. User submits score → Encrypt data (CryptoHelper)
2. Store in Room database (DatabaseHelper)
3. User triggers sync → Upload to Firebase

**Location Flow:**
1. Request permission → User grants/denies
2. Obtain location → FusedLocationProviderClient
3. Check bonus location → Calculate bonus
4. Update score display

### 7.4 Technology Stack

**Frontend:**
- Android SDK 34
- Java 11
- XML layouts
- Material Design components

**Backend/Services:**
- Firebase Realtime Database
- Firebase Authentication
- Google Play Services Location

**Data Storage:**
- Room Database (SQLite)
- Android Keystore (encryption keys)

**Libraries:**
- AndroidX AppCompat 1.6.1
- Material Components 1.9.0
- Room 2.6.1
- Play Services Location 21.0.1
- OkHttp 4.12.0
- Firebase Database 20.3.0
- Firebase Auth 22.3.0

**Testing:**
- JUnit 4.13.2
- AndroidX Test
- Espresso 3.5.1
- Robolectric 4.11.1

---

## 8. Data Requirements

### 8.1 Data Models

#### 8.1.1 UserRecord Entity

```java
@Entity(tableName = "userrecord")
public class UserRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;                      // Auto-generated unique ID
    public String encryptedUserId;      // Encrypted user identifier
    public String encryptedScore;       // Encrypted score value
    public String encryptedLocation;    // Encrypted lat,long coordinates
    public String encryptedTimestamp;   // Encrypted timestamp
}
```

**Field Descriptions:**
- **id**: Primary key, auto-incremented
- **encryptedUserId**: AES-encrypted user identifier
- **encryptedScore**: AES-encrypted score value
- **encryptedLocation**: AES-encrypted location coordinates (format: "lat,long")
- **encryptedTimestamp**: AES-encrypted Unix timestamp

#### 8.1.2 Sudoku Board Data

**Structure:** 2D integer array (9×9)
- **Values**: 0-9 (0 represents empty cell)
- **Storage**: In-memory during gameplay
- **Persistence**: Not persisted (regenerated each game)

### 8.2 Database Schema

#### 8.2.1 Local Database (Room/SQLite)

**Database Name:** sudoku_database_v2

**Tables:**
1. **userrecord**
   - id (INTEGER, PRIMARY KEY, AUTOINCREMENT)
   - encryptedUserId (TEXT)
   - encryptedScore (TEXT)
   - encryptedLocation (TEXT)
   - encryptedTimestamp (TEXT)

**Indexes:** Primary key on id

**Version:** 2 (with fallback to destructive migration)

#### 8.2.2 Cloud Database (Firebase)

**Database Structure:**
```
/userRecords
  /{recordId}
    - id: integer
    - encryptedUserId: string
    - encryptedScore: string
    - encryptedLocation: string
    - encryptedTimestamp: string
```

**Access Rules:** (To be configured in Firebase Console)
- Read: Authenticated users
- Write: Authenticated users (own records)

### 8.3 Data Encryption Specifications

**Algorithm:** AES (Advanced Encryption Standard)
**Mode:** CBC (Cipher Block Chaining) or GCM (Galois/Counter Mode)
**Key Size:** 256 bits
**Key Storage:** Android Keystore System
**IV:** Randomly generated per encryption operation

**Encryption Process:**
1. Generate/retrieve AES key from Keystore
2. Generate random IV
3. Encrypt plaintext data
4. Store IV + ciphertext (or use authenticated encryption)

**Decryption Process:**
1. Retrieve AES key from Keystore
2. Extract IV from stored data
3. Decrypt ciphertext
4. Return plaintext

### 8.4 Data Retention and Privacy

**Local Data:**
- Retention: Until user uninstalls app or clears data
- Deletion: User can clear app data via Android settings

**Cloud Data:**
- Retention: Indefinite (for research purposes)
- Deletion: Manual deletion via Firebase Console
- Privacy: All sensitive fields encrypted

**Location Data:**
- Collection: Only when user grants permission
- Usage: Location bonus calculation, research analysis
- Storage: Encrypted in database
- Sharing: Not shared with third parties

---

## 9. Appendices

### 9.1 Glossary

**Backtracking Algorithm**: A recursive algorithm used to solve constraint satisfaction problems like Sudoku by trying possibilities and backtracking when constraints are violated.

**Hybrid Connectivity**: Operating mode where application functions offline but synchronizes data when online.

**GPS Spoofing**: The practice of falsifying GPS location data to deceive location-based applications.

**Android Keystore**: A secure container for cryptographic keys on Android devices, protected by hardware security if available.

**Room Database**: Android's persistence library providing an abstraction layer over SQLite.

**Firebase Realtime Database**: A cloud-hosted NoSQL database that synchronizes data in real-time.

**AES Encryption**: Advanced Encryption Standard, a symmetric encryption algorithm widely used for securing data.

### 9.2 Analysis Models

#### 9.2.1 Use Case Diagram

```
                    ┌──────────────┐
                    │    Player    │
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Start New   │   │  Fill Sudoku │   │ Submit Score │
│    Game      │   │    Cells     │   │              │
└──────────────┘   └──────────────┘   └──────────────┘
        │                  │                  │
        │                  │                  ▼
        │                  │          ┌──────────────┐
        │                  │          │ Sync to      │
        │                  │          │ Firebase     │
        │                  │          └──────────────┘
        │                  │
        ▼                  ▼
┌──────────────────────────────────┐
│   View Score & Location Status   │
└──────────────────────────────────┘
```

#### 9.2.2 State Diagram (Game State)

```
     [Start]
        │
        ▼
   ┌─────────┐
   │  Idle   │◄──────────┐
   └────┬────┘           │
        │                │
        │ New Game       │
        ▼                │
   ┌─────────┐           │
   │Generating│          │
   │ Puzzle  │           │
   └────┬────┘           │
        │                │
        ▼                │
   ┌─────────┐           │
   │ Playing │           │
   └────┬────┘           │
        │                │
        │ Fill cells     │
        ▼                │
   ┌─────────┐           │
   │Validating│          │
   └────┬────┘           │
        │                │
        ├─Valid──────────┤
        │                │
        ├─Complete───────┤
        │                │
        ▼                │
   ┌─────────┐           │
   │ Solved  │───────────┘
   └─────────┘
```

### 9.3 Security Threat Model

#### 9.3.1 Identified Threats

| Threat ID | Threat | Impact | Likelihood | Mitigation |
|-----------|--------|--------|------------|------------|
| T-01 | GPS Spoofing | High | High | Server-side validation, anomaly detection |
| T-02 | External Solver | Medium | Medium | Server-side puzzle validation, timing analysis |
| T-03 | Data Tampering | High | Low | Encryption, checksums |
| T-04 | Man-in-the-Middle | High | Low | HTTPS, certificate pinning |
| T-05 | Unauthorized Access | Medium | Low | Firebase authentication, access rules |
| T-06 | Score Inflation | Medium | Medium | Rate limiting, behavioral analysis |
| T-07 | Privacy Breach | High | Low | Data encryption, minimal data collection |

#### 9.3.2 Attack Scenarios

**Scenario 1: GPS Spoofing Attack**
1. Attacker installs GPS spoofing app
2. Sets fake location to bonus location (Klaipeda University)
3. Plays Sudoku game
4. Receives unearned location bonus
5. Submits inflated score

**Mitigation:**
- Implement server-side location verification
- Cross-reference GPS with Wi-Fi/cellular data
- Detect impossible location patterns
- Rate limit bonus claims

**Scenario 2: Automated Solver Attack**
1. Attacker extracts puzzle from app
2. Uses external Sudoku solver
3. Inputs solution rapidly
4. Achieves perfect score in unrealistic time
5. Submits fraudulent score

**Mitigation:**
- Server-side puzzle validation
- Timing analysis (flag suspiciously fast completions)
- Behavioral pattern detection
- CAPTCHA for score submission (future)

### 9.4 Testing Requirements

#### 9.4.1 Unit Testing
- **SudokuGame**: Puzzle generation, validation logic
- **CryptoHelper**: Encryption/decryption operations
- **DatabaseHelper**: CRUD operations
- **Coverage Target**: 80% code coverage

#### 9.4.2 Integration Testing
- Firebase synchronization
- Location services integration
- Database operations with encryption
- UI interactions with backend logic

#### 9.4.3 Security Testing
- Encryption key security
- Data transmission security
- Permission handling
- GPS spoofing detection
- Penetration testing (ethical hacking)

#### 9.4.4 Performance Testing
- Puzzle generation time
- UI responsiveness
- Database query performance
- Location acquisition time
- Memory usage

#### 9.4.5
