# BitTalk - Bluetooth Chat App for Android

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=material-design&logoColor=white" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue?style=for-the-badge" />
</p>

<p align="center">
  <img src="screenshots/app_icon.png" alt="BitTalk Logo" width="120" height="120">
</p>

<h3 align="center">A modern, Material Design 3 Bluetooth chat application for Android devices</h3>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#requirements">Requirements</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#contributing">Contributing</a>
</p>

BitTalk – Offline Bluetooth Messaging for Android
BitTalk is an open-source Android app inspired by Jack Dorsey’s BitChat, bringing decentralized peer-to-peer messaging to Android devices. Built with Java and Android Studio, BitTalk uses Bluetooth for real-time communication without any servers, internet, or phone numbers.

✅ Current Features:
Real-time one-to-one messaging via Bluetooth

Automatic nearby device discovery

Material Design 3 interface

🚀 In Development:
Bluetooth LE Mesh support (multi-hop message relaying)

End-to-end encryption (X25519 + AES-256-GCM)

Store & forward: caching messages for offline peers

BitTalk aims to empower offline communication, privacy, and decentralization — especially in areas with poor connectivity or surveillance concerns.



## 📱 Features

- **💬 Real-time Messaging**: Send and receive messages instantly via Bluetooth
- **🎨 Material Design 3**: Beautiful, modern UI following Google's latest design guidelines
- **📱 Device Discovery**: Easily find and connect to nearby Bluetooth devices
- **🔐 Secure Connections**: Direct peer-to-peer encrypted Bluetooth communication
- **📝 Message History**: View your conversation history during the session
- **⚡ Fast & Lightweight**: Optimized for performance and battery efficiency
- **🌓 Theme Support**: Follows system theme (Light/Dark mode)
- **⌨️ Smart Keyboard Handling**: Input field always visible when typing

## 📸 Screenshots

<p align="center">
  <img src="screenshots/main_screen.png" alt="Main Screen" width="250">
  <img src="screenshots/device_selection.png" alt="Device Selection" width="250">
  <img src="screenshots/chat_screen.png" alt="Chat Screen" width="250">
</p>

## 📋 Requirements

- Android Studio Arctic Fox or newer
- Android device or emulator running Android 7.0 (API 24) or higher
- Bluetooth capability on device
- Java 17

## 🚀 Installation

### Clone the Repository

```bash
git clone https://github.com/yourusername/BitTalk.git
cd BitTalk
```

### Open in Android Studio

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the cloned repository
4. Let Android Studio sync the project

### Build and Run

1. Connect your Android device via USB or start an emulator
2. Enable Developer Mode and USB Debugging on your device
3. Click the "Run" button in Android Studio
4. Select your device and click OK

### Build APK

```bash
./gradlew assembleDebug
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

## 📖 Usage

### First Time Setup

1. **Install BitTalk** on two Android devices
2. **Enable Bluetooth** on both devices
3. **Pair the devices** in system Bluetooth settings
4. **Grant permissions** when prompted:
   - Bluetooth permissions
   - Location permission (for device discovery on older Android versions)

### Connecting to Another Device

1. Tap the **Connect** floating action button
2. Select a device from the list of paired devices
3. Or tap "Scan" to discover new devices
4. Wait for connection confirmation

### Sending Messages

1. Once connected, type your message in the input field
2. Tap the send button or press enter
3. Messages appear in real-time on both devices

## 🏗️ Architecture

### Project Structure

```
BitTalk/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/cj/bittalk/
│           │   ├── MainActivity.java
│           │   ├── adapters/
│           │   │   ├── MessageAdapter.java
│           │   │   └── DeviceAdapter.java
│           │   ├── model/
│           │   │   ├── Message.java
│           │   │   └── Device.java
│           │   └── services/
│           │       └── BluetoothService.java
│           └── res/
│               ├── layout/
│               ├── values/
│               └── drawable/
```

### Tech Stack

- **Language**: Java
- **UI Framework**: Material Design 3
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: MVC with Service layer

### Key Components

- **MainActivity**: Main UI controller and permission handling
- **BluetoothService**: Manages Bluetooth connections and message transmission
- **MessageAdapter**: RecyclerView adapter for chat messages
- **DeviceAdapter**: RecyclerView adapter for device selection

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### How to Contribute

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Android Java style guidelines
- Use meaningful variable and method names
- Add comments for complex logic
- Test your changes on multiple devices

## 🔮 Future Enhancements

- [ ] Bluetooth Mesh networking support
- [ ] Group chat functionality
- [ ] Message encryption
- [ ] File sharing capabilities
- [ ] Voice messages
- [ ] Message persistence (local database)
- [ ] Custom themes
- [ ] Chat backup and restore
- [ ] Typing indicators
- [ ] Read receipts

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@chiragjagani](https://github.com/chiragjagani)
- LinkedIn: [Jagani Chirag](https://www.linkedin.com/in/jaganichirag/)

## 🙏 Acknowledgments

- Material Design 3 guidelines by Google
- Android Bluetooth documentation
- Open source community

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/yourusername/BitTalk/issues) page
2. Create a new issue with detailed information
3. Include device model, Android version, and steps to reproduce

---

<p align="center">Made with ❤️ for the Android community</p>

