package com.geomonitor.processing.cache

import com.geomonitor.processing.model.CellTower
import com.geomonitor.processing.model.Location
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import kotlin.math.roundToInt

/**
 * Lookup/storage of resolved device and cell tower locations in PostGIS.
 */
@Repository
class LocationCache(private val jdbcTemplate: JdbcTemplate) {

    fun getDeviceLocation(deviceId: String): Location? =
        jdbcTemplate.query(
            """
            SELECT ST_Y(location::geometry), ST_X(location::geometry)
            FROM device_locations
            WHERE device_id = ?
            """.trimIndent(),
            { rs, _ -> Location(latitude = rs.getDouble(1), longitude = rs.getDouble(2)) },
            deviceId,
        ).firstOrNull()

    fun getCellTowerLocation(tower: CellTower): Location? =
        jdbcTemplate.query(
            """
            SELECT ST_Y(location::geometry), ST_X(location::geometry)
            FROM cell_tower_cache
            WHERE mcc = ? AND mnc = ? AND lac = ? AND cell_id = ?
            """.trimIndent(),
            { rs, _ -> Location(latitude = rs.getDouble(1), longitude = rs.getDouble(2)) },
            tower.mcc, tower.mnc, tower.lac, tower.cellId,
        ).firstOrNull()

    fun storeDeviceLocation(deviceId: String, location: Location, source: String) {
        jdbcTemplate.update(
            """
            INSERT INTO device_locations (device_id, location, source, last_seen)
            VALUES (?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, now())
            ON CONFLICT (device_id) DO UPDATE
                SET location = EXCLUDED.location,
                    source = EXCLUDED.source,
                    last_seen = now()
            """.trimIndent(),
            deviceId, location.longitude, location.latitude, source,
        )
    }

    fun storeCellTowerLocation(tower: CellTower, location: Location) {
        jdbcTemplate.update(
            """
            WITH pt AS (
                SELECT ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography AS geog, ?::integer AS range_m
            )
            INSERT INTO cell_tower_cache (mcc, mnc, lac, cell_id, location, range_m, coverage, resolved_at)
            SELECT ?, ?, ?, ?, pt.geog, pt.range_m, ST_Buffer(pt.geog, pt.range_m), now()
            FROM pt
            ON CONFLICT (mcc, mnc, lac, cell_id) DO UPDATE
                SET location = EXCLUDED.location,
                    range_m = EXCLUDED.range_m,
                    coverage = EXCLUDED.coverage,
                    resolved_at = now()
            """.trimIndent(),
            location.longitude, location.latitude, location.accuracyMeters?.roundToInt(),
            tower.mcc, tower.mnc, tower.lac, tower.cellId,
        )
    }
}
