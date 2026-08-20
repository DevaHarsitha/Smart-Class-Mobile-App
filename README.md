# Smart Class Mobile App

> An Android-based classroom management application designed to simplify communication and academic resource sharing between students and staff.

**Source Code:** https://github.com/DevaHarsitha/Smart-Class-Mobile-App

---

## Overview

Smart Class is an Android application developed to provide a centralized platform for managing classroom-related academic activities.

The application supports two user roles: **Student** and **Staff**. Staff members can share announcements, study materials, and assignments, while students can access academic resources, view announcements, and submit assignments through the application.

The application uses local data persistence to maintain user information and application data, providing a simple and accessible solution for classroom management.

---

## Features

### Student

* Student login and profile management
* View classroom announcements
* Access subject-wise study materials
* View assignments
* Upload assignment submissions
* Track academic information
* View attendance information

### Staff

* Staff login and profile management
* Post classroom announcements
* Upload study materials
* Create and manage assignments
* View student-related information
* Manage classroom academic content

### Application Features

* Role-based navigation for Student and Staff
* Subject-wise organization of academic resources
* Announcement management
* Assignment management
* Material sharing
* Assignment submission
* Attendance management
* Persistent local data storage
* RecyclerView-based dynamic content display

---

## Application Architecture

```text
                         Smart Class
                              |
                 ┌────────────┴────────────┐
                 |                         |
              Student                    Staff
                 |                         |
        ┌────────┼────────┐        ┌───────┼────────┐
        |        |        |        |       |        |
   Materials  Assignments  |   Materials  Assignments
        |        |        |        |       |        |
        └────────┴────────┘        └───────┴────────┘
                 |
                 v
          Local Data Storage
        SharedPreferences + Gson
```

---

## Technology Stack

| Category           | Technologies                                  |
| ------------------ | --------------------------------------------- |
| Platform           | Android                                       |
| Language           | Java                                          |
| IDE                | Android Studio                                |
| UI                 | XML, ConstraintLayout, LinearLayout, CardView |
| Lists              | RecyclerView                                  |
| Local Storage      | SharedPreferences                             |
| Data Serialization | Gson                                          |
| Architecture       | Activity / Fragment based                     |
| Build System       | Gradle                                        |

---

## Key Android Components

### SharedPreferences

SharedPreferences is used for persistent storage of application data such as:

* User login state
* User type
* Profile information
* Announcements
* Application preferences

### Gson

Gson is used to serialize and deserialize Java objects when storing structured data in SharedPreferences.

### RecyclerView

RecyclerView is used to dynamically display:

* Announcements
* Assignments
* Materials
* Other list-based academic content

### Fragments and Activities

The application uses Activities and Fragments to organize different sections of the application and provide role-specific navigation.

### Role-Based Access

The application distinguishes between:

```text
Student
   |
   ├── View Materials
   ├── View Assignments
   ├── Submit Assignments
   ├── View Announcements
   └── View Profile

Staff
   |
   ├── Post Materials
   ├── Create Assignments
   ├── Post Announcements
   ├── Manage Academic Content
   └── View Profile
```

---

## Project Structure

```text
Smart-Class-Mobile-App/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── gradle/
│
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

---

## Getting Started

### Prerequisites

* Android Studio
* Android SDK
* Java Development Kit (JDK)
* Android Emulator or physical Android device

### Open the Project

1. Open Android Studio.
2. Select **Open**.
3. Choose the cloned `Smart-Class-Mobile-App` directory.
4. Allow Gradle to synchronize the project.
5. Connect an Android device or start an emulator.
6. Build and run the application.

---

## Application Flow

```text
Launch Application
       |
       v
     Login
       |
       v
  Identify User Type
       |
   ┌───┴────┐
   |        |
Student    Staff
   |        |
   v        v
Student   Staff
Dashboard Dashboard
   |        |
   v        v
Academic  Manage
Resources Resources
```

---

## Security and Data Handling

The application separates Student and Staff functionality using role-based application logic.

User preferences and locally stored application data are maintained using Android's SharedPreferences mechanism.

For a production version, the application could be extended with a secure backend authentication system and remote database instead of relying primarily on local storage.

---

## Future Enhancements

* Cloud-based database integration
* Firebase authentication
* Push notifications for announcements and assignments
* Online attendance synchronization
* Faculty-student messaging
* Cloud-based assignment storage
* Online assignment evaluation
* Attendance reports and analytics
* Improved backend security
* Multi-device data synchronization

---

## Learning Outcomes

This project demonstrates practical experience with:

* Android application development using Java
* Android Studio and Gradle
* Activity and Fragment lifecycle
* XML-based UI development
* RecyclerView implementation
* SharedPreferences
* Gson serialization
* Role-based application functionality
* Form handling and navigation
* Local data persistence
* Mobile application UI design

---

## Author

### Deva Harsitha B V

Computer Science Engineering Student

**GitHub:** https://github.com/DevaHarsitha
