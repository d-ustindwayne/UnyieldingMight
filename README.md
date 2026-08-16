# Unyielding Might Booking Application

Welcome to **Unyielding Might (UM)**, an Android application designed to eliminate booking friction and streamline gym operations. 

While UM offers premium gym amenities, its standout feature is its expert-led fitness classes (free for members, paid for non-members). 
To make this class model sustainable, this app provides a dual-purpose solution:

**Customers:** Register, browse, and book classes.
**Admins:** Gym classes are editable. 

This repository documents the software architecture, design patterns, and unit testing strategies used to build a reliable, scalable mobile experience.

## Installation

## Prerequisites
Before running the application, ensure you have the following installed:
**Android Studio** (Ladybug or newer recommended)
**Android SDK** (API Level 33 or higher)
**Java Development Kit (JDK)** 17 or higher

Clone the repository and open it in **Android Studio**:
```bash
   git clone [https://github.com/SaplingBeing/UnyieldingMight.git](https://github.com/SaplingBeing/UnyieldingMight.git)
   cd UnyieldingMight
```

## Run the app:
**Open Android Studio.**
To run and deploy the application onto an Android Emulator or a physical testing device:

## Android Studio (GUI):
1. Select your target virtual or physical device from the device dropdown menu in the top toolbar.
2. Click the Run button (green play icon) or press Shift + F10.

## Test Commands:

**Via Android Studio:**
1. Open the Project tool window on the left side of Android Studio (ensure the view dropdown at the top is set to Android).
2. Expand the java folder. You will see three packages. Look for the one with (test) next to it (e.g., com.example.unyieldingmight (test)).
3. Right-click on that test folder.
4. Select Run 'All Tests' (or press Ctrl + Shift + F10 on Windows)

**Via Command Line (CLI):**
**Run all unit test:**

```bash
 ./gradlew test
```

**Run Specific Test Classes:**

```bash
./gradlew testDebugUnitTest --tests "com.example.unyieldingmight.*"
```

##  Features
- Dual-User Portals: Separate interfaces and permissions for Customers and Admins.
- Seamless Booking & Payments: Streamlined flow for non-members to pay and members to book instantly.
- Class Schedule Management: Real-time updates for class capacities, times, and trainers.
- Robust Testing: Comprehensive unit testing ensuring all core functional requirements are met.

## Tech Stack
- Platform: Android
- Language: Java
- Architecture: Client-Server Model (CSM) / Event-Driven Architecture
- Build System: Gradle
- Database: SQL Server Management Studio

## Contributing
Pull requests are welcome! For major changes, please open an issue first to discuss what you’d like to change.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits
https://github.com/SaplingBeing
https://github.com/d-ustindwayne
https://github.com/darawesome
