# AdSkipper

AdSkipper is a lightweight Android accessibility companion that automatically taps the **"Skip Ad"** button the moment it appears on YouTube, keeping playback smooth without modifying the YouTube app or blocking network traffic. The entire experience runs locally on your device, so no accounts, servers, or analytics are required.

## Highlights

- ⚡ **Instant skips** – configurable 0.1 s–0.5 s delay for natural interactions.
- 🔒 **Privacy-first** – no tracking, logins, or network calls; everything stays on-device.
- 🔋 **Battery aware** – optional battery-optimization exemption to keep the service reliable.
- 🎨 **Modern UI** – polished Compose screens, smooth nav animations, and onboarding claim flow.
- 🆓 **Free forever** – open source with a friendly non-commercial license.

## Getting Started

### Requirements
- Android Studio Ladybug+ with Kotlin/Compose toolchain
- Android device/emulator running Android 8.0 (API 26) or newer
- Enable Developer options + USB debugging if deploying to hardware

### Build & Install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### First Launch Checklist
1. Walk through the built-in onboarding to grant the Accessibility Service permission.
2. Optionally allow the "Run in Battery Saver" toggle so the service stays responsive.
3. (Optional) Tweak the skip-delay slider inside **Settings → Skip Delay** to match your preference.
4. Open YouTube and let AdSkipper handle the rest.

## Permissions Explained
| Permission | Why it is needed |
|------------|------------------|
| Accessibility Service | Detects the on-screen "Skip Ad" button and performs the same tap you would. |
| Battery Optimization Exemption (optional) | Prevents Android from pausing the service while the screen is off or the device is in saver mode. |

## Project Structure
- `app/src/main/java/com/evolvarc/adskipper/ui/**` – Jetpack Compose screens and onboarding experience
- `app/src/main/java/com/evolvarc/adskipper/service/**` – Accessibility service and heuristics for identifying ads
- `app/src/main/java/com/evolvarc/adskipper/data/**` – DataStore-based preference wrappers

## Support & Contact
- LinkedIn: [linkedin.com/in/jaswanth-satya-dev](https://www.linkedin.com/in/jaswanth-satya-dev/)
- X (Twitter): [@jaswanthsatydev](https://x.com/jaswanthsatydev)
- GitHub: [github.com/jaswanthsatyadev/AdSkipper](https://github.com/jaswanthsatyadev/AdSkipper)
- Email: [contact@evolvarc.com](mailto:contact@evolvarc.com)

## License
Released under the **AdSkipper Non-Commercial License**. See [`LICENSE.md`](LICENSE.md) for full terms. Commercial use requires written permission from Jaswanth Satya Dev.
