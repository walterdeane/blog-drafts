# Australia Map Layers — Data Sources

This project uses three datasets to build a base map with Australian administrative and locality detail.

## 1. World Basemap — Natural Earth

- **Source:** https://www.naturalearthdata.com/downloads/
- **Layer:** Admin 0 – Countries (1:10m scale recommended for detail)
- **Format:** Shapefile
- **License:** Public domain
- **Notes:** Provides world country boundaries/coastlines as the background layer. Use the 1:10m resolution for closer zoom levels; 1:50m or 1:110m if file size/performance is a concern for a world-scale view.

## 2. Local Government Areas (LGAs) — ABS ASGS

- **Source:** Australian Bureau of Statistics, Australian Statistical Geography Standard (ASGS) Edition 3
- **Page:** https://www.abs.gov.au/statistics/standards/australian-statistical-geography-standard-asgs/edition-3-july-2021-june-2026/access-and-downloads/digital-boundary-files
- **Layer:** LGA (Local Government Areas), latest available year (e.g. LGA_2024_AUST_GDA2020 or LGA_2025_AUST_GDA2020)
- **Format:** ESRI Shapefile or GeoPackage
- **CRS:** GDA2020 (recommended) or GDA94
- **License:** Creative Commons Attribution 4.0 International, © Commonwealth of Australia (ABS)
- **Notes:** Covers all of Australia. Choose GDA2020 to match modern standards; GDA94 differs by ~1.8m on the ground.

## 3. Suburbs and Localities (SAL) — ABS ASGS

- **Source:** Australian Bureau of Statistics, ASGS Edition 3 (same publication as LGAs)
- **Page:** https://www.abs.gov.au/statistics/standards/australian-statistical-geography-standard-asgs/edition-3-july-2021-june-2026/access-and-downloads/digital-boundary-files
- **Layer:** SAL (Suburbs and Localities)
- **Format:** ESRI Shapefile or GeoPackage
- **CRS:** GDA2020 (recommended) or GDA94, matching the LGA layer
- **License:** Creative Commons Attribution 4.0 International, © Commonwealth of Australia (ABS)
- **Notes:** Provides suburb/locality boundaries and names across Australia — a lighter, more relevant alternative to the full Gazetteer of Australia for city/suburb labelling.

## Setup Notes

- Ensure LGA and SAL layers use the same CRS (GDA2020 recommended) for alignment.
- Natural Earth data is in WGS84 (EPSG:4326); reproject if needed to match GDA2020 (EPSG:7844) for consistency with ABS layers.
- All ABS datasets require attribution per the CC BY 4.0 licence.
