// Shared helpers and initializers for the /demo/* Leaflet pages.

const AUSTRALIA_CENTER = [-25.27, 133.78];
const DEFAULT_ZOOM = 4;
const MIN_ZOOM = 3;

const STATUS_LABELS = {
    online: 'Online',
    expected_offline: 'Expected offline',
    unexpected_offline: 'Unexpected offline',
};

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

function createDemoMap(containerId) {
    const map = L.map(containerId, {
        center: AUSTRALIA_CENTER,
        zoom: DEFAULT_ZOOM,
        minZoom: MIN_ZOOM,
        zoomControl: false,
    });

    L.control.zoom({position: 'topleft'}).addTo(map);
    L.control.scale({position: 'bottomleft', imperial: false}).addTo(map);

    return map;
}

function addBasemap(map, wmsUrl) {
    return L.tileLayer.wms(wmsUrl, {
        layers: 'geospatial-monitoring:demo-basemap',
        format: 'image/png',
        transparent: true,
        version: '1.1.0',
    }).addTo(map);
}

function wmsOverlay(wmsUrl, layers, options) {
    return L.tileLayer.wms(wmsUrl, Object.assign({
        layers: layers,
        format: 'image/png',
        transparent: true,
        version: '1.1.0',
    }, options || {}));
}

function addLoadingIndicator(map) {
    const shell = document.querySelector('.demo-map-shell');
    if (!shell) return;

    const overlay = document.createElement('div');
    overlay.className = 'map-loading';
    overlay.setAttribute('role', 'status');
    overlay.innerHTML = '<span class="map-loading-spinner"></span><span>Loading map layers…</span>';
    shell.appendChild(overlay);

    let pending = 0;
    let hideTimer;

    function show() {
        clearTimeout(hideTimer);
        overlay.classList.remove('is-hidden');
    }

    function hideSoon() {
        clearTimeout(hideTimer);
        hideTimer = setTimeout(function () {
            if (pending === 0) {
                overlay.classList.add('is-hidden');
            }
        }, 250);
    }

    map.on('tileloadstart', function () {
        pending += 1;
        show();
    });

    map.on('tileload tileerror tileabort', function () {
        pending = Math.max(0, pending - 1);
        hideSoon();
    });

    map.whenReady(hideSoon);
}

function addLegendControl(map, html) {
    const legend = L.control({position: 'bottomright'});
    legend.onAdd = function () {
        const div = L.DomUtil.create('div', 'legend');
        div.innerHTML = html;
        L.DomEvent.disableClickPropagation(div);
        return div;
    };
    legend.addTo(map);
    return legend;
}

function addLayerControl(map, overlays, options) {
    const control = L.control.layers(null, overlays, Object.assign({
        collapsed: false,
        position: 'topright',
    }, options || {}));
    control.addTo(map);
    return control;
}

function addDateControl(map, dates, onChange) {
    const control = L.control({position: 'topleft'});
    control.onAdd = function () {
        const div = L.DomUtil.create('div', 'date-control leaflet-bar');
        const label = document.createElement('label');
        label.htmlFor = 'sales-date';
        label.textContent = 'Day';
        const select = document.createElement('select');
        select.id = 'sales-date';
        dates.forEach(function (date) {
            const option = document.createElement('option');
            option.value = date;
            option.textContent = new Date(date + 'T00:00:00').toLocaleDateString(undefined, {
                weekday: 'short',
                day: 'numeric',
                month: 'short',
            });
            select.appendChild(option);
        });
        select.addEventListener('change', function () {
            onChange(select.value);
        });
        div.appendChild(label);
        div.appendChild(select);
        L.DomEvent.disableClickPropagation(div);
        return div;
    };
    control.addTo(map);
    return control;
}

function openPopup(map, latlng, content) {
    L.popup({className: 'demo-popup', maxWidth: 320, minWidth: 220})
        .setLatLng(latlng)
        .setContent(content)
        .openOn(map);
}

function queryFeature(map, layer, latlng) {
    return fetch(getFeatureInfoUrl(map, layer, latlng)).then(function (res) {
        return res.json();
    });
}

function statusBadge(status) {
    const label = STATUS_LABELS[status] || status;
    return '<span class="badge status-' + status + '">' + label + '</span>';
}

function deviceProperties(deviceId, props) {
    const statusLabel = STATUS_LABELS[props.connectivity_status] || props.connectivity_status;
    return '<div class="popup">' +
        '<div class="popup-header">' +
        '<strong>' + deviceId + '</strong>' +
        statusBadge(props.connectivity_status) +
        '</div>' +
        '<dl class="popup-details">' +
        '<div><dt>Merchant</dt><dd>' + (props.merchant_category_name || '—') + '</dd></div>' +
        '<div><dt>Status</dt><dd><span class="status-' + props.connectivity_status + '">' + statusLabel + '</span></dd></div>' +
        '<div><dt>Network</dt><dd>' + (props.network_provider || 'Unknown') + ' · ' + (props.network_interface || 'Unknown') + '</dd></div>' +
        '<div><dt>Battery</dt><dd>' + props.battery_pct + '%</dd></div>' +
        '<div><dt>Last seen</dt><dd>' + new Date(props.last_seen).toLocaleString() + '</dd></div>' +
        '</dl>' +
        '<a class="popup-link" href="/support/devices/' + deviceId + '">View device details →</a>' +
        '</div>';
}

function cellTowerProperties(props) {
    return '<div class="popup">' +
        '<div class="popup-header">' +
        '<strong>Cell tower</strong>' +
        '<span class="badge carrier-' + (props.carrier || 'other').toLowerCase() + '">' + props.carrier + '</span>' +
        '</div>' +
        '<dl class="popup-details">' +
        '<div><dt>MCC / MNC</dt><dd>' + props.mcc + ' / ' + props.mnc + '</dd></div>' +
        '<div><dt>LAC / Cell ID</dt><dd>' + props.lac + ' / ' + props.cell_id + '</dd></div>' +
        '<div><dt>Range</dt><dd>' + props.range_m + ' m</dd></div>' +
        '<div><dt>Resolved</dt><dd>' + new Date(props.resolved_at).toLocaleString() + '</dd></div>' +
        '</dl>' +
        '</div>';
}

function salesProperties(deviceId, props) {
    const salesTotal = (props.sales_total_cents / 100).toLocaleString(undefined, {
        style: 'currency',
        currency: 'AUD',
    });
    return '<div class="popup">' +
        '<div class="popup-header">' +
        '<strong>' + deviceId + '</strong>' +
        '</div>' +
        '<dl class="popup-details">' +
        '<div><dt>Merchant</dt><dd>' + (props.merchant_category_name || '—') + '</dd></div>' +
        '<div><dt>Date</dt><dd>' + props.summary_date + '</dd></div>' +
        '<div><dt>Sales total</dt><dd>' + salesTotal + '</dd></div>' +
        '<div><dt>Transactions</dt><dd>' + props.transaction_count + '</dd></div>' +
        '</dl>' +
        '<a class="popup-link" href="/support/devices/' + deviceId + '">View device details →</a>' +
        '</div>';
}

function initTerminalStatusMap(wmsUrl) {
    const map = createDemoMap('map');
    addBasemap(map, wmsUrl);
    addLoadingIndicator(map);

    function statusLayer(status) {
        return wmsOverlay(wmsUrl, 'geospatial-monitoring:device_locations', {
            styles: 'terminal-status',
            cql_filter: 'connectivity_status=\'' + status + '\'',
        });
    }

    const online = statusLayer('online').addTo(map);
    const expectedOffline = statusLayer('expected_offline').addTo(map);
    const unexpectedOffline = statusLayer('unexpected_offline').addTo(map);

    addLayerControl(map, {
        'Online': online,
        'Expected offline': expectedOffline,
        'Unexpected offline': unexpectedOffline,
    });

    const deviceQueryLayer = wmsOverlay(wmsUrl, 'geospatial-monitoring:device_locations');

    map.on('click', function (e) {
        queryFeature(map, deviceQueryLayer, e.latlng).then(function (data) {
            if (!data.features || data.features.length === 0) return;
            const feature = data.features[0];
            const deviceId = feature.id.split('.')[1];
            openPopup(map, e.latlng, deviceProperties(deviceId, feature.properties));
        });
    });

    addLegendControl(map,
        '<strong>Connectivity status</strong>' +
        '<div><span class="swatch circle" style="background:#2E7D32"></span>Online</div>' +
        '<div><span class="swatch circle" style="background:#EF6C00"></span>Expected offline</div>' +
        '<div><span class="swatch circle" style="background:#C62828"></span>Unexpected offline</div>'
    );
}

function initTerminalNetworkMap(wmsUrl) {
    const map = createDemoMap('map');
    addBasemap(map, wmsUrl);
    addLoadingIndicator(map);

    const coverage = wmsOverlay(wmsUrl, 'geospatial-monitoring:cell_tower_carrier_coverage', {
        styles: 'cell-tower-coverage',
    });

    const towers = wmsOverlay(wmsUrl, 'geospatial-monitoring:cell_tower_carrier_points', {
        styles: 'cell-tower-carrier',
    }).addTo(map);

    const terminals = wmsOverlay(wmsUrl, 'geospatial-monitoring:device_locations', {
        styles: 'terminal-provider',
    }).addTo(map);

    addLayerControl(map, {
        'Terminals (SIM provider)': terminals,
        'Cell towers': towers,
        'Cell tower coverage': coverage,
    });

    function queryTowers(latlng) {
        if (!map.hasLayer(towers)) return;
        queryFeature(map, towers, latlng).then(function (data) {
            if (!data.features || data.features.length === 0) return;
            openPopup(map, latlng, cellTowerProperties(data.features[0].properties));
        });
    }

    map.on('click', function (e) {
        if (!map.hasLayer(terminals)) {
            queryTowers(e.latlng);
            return;
        }
        queryFeature(map, terminals, e.latlng).then(function (data) {
            if (data.features && data.features.length > 0) {
                const feature = data.features[0];
                const deviceId = feature.id.split('.')[1];
                openPopup(map, e.latlng, deviceProperties(deviceId, feature.properties));
                return;
            }
            queryTowers(e.latlng);
        });
    });

    addLegendControl(map,
        '<div class="legend-section">' +
        '<strong>Terminals (SIM provider)</strong>' +
        '<div><span class="swatch circle" style="background:#00549F"></span>Telstra</div>' +
        '<div><span class="swatch circle" style="background:#F26522"></span>Optus</div>' +
        '<div><span class="swatch circle" style="background:#E60000"></span>Vodafone</div>' +
        '<div><span class="swatch circle" style="background:#6A1B9A"></span>TPG</div>' +
        '<div><span class="swatch circle" style="background:#888888"></span>Other</div>' +
        '</div>' +
        '<div class="legend-section">' +
        '<strong>Cell towers / coverage</strong>' +
        '<div><span class="swatch triangle" style="background:#0B3D6B"></span>' +
        '<span class="swatch translucent" style="background:#0B3D6B"></span>Telstra</div>' +
        '<div><span class="swatch triangle" style="background:#B84A12"></span>' +
        '<span class="swatch translucent" style="background:#B84A12"></span>Optus</div>' +
        '<div><span class="swatch triangle" style="background:#4B5563"></span>' +
        '<span class="swatch translucent" style="background:#4B5563"></span>Other</div>' +
        '</div>'
    );
}

function initSalesByDayMap(wmsUrl, dates) {
    const map = createDemoMap('map');
    addBasemap(map, wmsUrl);
    addLoadingIndicator(map);

    const sales = wmsOverlay(wmsUrl, 'geospatial-monitoring:device_sales_by_day', {
        styles: 'sales-by-day',
        viewparams: 'date:' + dates[0],
    }).addTo(map);

    addDateControl(map, dates, function (date) {
        sales.setParams({viewparams: 'date:' + date});
    });

    map.on('click', function (e) {
        queryFeature(map, sales, e.latlng).then(function (data) {
            if (!data.features || data.features.length === 0) return;
            const feature = data.features[0];
            openPopup(map, e.latlng, salesProperties(feature.properties.device_id, feature.properties));
        });
    });

    addLegendControl(map,
        '<strong>Sales per terminal (day)</strong>' +
        '<div><span class="swatch circle" style="background:#EDF8E9"></span>Under $2,000</div>' +
        '<div><span class="swatch circle" style="background:#BAE4B3"></span>$2,000 - $5,000</div>' +
        '<div><span class="swatch circle" style="background:#74C476"></span>$5,000 - $10,000</div>' +
        '<div><span class="swatch circle" style="background:#31A354"></span>$10,000 - $15,000</div>' +
        '<div><span class="swatch circle" style="background:#006D2C"></span>$15,000 and over</div>'
    );
}
