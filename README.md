# Samsung TV Remote (Android)

Pilot do telewizora Samsung przez usługę [`samsung-tv-remote`](https://github.com/michalkulik/samsung-tv-remote)
(FastAPI na hoście `192.168.68.19:9039`, ten sam protokół co SmartThings / Home Assistant).

Aplikacja w natywnym **Material Design 3** (Jetpack Compose). Po odpaleniu pokazuje pilota:
D-pad z OK, power (toggle / WoL / off), source, home, cofnij, głośność, P+/P-, numer kanału,
media, App ID, parowanie oraz ustawienia połączenia (adres usługi + opcjonalny token API).

## Widgety (ekran startowy)

| Widget | Zawartość |
|---|---|
| **TV Pilot (pełny)** | D-pad + OK, cofnij, home, vol+/vol−, mute, P+/P−, power, source |
| **TV Power** | tylko power (toggle) |
| **TV Power+Source+OK** | power, source, OK |

Widgety nie mają pól tekstowych (Glance nie wspiera inputu) — numer kanału i App ID tylko z aplikacji.
Każdy przycisk wykonuje jedno wywołanie API; kropka pokazuje osiągalność usługi.

## Wymagania

* Android 8.0+ (`minSdk 26`), target API 36.
* Telefon i host usługi w **tej samej sieci LAN** (ograniczenie Samsunga).
* Domyślny adres usługi: `192.168.68.19:9039` (do zmiany w Ustawieniach w aplikacji).

## Budowanie

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Release jest podpisany commited keystore (`keystore/samsung-tv-remote-release.jks`,
hasła w `gradle.properties`) — każdy build daje ten sam podpis, APK instaluje się
jeden na drugim. Wydania publikuje workflow `Release` po pushu tagu `v*`
(`softprops/action-gh-release`), build sprawdza workflow `Build` na każdy push do `main`.

## API

Pełna dokumentacja usługi: `http://192.168.68.19:9039/docs`.
Aplikacja używa: `/api/status`, `/api/power/{toggle,on,off}`, `/api/nav/*`,
`/api/volume/*`, `/api/mute`, `/api/channel*`, `/api/media/*`, `/api/key`,
`/api/apps/run`, `/api/pair`.
