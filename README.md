<!-- PROJECT SHIELDS -->
<!--
*** We are using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project
This project is build to serve our customer/partners and give an overview of the Android agent features.

### Built With

* [Android Studio](https://developer.android.com/studio/features)
* [AppDynamics Android Agent](https://www.appdynamics.com/supported-technologies/android)



<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

Android Studio downloaded (i.e. from [developer.android.com](https://developer.android.com/studio)) and installed.

Set of env properties, below example values for macOS 10.15
```
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_261.jdk/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:/usr/local/opt/openjdk/bin"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
export PATH="$PATH:$ANDROID_HOME/emulator"
export PATH="$PATH:$ANDROID_HOME/tools/bin"
export PATH="$PATH:$ANDROID_HOME/tools"
```

To test the features of AppDynamics Android agent, you will need also to  (hence have MRUM license). If you do not have one, please contact [our sales](https://www.appdynamics.com/company/contact-us)

Please create an MRUM application on your AppDynamics Controller. You shall get the App Key and Collector URL from this process. If you would like to test additionally crash reports, this will require publishing pro-guard/dex file mappings. To upload those files, you will need to provide additionally your account name (sample: `AppDynamics-abcdefghXXXXXXXX`) and your EUM license's key (sample: `abcdef12-3456-7890-abcd-17f7fcecd4f0`).

* [Configure ProGuard to Prevent Obfuscation and Class Removal](https://docs.appdynamics.com/21.6/en/end-user-monitoring/mobile-real-user-monitoring/instrument-android-applications/customize-the-android-build/configure-proguard-to-prevent-obfuscation-and-class-removal)
* [Automatically Upload Mapping Files](https://docs.appdynamics.com/21.6/en/end-user-monitoring/mobile-real-user-monitoring/instrument-android-applications/customize-the-android-build/automatically-upload-mapping-files)

For security reason, we keep above values inside `secrets.xml` and `appdynamics.properties` files. This is only to prevent those details to be visible on GitHub. Resource files are more vulnerable to decompilation of your application, hence discoverable. If you would like to keep those values hidden from your end-users refer to [this SO answer](https://stackoverflow.com/a/14572051) for a detailed breakdown of the obfuscation options.

Please create a file `appdynamics.properties` inside your project's root folder (alongside settings.gradle).
```properties
EUM_ACCOUNT_NAME="AppDynamics-abcdefghXXXXXXXX"
EUM_LICENSE_KEY="abcdef12-3456-7890-abcd-17f7fcecd4f0"
```

Please create a file `secrets.xml` in `app/src/main/res/values/` as [explained by CodePath](https://guides.codepath.com/android/Storing-Secret-Keys-in-Android#secrets-in-resource-files)
secrets.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="APP_KEY">YOUR_APP_KEY</string>
    <string name="COLLECTOR_URL">YOUR_COLLECTOR_URL</string>
</resources>
```

### Installation

You can run this project directly, from Android Studio or cloning the repo and opening it with Android Studio application.
1. Clone the project from gitbub
2. Open project in Android studio
3. Replace values in secrets.xml
3. Build application on emulator or physical device

<!-- USAGE EXAMPLES -->
## Usage
For more examples, please refer to the [AppDynamics Documentation](https://docs.appdynamics.com/21.6/en/end-user-monitoring/mobile-real-user-monitoring/instrument-android-applications)_

<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/Appdynamics/Sample-Android-Application/issues) for a list of proposed features (and known issues).

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<!-- LICENSE -->
## License
Distributed under the GNU GPLv3 License. See [LICENSE](https://github.com/Appdynamics/Sample-Android-Application/blob/master/LICENSE.txt) for more information.

<!-- CONTACT -->
## Contact
Feel free to reach out to [AppDynamics Support](https://www.appdynamics.com/support)
Project Link: [https://github.com/Appdynamics/Sample-Android-Application](https://github.com/Appdynamics/Sample-Android-Application)


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/Appdynamics/Sample-Android-Application.svg?style=plastic
[contributors-url]: https://github.com/Appdynamics/Sample-Android-Application/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/Appdynamics/Sample-Android-Application.svg?style=plastic
[forks-url]: https://github.com/Appdynamics/Sample-Android-Application/network/members
[stars-shield]: https://img.shields.io/github/stars/Appdynamics/Sample-Android-Application.svg?style=plastic
[stars-url]: https://github.com/Appdynamics/Sample-Android-Application/stargazers
[issues-shield]: https://img.shields.io/github/issues/Appdynamics/Sample-Android-Application.svg?style=plastic
[issues-url]: https://github.com/Appdynamics/Sample-Android-Application/issues
[license-shield]: https://img.shields.io/github/license/Appdynamics/Sample-Android-Application.svg?style=plastic
[license-url]: https://github.com/Appdynamics/Sample-Android-Application/blob/master/LICENSE.txt

