# StudyBitsWallet

Android Wallet application for the StudyBits project.

## Building

1. Compile quindy and install in local repository (`mvn install -DskipTests`)
2. Import the project in Android Studio


## Running
1. Run the docker compose setup from the [university-agent](https://github.com/Quintor/StudyBits)
2. Edit the `ENDPOINT_IP` in MainActivity.java to match the `TEST_POOL_IP` injected in the docker-compose setup above.
   This should be an ip that the Android device or emulator can use to reach the pool.

