# `505.csv` Column Reference

<!-- markdownlint-disable MD033 -- <br> used for line breaks within table cells -->

Sourced from from https://opencellid.org/

| Parameter | Data type | Description |
| --- | --- | --- |
| `radio` | string | Network type. One of the strings `GSM`, `UMTS`, `LTE` or `CDMA`. |
| `mcc` | integer | Mobile Country Code, for example `260` for Poland. |
| `net` | integer | For GSM, UMTS and LTE networks, this is the Mobile Network Code (MNC).<br>For CDMA networks, this is the System IDentification number (SID). |
| `area` | integer | Location Area Code (LAC) for GSM and UMTS networks.<br>Tracking Area Code (TAC) for LTE networks.<br>Network IDentification number (NID) for CDMA networks. |
| `cell` | integer | Cell ID (CID) for GSM and LTE networks.<br>UTRAN Cell ID / LCID for UMTS networks, which is the concatenation of 2 or 4 bytes of Radio Network Controller (RNC) code and 4 bytes of Cell ID.<br>Base station IDentifier number (BID) for CDMA networks. |
| `unit` | integer | Primary Scrambling Code (PSC) for UMTS networks.<br>Physical Cell ID (PCI) for LTE networks.<br>Empty for GSM and CDMA networks. |
| `lon` | double | Longitude in degrees between -180.0 and 180.0.<br>`changeable=1`: average of longitude values of all related measurements.<br>`changeable=0`: exact GPS position of the cell tower. |
| `lat` | double | Latitude in degrees between -90.0 and 90.0.<br>`changeable=1`: average of latitude values of all related measurements.<br>`changeable=0`: exact GPS position of the tower. |
| `range` | integer | Estimate of cell range, in meters. |
| `samples` | integer | Total number of measurements assigned to the cell tower. |
| `changeable` | integer | Defines if coordinates of the cell tower are exact or approximate.<br>`changeable=1`: the GPS position has been calculated from all available measurements.<br>`changeable=0`: the GPS position is precise - no measurements have been used to calculate it. |
| `created` | integer | The first time the cell tower was seen and added to the OpenCellID database.<br>A timestamp: number of seconds since the UTC Unix Epoch (`1970-01-01T00:00:00Z`).<br>For example `1409522613` is the timestamp for `2014-08-31T22:03:33Z`. |
| `updated` | integer | The last time the cell tower was seen and updated.<br>A timestamp: number of seconds since the UTC Unix Epoch (`1970-01-01T00:00:00Z`).<br>For example `1409522613` is the timestamp for `2014-08-31T22:03:33Z`. |
| `averageSignal` | integer | Average signal strength from all assigned measurements for the cell.<br>Either in dBm or as defined in TS 27.007 8.5 - both are accepted. |
