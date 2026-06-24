Travel Budget Tracker ✈️🌍

A premium, offline-first Android application designed for managing group travel expenses, tracking debts, and storing receipt images locally. Built with modern Android development best practices.

🚀 Features

Offline-First: All data is stored locally using Room Database. No internet connection required.

Smart Dashboard: Track total trip spending and see exactly who owes you money.

Split-Bill Tracking: Easily log expenses shared with friends and mark them as settled when paid.

Receipt Management: Securely attach and view receipt images stored in internal app storage.

CSV Export: Export your complete expense history for any trip to CSV format for easy sharing.

Modern UI: Built with Jetpack Compose for a smooth, premium, and responsive user experience.

🛠 Tech Stack

Language: Kotlin

UI Toolkit: Jetpack Compose

Architecture: MVVM (Model-View-ViewModel)

Local Database: Room Database

Asynchronous Operations: Kotlin Coroutines & Flow

Image Loading: Coil (for efficient receipt display)

Navigation: Jetpack Navigation Compose

🏗 Project Architecture

The project follows clean architecture principles, separating the concerns into distinct layers:

data/: Contains Room entities, DAOs, and the database singleton.

ui/: Contains Compose screens, ViewModels, and UI state management.

📥 Installation

Download the latest .apk file from the Releases page.

Transfer the file to your Android device.

Tap the file to install.

Note: You may need to grant permission in your phone's Settings to "Allow installation from unknown sources" since this is a self-published app.

💻 Building from Source

If you are a developer and want to build the project locally:

Clone this repository: git clone https://github.com/007-Akira/Travel-Budget-Tracker.git

Open the project in Android Studio.

Let Gradle sync and download the necessary dependencies.

Build the project (Build -> Make Project) and run it on an emulator or a connected device.

💡 Feedback & Contributions

This app is an ongoing project. If you find bugs or have feature suggestions (like currency conversion or automated reminders), please feel free to open an Issue in this repository.

Built with ❤️ by Akira
