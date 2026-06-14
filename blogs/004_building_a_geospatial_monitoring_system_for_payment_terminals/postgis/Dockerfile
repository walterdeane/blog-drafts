FROM postgis/postgis:16-3.4

# shp2pgsql is used by postgis/init/003_load_boundaries.sh to seed the
# boundary tables from the shapefiles checked into src-data/.
RUN apt-get update \
    && apt-get install -y --no-install-recommends postgis \
    && rm -rf /var/lib/apt/lists/*
