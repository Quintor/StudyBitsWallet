# StudyBitsWallet

Android Wallet application for the StudyBits project.

## Building

1. Compile quindy and install in local repository (located at `./Studybits/quindy`) using `mvn install -DskipTests`
2. Import the project in Android Studio


## Running
1. Run the docker compose setup from the university-agent (located at `./StudyBits/`) (injecting your ip)
2. Create a file `settings.gradle` containing `ENDPOINT_IP="<YOUR_IP_HERE>"`
3. Build and deploy to a device

## Running tests
1. Start an emulator or connect an android device to your machine
2. Run `./.travis.sh`

