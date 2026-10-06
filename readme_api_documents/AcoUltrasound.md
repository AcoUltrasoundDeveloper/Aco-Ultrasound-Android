# AcoUltrasound
### Functions
|name|description|
|--|--|
| initialize | Initialize the sdk to make it work. |
| destroy | Release all sdk resources. |
| getConnectedProbe | Get currently connected probe. |
| startDiscoverProbes | Start discover probe. |
| stopDiscoverProbes | Stop discover probe. Called automatically when a probe connects (see AcoProbe.connect); call this directly only if you need to stop discovery before that. |
| isDiscoveringProbes | Is discovering Probes or not. |
| registerProbeDiscoveredListener | Register a listener for discovering Probes. |
| unregisterProbeDiscoveredListener | Unregister a listener for discovering Probes. |
| isDiscoveringProbes | Is discovering Probes or not. |