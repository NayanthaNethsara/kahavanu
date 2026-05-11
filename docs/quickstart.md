# Quick start

1. Install Android Studio and JDK 17.
2. Clone the repository.
3. Add Firebase config:
   - Copy `app/google-services.example.json` to `app/google-services.json`.
   - Replace it with your Firebase project config.
4. Build and run:
   ```bash
   ./gradlew :app:assembleDebug
   ```

If you hit build issues, run `./gradlew clean` and rebuild.
