# Eversense E3 / 365 for AAPS 4

Ported from the local CAPTCG checkout, branch `florians-dana-fix`, commit `bf3acad275c9aa3049fa30f897d49b62414f0c62`. Original project: https://github.com/CAPTCG/AndroidAPS-Eversense-/tree/florians-dana-fix.

The Android backend includes BLE discovery, E3/365 authentication, reconnect and sync handling, packet codecs, calibration, placement mode and optional Eversense cloud upload. The source adapter registers with Metro and stores sensor values and trends through AAPS persistence. Device status, placement and calibration screens are available in the plugin settings. The shared Compose UI provides a calibration quick action and a 15-minute countdown after a successful submission.

CAPTCG limits remain: E3 has no history backfill. Initial sensor setup and alarm-threshold setup still require the official app. The four stored glucose target/alert keys from PR #191 are retained for compatibility but do not configure a transmitter.

The port keeps the CAPTCG credential keys. The legacy `EversenseCGMManager` state is imported once into the registered, non-exported `eversense_device_state_json` preference in AAPS 4 storage; settings import preserves it. The old file is kept. Cloud upload keeps its original enabled default. BLE and cloud support are Android only; shared screens hide the calibration action when no Eversense source is active.

Builds and local tests do not verify a physical transmitter. E3/365 connection, reconnect, calibration, placement and cloud behavior still need device checks.

## Connection lifecycle

`EversenseConnectionController` owns connection intent, one retry timer, a three-minute connection/authentication deadline and a seven-minute data watchdog. Retries back off from five to sixty seconds; E3 status 19 waits thirty seconds. Normal 365 status-19 reconnects reuse the GATT handle. A manual disconnect remains paused until an explicit connect; stopping the source closes GATT, cancels timers, stops scanning and shuts down both executors.

Every fresh GATT client receives a session identifier. State access and Android callbacks are guarded by that session. A separate link generation rejects queued work and HTTP results from an interrupted 365 link even when its GATT handle is reused. Packet completion is registered before sending; a timeout closes the link before another command can consume a late response. BLE writes share one executor and a packet exchange lock.

Credential/region changes invalidate the old session and token cache before reconnecting. Sign-out also clears the typed AAPS credential preferences. Placement polling belongs to the resumed screen and leaves diagnostic mode during cancellation. Scans have a ten-second backend deadline and an owner-specific cancellation handle.

Regression tests cover connection deadlines, backoff, stale callbacks, manual pause, 365 GATT reuse, stale state writes, early packet responses and placement cancellation. Physical transmitter and battery measurements remain necessary.

## Optional hourly cloud delivery and offline operation

Glucose is persisted to AAPS before optional cloud work is queued. BLE reception, calibration and local AAPS processing do not wait for uploads or cloud-token refreshes. A cloud failure does not request a BLE reconnect. In airplane mode Bluetooth must remain enabled; missing internet/validated connectivity skips cloud attempts.

Cloud readings are deduplicated and buffered in the separate, non-exported `eversense_cloud_queue_json` preference. At most one upload cycle starts per hour, including failed attempts; the attempt timestamp survives process restarts. A cycle sends the accumulated readings to the existing model-specific endpoints and updates portal status with the newest reading. Successful batches are removed; failures remain pending. The next reading after connectivity returns can trigger a due cycle. There is no separate wake-up timer. The cloud buffer retains up to seven days / 2016 readings; this does not limit AAPS glucose history. Sign-out or account/region changes clear pending cloud batches. 365 history entries without raw BLE payload stay in AAPS but cannot be sent through the existing raw-data cloud endpoint.

An already registered ES365 identity can establish a new BLE session using stored keys without cloud access, even when its cloud access token has expired. Offline connection/decryption failures retain that identity for another local handshake. Initial 365 registration, missing keys, or registration genuinely invalidated by the transmitter still require the manufacturer service; this protocol requirement cannot be replaced by the offline upload queue. Physical flight-mode/reconnect/loop tests remain necessary.

## Logging

The Eversense settings provide a separate **Diagnostic logging** switch, off by default and excluded from settings exports/imports. It changes immediately while the source is active. Normal logging keeps important connection, calibration, alarm, HTTP status and error events. Packet dumps, individual readings, full state dumps, detailed sync traces, HTTP response bodies and exception stack traces require diagnostic logging. Disabled diagnostic messages are formatted lazily; the logger no longer allocates a stack trace for each log line. The setting does not enable the transmitter placement/diagnostic mode. Symmetric session keys are never logged, including in diagnostic mode. Existing log archives are unchanged.
