# Samperk Billing — offline Android app

The whole shop system (billing, purchase, batch/expiry stock, GST returns,
ledgers, final accounts) is one HTML file in `www/index.html`. This folder wraps
it in a Capacitor shell so it installs as a normal Android app that works with
no internet at all, and prints through Android's own print service.

## Getting the APK without installing anything

1. Create a new repository on GitHub (private is fine) and upload this whole folder to it.
2. Open the **Actions** tab → **Build Samperk Billing APK** → **Run workflow**.
3. Wait ~5 minutes. Open the finished run and download the **samperk-billing-apk** artifact.
4. Unzip it, copy `app-debug.apk` to the phone or tablet, and tap it.
   Android will ask to allow installs from this source — allow it once.

That debug APK is fully functional and never expires. It just isn't signed with
your own key, so it can't go on the Play Store.

### If you want a signed release APK

On any computer with Java installed:

```
keytool -genkey -v -keystore samperk.keystore -alias samperk \
        -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 samperk.keystore > keystore.txt
```

Then in the GitHub repo, **Settings → Secrets and variables → Actions**, add:

| Secret | Value |
|---|---|
| `KEYSTORE_B64` | the contents of `keystore.txt` |
| `KEYSTORE_PASSWORD` | the store password you chose |
| `KEY_ALIAS` | `samperk` |
| `KEY_PASSWORD` | the key password you chose |

Re-run the workflow and you also get `app-release.apk`. Keep `samperk.keystore`
safe — updates to the app must be signed with the same key.

## Building locally instead

Needs Node 20+, JDK 21 and the Android SDK (Android Studio installs both).

```
npm install
npx cap add android
cp android-src/app/src/main/java/in/samperk/billing/MainActivity.java \
   android/app/src/main/java/in/samperk/billing/MainActivity.java
npx cap sync android
cd android && ./gradlew assembleDebug
```

APK lands in `android/app/build/outputs/apk/debug/`.

## Printing

Android's WebView has no `window.print()`, so the page calls
`AndroidPrint.print()` and `MainActivity.java` hands the rendered document to
Android's `PrintManager`. In the print dialog you'll see every printer the
device can reach:

* Wi-Fi / network / Mopria printers — for A4 tax invoices
* USB-OTG printers via a print-service app
* Bluetooth thermal printers (58 mm / 80 mm) whose app registers a print
  service — set **Setup → Billing settings → Default print** to
  *Thermal 80mm* or *Thermal 58mm* so the page is laid out for the roll
* **Save as PDF** — for emailing or WhatsApping a bill

## Updating the app later

Replace `www/index.html` with the newer file, push, run the workflow again,
install the new APK over the old one. Your data is untouched — it lives in the
app's own storage and in the JSON file you save.

## Where the data lives

* A copy is kept inside the app automatically after every change.
* **Save** writes a single `.json` file holding everything — keep it on the
  device, and copy it to a pen drive or Google Drive as your backup.
* **Setup → Open file** loads a saved file back, on any device.
