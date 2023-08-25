#!/system/bin/sh
MODDIR=${0%/*}

package_name="__PKG_NAME__"
version="__VERSION__"

rm "$MODDIR/log"

until [ "$(getprop sys.boot_completed)" = 1 ]; do sleep 5; done
sleep 5

base_path="$MODDIR/$package_name.apk"
stock_path="$(pm path "$package_name" | grep base | sed 's/package://g')"

echo "base_path: $base_path" >> "$MODDIR/log"
echo "stock_path: $stock_path" >> "$MODDIR/log"

stock_version="$(dumpsys package "$package_name" | grep versionName | cut -d "=" -f2)"

echo "version: $version" >> "$MODDIR/log"
echo "stock_version: $stock_version" >> "$MODDIR/log"

if [ "$version" = "$stock_version" ]; then

  [ -n "$stock_path" ] && mount -o bind "$base_path" "$stock_path" >> "$MODDIR/log"

else
  echo "Not mounted as versions don't match" >> "$MODDIR/log"
fi