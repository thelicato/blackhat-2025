#!/usr/bin/env bash

set -e

APPDIR="bh-state-machine"
KEYSTORE_PATH="./keystore.jks"
KEYSTORE_ABS_PATH=$(realpath $KEYSTORE_PATH)

if [[ ! -f "$KEYSTORE_ABS_PATH" ]]; then
  echo "Keystore file not found at: $KEYSTORE_ABS_PATH"
  exit 1
fi

KEY_ALIAS=<GENERATE_RANDOMLY>
KEYSTORE_PASSWORD=<GENERATE_RANDOMLY>

function help() { 
    echo "
Usage: $(basename $0) <command>

Command (required):
  local                     Build the app locally.
  docker                    Build the app through Docker.
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

function local_build_app() {
  if [[ -f "$APPDIR/vulnerable-app/gradlew" ]]; then
    echo "===================================="
    echo "Building app in: $APPDIR"

    cd "$APPDIR/vulnerable-app"

    # Placeholder build
    echo "Building placeholder version..."
    ./gradlew clean assembleRelease \
      -Pandroid.injected.signing.store.file=$KEYSTORE_ABS_PATH \
      -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
      -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
      -Pandroid.injected.signing.key.password=$KEYSTORE_PASSWORD
    check_last_command

    cp app/build/outputs/apk/release/app-release.apk "../../build/${APPDIR%/}-placeholder.apk"

    if [ ! -f "config.json" ]; then
        echo "config.json does not exist for this app ($APPDIR), skipping..."
        cd ../..
        mv "./build/${APPDIR%/}-placeholder.apk" "./build/${APPDIR%/}.apk"
        echo "Finished building: $APPDIR"
        echo
        return
    fi

    # Read config
    PLACEHOLDER=$(jq -r '.placeholder' config.json)
    FLAG=$(jq -r '.flag' config.json)
    FILES=$(jq -r '.files[]' config.json)

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

    cp app/build/outputs/apk/release/app-release.apk "../../build/${APPDIR%/}-flag.apk"

    echo "Restoring modified source files..."
    for file in $FILES; do
      echo "Restoring file $file"
      git restore $file
    done

    cd ../..
    echo "Finished building: $APPDIR"
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
        local_build_app
    fi

    # Docker build
    if [[ "$COMMAND" == "docker" ]]; then
        docker run -it --rm -v ${PWD}:/app -w /app --entrypoint /app/build.sh alvrme/alpine-android:android-34 local
    fi
}


main "$@"