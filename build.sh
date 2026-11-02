#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT="$(cd "$(dirname "$0")" && pwd)"
SDK="$HOME/android-sdk"
ANDROID_JAR="$SDK/platforms/android-35/android.jar"
R8_JAR="$SDK/r8.jar"
BUILD="$PROJECT/build"
KEYSTORE="$PROJECT/keystore/fluxo.jks"
KEY_ALIAS="fluxo"
KEY_PROPS="$PROJECT/keystore/keystore.properties"
APK_OUT="$PROJECT/Fluxo.apk"

# A senha fica em keystore/keystore.properties (ignorado pelo git).
# Se não existir, uma senha aleatória é gerada no primeiro build.
if [ -f "$KEY_PROPS" ]; then
  # shellcheck disable=SC1090
  . "$KEY_PROPS"
fi
if [ -z "${KEY_PASS:-}" ]; then
  KEY_PASS="$(head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)"
  mkdir -p "$PROJECT/keystore"
  printf 'KEY_PASS=%s\n' "$KEY_PASS" > "$KEY_PROPS"
  chmod 600 "$KEY_PROPS"
fi

if [ ! -f "$ANDROID_JAR" ]; then
  echo "android.jar não encontrado em $ANDROID_JAR"
  exit 1
fi
if [ ! -f "$R8_JAR" ]; then
  echo "r8.jar não encontrado em $R8_JAR"
  exit 1
fi

echo "== limpando"
rm -rf "$BUILD"
mkdir -p "$BUILD/gen" "$BUILD/classes" "$BUILD/dex"

echo "== compilando recursos"
aapt2 compile --dir "$PROJECT/res" -o "$BUILD/res.zip"

echo "== linkando recursos"
aapt2 link \
  -o "$BUILD/unsigned.apk" \
  -I "$ANDROID_JAR" \
  --manifest "$PROJECT/AndroidManifest.xml" \
  -R "$BUILD/res.zip" \
  --java "$BUILD/gen" \
  --min-sdk-version 26 \
  --target-sdk-version 35 \
  --version-code 1 \
  --version-name 1.0 \
  --auto-add-overlay

echo "== compilando Java"
javac --release 17 -Xlint:-options \
  -classpath "$ANDROID_JAR" \
  -d "$BUILD/classes" \
  $(find "$PROJECT/src" "$BUILD/gen" -name '*.java')

echo "== gerando classes.dex (D8)"
java -cp "$R8_JAR" com.android.tools.r8.D8 \
  --release --min-api 26 \
  --lib "$ANDROID_JAR" \
  --output "$BUILD/dex" \
  $(find "$BUILD/classes" -name '*.class')

( cd "$BUILD/dex" && zip -q "$BUILD/unsigned.apk" classes.dex )

echo "== assinando"
mkdir -p "$PROJECT/keystore"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair \
    -keystore "$KEYSTORE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$KEY_PASS" -keypass "$KEY_PASS" \
    -dname "CN=Fluxo, O=Fluxo, C=BR" >/dev/null 2>&1
fi

apksigner sign \
  --ks "$KEYSTORE" \
  --ks-pass "pass:$KEY_PASS" \
  --key-pass "pass:$KEY_PASS" \
  --out "$APK_OUT" \
  "$BUILD/unsigned.apk"

apksigner verify "$APK_OUT"

echo "== ok: $APK_OUT ($(du -h "$APK_OUT" | cut -f1))"
