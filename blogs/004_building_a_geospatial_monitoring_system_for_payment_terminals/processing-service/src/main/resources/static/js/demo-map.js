// Shared helpers for the /demo/* Leaflet pages: WMS GetFeatureInfo lookups and
// popup content for terminals and cell towers.

function getFeatureInfoUrl(map, layer, latlng) {
    const point = map.latLngToContainerPoint(latlng, map.getZoom());
    const size = map.getSize();
    const params = L.Util.extend({}, layer.wmsParams, {
        request: 'GetFeatureInfo',
        srs: 'EPSG:4326',
        bbox: map.getBounds().toBBoxString(),
        width: size.x,
        height: size.y,
        x: Math.round(point.x),
        y: Math.round(point.y),
        info_format: 'application/json',
        feature_count: 5,
        buffer: 16,
    });
    params.query_layers = params.layers;
    return layer._url + L.Util.getParamString(params, layer._url, true);
}

const STATUS_LABELS = {
    online: 'Online',
    expected_offline: 'Expected offline',
    unexpected_offline: 'Unexpected offline',
};

function deviceProperties(deviceId, props) {
    const statusLabel = STATUS_LABELS[props.connectivity_status] || props.connectivity_status;
    return '<div class="popup">' +
        '<strong>' + deviceId + '</strong>' +
        '<div>' + (props.merchant_category_name || '') + '</div>' +
        '<div>Status: <span class="status-' + props.connectivity_status + '">' + statusLabel + '</span></div>' +
        '<div>Network: ' + (props.network_provider || 'Unknown') + ' (' + (props.network_interface || 'Unknown') + ')</div>' +
        '<div>Battery: ' + props.battery_pct + '%</div>' +
        '<div>Last seen: ' + new Date(props.last_seen).toLocaleString() + '</div>' +
        '<a href="/support/devices/' + deviceId + '">View details</a>' +
        '</div>';
}

function cellTowerProperties(props) {
    return '<div class="popup">' +
        '<strong>Cell tower (' + props.carrier + ')</strong>' +
        '<div>MCC/MNC: ' + props.mcc + '/' + props.mnc + '</div>' +
        '<div>LAC: ' + props.lac + ', Cell ID: ' + props.cell_id + '</div>' +
        '<div>Range: ' + props.range_m + ' m</div>' +
        '<div>Resolved: ' + new Date(props.resolved_at).toLocaleString() + '</div>' +
        '</div>';
}
