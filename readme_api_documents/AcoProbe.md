# AcoProbe
### Functions
|name|description|
|--|--|
| isConnected | Return probe is connected or not. |
| isConnecting | Return probe is connecting or not. |
| isDisconnected | Return probe is disconnected or not. |
| connect | Connect to probe with a license key. By default (`remember = true`) the key is saved for this probe's serial number if valid. |
| hasSavedLicenseKey | Whether a license key was previously remembered for this probe. |
| connectWithSavedLicenseKey | Connect using the remembered license key, if any. Returns false (and forgets the key) if none was saved or it's no longer valid. |
| forgetLicenseKey | Forget the license key remembered for this probe. |
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
| setScanMode | Switch the probe's scan mode (B / Color / Power). |
| observerScanModeChange | Observer scan mode changes. |
| getCfmGain / setCfmGain | Get/set Color (CFM) gain. |
| getCfmScale / setCfmScale | Get/set Color scale (PRF). |
| getCfmBaseLine / setCfmBaseLine | Get/set Color baseline. |
| setCfmRoi | Set the Color/Power ROI box (see AcoProbeRoi for the coordinate ranges). |
| getCfmColorMapColors | Get the color map (one 0xRRGGBB entry per step, top to bottom) used to paint Color / Power flow, for drawing a color bar. Null outside Color / Power mode. |

### Data Models
|name|description|
|--|--|
| AcoUltrasoundScanMode | Enum of scan modes: B_MODE, COLOR_MODE, POWER_MODE. |
| AcoProbeParameters.cfm | AcoProbeCfmParameters: gain/scale/baseline, each an AcoProbeParameter (max/min/step/value/menu). |
| AcoProbeRoi | ROI box (x1,y1,x2,y2), used by setCfmRoi. x1/x2 are scan-line indexes, 0 to 255 (256 scan lines). y1/y2 are depths in the same unit as the depth parameter (mm), 0 to the current depth value, so the upper limit changes with depth. |

### Varable
|name|description|
|--|--|
| onStreamingListener | Set a listener to get realtime images. |
| onErrorListener | Set a listener to get realtime errors. |
| streamingMutableLiveData | LiveData<Boolean> that is `true` while the probe is streaming. Observe it to follow the streaming state, or read `.value` to check whether `streaming()` was already called. |
| onProbeConditionListener | Typed probe-condition events: FirmwareError (raw per-frame ErrorStatus code + message), ThermalThrottling (probe hot and delivery well below the requested rate), TemperatureHigh (approaching firmware cutoff), Recovered. The probe throttles frame generation as it heats (observed: full rate ~32C, throttling from ~40C, near-halt ~46C); pause scanning (freeze) to let it cool — the vendor app enforces a 5-minute auto-freeze for this reason. |