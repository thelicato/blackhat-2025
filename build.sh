#!/usr/bin/env bash

set -e

APPSDIR="apps"
KEYSTORE_PATH="./keystore.jks"
KEYSTORE_ABS_PATH=$(realpath $KEYSTORE_PATH)
KEY_ALIAS="alias_$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16; echo)"
KEYSTORE_PASSWORD=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16; echo)

# Always create a fresh keystore
function generate_keystore() {
  echo "Cleaning up any existing keystore..."
  rm -f "$KEYSTORE_PATH"

  keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_PATH" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEYSTORE_PASSWORD" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias "$KEY_ALIAS" \
    -dname "CN=BlackHat, OU=Dev, O=BlackHat, L=BlackHat, S=None, C=BH" \
    >/dev/null 2>&1

  echo "Keystore created at: $KEYSTORE_ABS_PATH"
}

function help() { 
    echo "
Usage: $(basename $0) <command>

Command (required):
  local                     Build the apps locally.
  docker                    Build the apps through Docker.
  help                      Show this help message and exit.
"

    exit 1
}

function check_last_command() {
    local status=$?
    if [[ $status -ne 0 ]]; then
        echo "Last command failed with exit code $status"
        exit $status
    fi
}

function setup_build_folder() {
  rm -rf build
  mkdir -p build
}

function local_build_vuln_app() {
  if [[ -f "$APPSDIR/vulnerable-app/gradlew" ]]; then
    echo "===================================="
    echo "Building app in: $APPSDIR"

    cd "$APPSDIR/vulnerable-app"

    # Placeholder build
    echo "Building placeholder version..."
    ./gradlew clean assembleRelease \
      -Pandroid.injected.signing.store.file=$KEYSTORE_ABS_PATH \
      -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
      -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
      -Pandroid.injected.signing.key.password=$KEYSTORE_PASSWORD
    check_last_command

    cp app/build/outputs/apk/release/app-release.apk "../../build/bh-demo-placeholder.apk"

    if [ ! -f "config.json" ]; then
        echo "config.json does not exist for this app, skipping..."
        cd ../..
        echo "Finished building vulnerable app"
        echo
        return
    fi

    # Read config
    PLACEHOLDER=$(awk -F'"' '/"placeholder"/{print $4}' config.json)
    FLAG=$(awk -F'[ :"]+' '/"flag"/{print $3}' config.json)
    FILES=$(awk -F'[][]' '/"files"/{gsub(/"|,/, "", $2); print $2}' config.json)

    echo -e "\n\n"
    # Flagged build
    echo "Replacing placeholder string with actual flag..."
    for file in $FILES; do
      echo "Modifying file: $file"
      sed -i "s/$PLACEHOLDER/${FLAG//\//\\/}/g" "$file"
    done

    echo ""
    echo "Building flagged version..."
    ./gradlew clean assembleRelease \
      -Pandroid.injected.signing.store.file=$KEYSTORE_ABS_PATH \
      -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
      -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
      -Pandroid.injected.signing.key.password=$KEYSTORE_PASSWORD
    check_last_command

    cp app/build/outputs/apk/release/app-release.apk "../../build/bh-demo-flag.apk"

    echo "Restoring modified source files..."
    for file in $FILES; do
      echo "Modifying file: $file"
      sed -i "s/$FLAG/${PLACEHOLDER//\//\\/}/g" "$file"
    done

    cd ../..
    echo "Finished building vulnerable app"
    echo
  fi
}

function local_build_exploit_app() {
  if [[ -f "$APPSDIR/exploit-app/gradlew" ]]; then
    echo "===================================="
    echo "Building exploit app in: $APPSDIR"

    cd "$APPSDIR/exploit-app"

    # Placeholder build
    echo "Building placeholder version..."
    ./gradlew clean assembleRelease \
      -Pandroid.injected.signing.store.file=$KEYSTORE_ABS_PATH \
      -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
      -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
      -Pandroid.injected.signing.key.password=$KEYSTORE_PASSWORD
    check_last_command

    cp app/build/outputs/apk/release/app-release.apk "../../build/exploit-app.apk"

    cd ../..
    echo "Finished building exploit app"
    echo
  fi
}

function main() {
    if [[ $# -lt 1 ]]; then
        echo "Error: Missing command."
        help
    fi

    COMMAND="$1"
    shift

    TARGET=""
    case "$COMMAND" in
        local|docker|help)
            # Valid command
            ;;
        *)
            echo "Error: Unknown command '$COMMAND'"
            help
            ;;
        esac

    # Help
    if [[ "$COMMAND" == "help" ]]; then
        help
    fi

    setup_build_folder

    # Local build
    if [[ "$COMMAND" == "local" ]]; then
        generate_keystore
        local_build_vuln_app
        local_build_exploit_app
    fi

    # Docker build
    if [[ "$COMMAND" == "docker" ]]; then
        docker run -it --rm -v ${PWD}:/app -w /app --entrypoint /app/build.sh alvrme/alpine-android:android-34 local
    fi
}


main "$@"