# AcoProbe
### Functions
|name|description|
|--|--|
| isConnected | Return probe is connected or not. |
| isConnecting | Return probe is connecting or not. |
| isDisconnected | Return probe is disconnected or not. |
| connect | Connect to probe. |
| disconnect | Disconnect to probe. |
| streaming | Start realtime streaming. |
| stop | Stop realtime streaming. |
| freeze | Freeze probe. |
| unFreeze | Unfreeze probe. |
| getParameters | Get gain, depth, preset values. |
| getConfig | Get probe configs. |
| getStatus | Get probe status. |
| getGain | Get probe gain value. |
| getDepth | Get probe depth value. |
| getPreset | Get probe preset value. |
| setGain | Get probe gain value. |
| setDepth | Get probe depth value. |
| setPreset | Get probe preset value. |
| observerParametersChange | Observer paramter changes. |
| removeObserverParametersChange | Stop observer paramter changes. |
| observerStatusChange | Observer status changes. |
| removeObserverStatusChange | Stop observer status changes. |
| observerConnectStatus | Observer connect status changes. |
| removeObserverConnectStatus | Stop observer connect status changes. |

### Varable
|name|description|
|--|--|
| onStreamingListener | Set a listener to get realtime images. |
| onErrorListener | Set a listener to get realtime errors. |