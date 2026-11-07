# Eversense E3 / 365 for AAPS 4

Ported from the local CAPTCG checkout, branch `florians-dana-fix`, commit `bf3acad275c9aa3049fa30f897d49b62414f0c62`. Original project: https://github.com/CAPTCG/AndroidAPS-Eversense-/tree/florians-dana-fix.

The Android backend includes BLE discovery, E3/365 authentication, reconnect and sync handling, packet codecs, calibration, placement mode and optional Eversense cloud upload. The source adapter registers with Metro and stores sensor values and trends through AAPS persistence. Device status, placement and calibration screens are available in the plugin settings. The shared Compose UI provides a calibration quick action and a 15-minute countdown after a successful submission.

CAPTCG limits remain: E3 has no history backfill. Initial sensor setup and alarm-threshold setup still require the official app. The four stored glucose target/alert keys from PR #191 are retained for compatibility but do not configure a transmitter.

The port keeps the CAPTCG credential keys. The legacy `EversenseCGMManager` state is imported once into the registered, non-exported `eversense_device_state_json` preference in AAPS 4 storage; settings import preserves it. The old file is kept. Cloud upload keeps its original enabled default. BLE and cloud support are Android only; shared screens hide the calibration action when no Eversense source is active.

Builds and local tests do not verify a physical transmitter. E3/365 connection, reconnect, calibration, placement and cloud behavior still need device checks.
