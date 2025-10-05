# doc


https://valhalla.openstreetmap.de/route

Address geocoding: Valhalla does not geocode addresses. You must convert addresses 
to coordinates first (use a geocoding service), then send the lat/lon to Valhalla /route.


```terminal
mdir valhalla/osm/
curl -L -o /Users/toregardandersen/Documents/dev/Optimize/docker-valhalla/valhalla/osm/norway-latest.osm.pbf https://download.geofabrik.de/europe/norway-latest.osm.pbf
```

/valhalla
```text
docker-compose.yml
```

```yaml
services:
  valhalla:
    image: ghcr.io/valhalla/valhalla:latest
    container_name: valhalla
    ports:
      - "8002:8002"
    volumes:
      - ./valhalla:/data
    command: >
      bash -lc "
      set -e;
      if [ ! -f /data/valhalla.json ]; then
        echo 'Creating config...';
        valhalla_build_config --mjolnir-tile-dir /data/valhalla_tiles --mjolnir-tile-extract /data/valhalla_tiles.tar > /data/valhalla.json;
      fi;
      if [ ! -d /data/valhalla_tiles ]; then
        echo 'Building tiles...';
        valhalla_build_tiles -c /data/valhalla.json /data/osm/norway-latest.osm.pbf;
        valhalla_add_predicted_traffic -c /data/valhalla.json || true;
        valhalla_build_admins -c /data/valhalla.json || true;
        valhalla_build_timezones -c /data/valhalla.json || true;
        tar -C /data -cf /data/valhalla_tiles.tar valhalla_tiles;
      fi;
      echo 'Starting service...';
      valhalla_service /data/valhalla.json 1"
```


```terminal
docker compose up -d && docker compose logs -f valhalla
```


post
http://localhost:8002/locate
```json
{ "locations":[{"lat":59.8709,"lon":10.6637},{"lat":59.9106,"lon":10.7276}] }
```

Result

Begge punkter lokaliseres fint, så nettverket finnes. Problemet er “multimodal” uten GTFS.

```json
[
    {
        "input_lat": 59.8709,
        "input_lon": 10.6637,
        "edges": [
            {
                "way_id": 962977818,
                "correlated_lat": 59.869659,
                "correlated_lon": 10.66207,
                "side_of_street": "neither",
                "percent_along": 0.0
            },
            {
                "way_id": 962977818,
                "correlated_lat": 59.869659,
                "correlated_lon": 10.66207,
                "side_of_street": "neither",
                "percent_along": 1.0
            }
        ],
        "nodes": [
            {
                "lon": 10.66207,
                "lat": 59.869659
            }
        ]
    },
    {
        "input_lat": 59.9106,
        "input_lon": 10.7276,
        "edges": [
            {
                "way_id": 4266035,
                "correlated_lat": 59.910597,
                "correlated_lon": 10.727593,
                "side_of_street": "neither",
                "percent_along": 0.58193
            },
            {
                "way_id": 4266035,
                "correlated_lat": 59.910597,
                "correlated_lon": 10.727593,
                "side_of_street": "neither",
                "percent_along": 0.41806
            }
        ],
        "nodes": []
    }
]
```


Prøv rute med pedestrian (eller auto/bicycle):

Post
http://localhost:8002/route
```json
{
  "locations":[
    {"lat":59.8709,"lon":10.6637},
    {"lat":59.9106,"lon":10.7276}
  ],
  "costing":"pedestrian",
  "units":"kilometers",
  "directions_options":{"language":"en-US"}
}
```

Result

```json
{
  "trip": {
    "locations": [
      {
        "type": "break",
        "lat": 59.8709,
        "lon": 10.6637,
        "original_index": 0
      },
      {
        "type": "break",
        "lat": 59.9106,
        "lon": 10.7276,
        "original_index": 1
      }
    ],
    "legs": [
      {
        "maneuvers": [
          {
            "type": 1,
            "instruction": "Walk south on Tangelaget.",
            "verbal_succinct_transition_instruction": "Walk south.",
            "verbal_pre_transition_instruction": "Walk south on Tangelaget.",
            "verbal_post_transition_instruction": "Continue for 90 meters.",
            "street_names": [
              "Tangelaget"
            ],
            "bearing_after": 164,
            "time": 64.235,
            "length": 0.091,
            "cost": 64.235,
            "begin_shape_index": 0,
            "end_shape_index": 10,
            "rough": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right onto Lagbukta.",
            "verbal_transition_alert_instruction": "Turn right onto Lagbukta.",
            "verbal_succinct_transition_instruction": "Turn right.",
            "verbal_pre_transition_instruction": "Turn right onto Lagbukta.",
            "verbal_post_transition_instruction": "Continue for 30 meters.",
            "street_names": [
              "Lagbukta"
            ],
            "bearing_before": 121,
            "bearing_after": 214,
            "time": 18.352,
            "length": 0.026,
            "cost": 23.352,
            "begin_shape_index": 10,
            "end_shape_index": 16,
            "rough": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right to stay on Lagbukta.",
            "verbal_transition_alert_instruction": "Turn right to stay on Lagbukta.",
            "verbal_succinct_transition_instruction": "Turn right.",
            "verbal_pre_transition_instruction": "Turn right to stay on Lagbukta.",
            "verbal_post_transition_instruction": "Continue for 200 meters.",
            "street_names": [
              "Lagbukta"
            ],
            "bearing_before": 176,
            "bearing_after": 257,
            "time": 118.588,
            "length": 0.168,
            "cost": 123.588,
            "begin_shape_index": 16,
            "end_shape_index": 37,
            "rough": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right onto Lagveien.",
            "verbal_transition_alert_instruction": "Turn right onto Lagveien.",
            "verbal_succinct_transition_instruction": "Turn right. Then Turn left onto the walkway.",
            "verbal_pre_transition_instruction": "Turn right onto Lagveien. Then Turn left onto the walkway.",
            "verbal_post_transition_instruction": "Continue for 10 meters.",
            "street_names": [
              "Lagveien"
            ],
            "bearing_before": 277,
            "bearing_after": 333,
            "time": 7.764,
            "length": 0.011,
            "cost": 7.764,
            "begin_shape_index": 37,
            "end_shape_index": 40,
            "verbal_multi_cue": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 15,
            "instruction": "Turn left onto the walkway.",
            "verbal_transition_alert_instruction": "Turn left onto the walkway.",
            "verbal_succinct_transition_instruction": "Turn left.",
            "verbal_pre_transition_instruction": "Turn left onto the walkway.",
            "verbal_post_transition_instruction": "Continue for 80 meters.",
            "bearing_before": 333,
            "bearing_after": 284,
            "time": 55.058,
            "length": 0.078,
            "cost": 60.058,
            "begin_shape_index": 40,
            "end_shape_index": 58,
            "rough": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right onto Tangenveien/156.",
            "verbal_transition_alert_instruction": "Turn right onto Tangenveien.",
            "verbal_succinct_transition_instruction": "Turn right.",
            "verbal_pre_transition_instruction": "Turn right onto Tangenveien, 156.",
            "verbal_post_transition_instruction": "Continue for 200 meters.",
            "street_names": [
              "Tangenveien",
              "156"
            ],
            "bearing_before": 230,
            "bearing_after": 307,
            "time": 174.352,
            "length": 0.247,
            "cost": 179.352,
            "begin_shape_index": 58,
            "end_shape_index": 72,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 23,
            "instruction": "Keep right to stay on Tangenveien/156.",
            "verbal_transition_alert_instruction": "Keep right to stay on Tangenveien.",
            "verbal_pre_transition_instruction": "Keep right to stay on Tangenveien, 156.",
            "verbal_post_transition_instruction": "Continue for 30 meters.",
            "street_names": [
              "Tangenveien",
              "156"
            ],
            "bearing_before": 302,
            "bearing_after": 310,
            "time": 21.882,
            "length": 0.031,
            "cost": 21.882,
            "begin_shape_index": 72,
            "end_shape_index": 74,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right.",
            "verbal_transition_alert_instruction": "Turn right.",
            "verbal_succinct_transition_instruction": "Turn right.",
            "verbal_pre_transition_instruction": "Turn right.",
            "verbal_post_transition_instruction": "Continue for 60 meters.",
            "bearing_before": 306,
            "bearing_after": 33,
            "time": 45.176,
            "length": 0.064,
            "cost": 50.176,
            "begin_shape_index": 74,
            "end_shape_index": 79,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 23,
            "instruction": "Keep right at the fork.",
            "verbal_transition_alert_instruction": "Keep right at the fork.",
            "verbal_pre_transition_instruction": "Keep right at the fork.",
            "verbal_post_transition_instruction": "Continue for 80 meters.",
            "bearing_before": 353,
            "bearing_after": 33,
            "time": 57.882,
            "length": 0.082,
            "cost": 57.882,
            "begin_shape_index": 79,
            "end_shape_index": 85,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 15,
            "instruction": "Turn left.",
            "verbal_transition_alert_instruction": "Turn left.",
            "verbal_succinct_transition_instruction": "Turn left. Then Turn right onto Tangenveien.",
            "verbal_pre_transition_instruction": "Turn left. Then Turn right onto Tangenveien.",
            "verbal_post_transition_instruction": "Continue for less than 10 meters.",
            "bearing_before": 359,
            "bearing_after": 303,
            "time": 4.235,
            "length": 0.006,
            "cost": 4.235,
            "begin_shape_index": 85,
            "end_shape_index": 87,
            "verbal_multi_cue": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right onto Tangenveien.",
            "verbal_transition_alert_instruction": "Turn right onto Tangenveien.",
            "verbal_succinct_transition_instruction": "Turn right.",
            "verbal_pre_transition_instruction": "Turn right onto Tangenveien.",
            "verbal_post_transition_instruction": "Continue for 40 meters.",
            "street_names": [
              "Tangenveien"
            ],
            "bearing_before": 303,
            "bearing_after": 78,
            "time": 57.529,
            "length": 0.039,
            "cost": 662.529,
            "begin_shape_index": 87,
            "end_shape_index": 95,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right to stay on Tangenveien.",
            "verbal_transition_alert_instruction": "Turn right to stay on Tangenveien.",
            "verbal_succinct_transition_instruction": "Turn right. Then Keep right at the fork.",
            "verbal_pre_transition_instruction": "Turn right to stay on Tangenveien. Then Keep right at the fork.",
            "verbal_post_transition_instruction": "Continue for 10 meters.",
            "street_names": [
              "Tangenveien"
            ],
            "bearing_before": 304,
            "bearing_after": 33,
            "time": 7.058,
            "length": 0.01,
            "cost": 7.058,
            "begin_shape_index": 95,
            "end_shape_index": 97,
            "rough": true,
            "verbal_multi_cue": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 23,
            "instruction": "Keep right at the fork.",
            "verbal_transition_alert_instruction": "Keep right at the fork.",
            "verbal_pre_transition_instruction": "Keep right at the fork.",
            "verbal_post_transition_instruction": "Continue for 6 kilometers.",
            "bearing_before": 33,
            "bearing_after": 35,
            "time": 1506.72,
            "length": 6.171,
            "cost": 908.359,
            "begin_shape_index": 97,
            "end_shape_index": 110,
            "ferry": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 29,
            "instruction": "Walk north on the walkway.",
            "verbal_succinct_transition_instruction": "Walk north.",
            "verbal_pre_transition_instruction": "Walk north on the walkway.",
            "verbal_post_transition_instruction": "Continue for 30 meters.",
            "bearing_before": 19,
            "bearing_after": 20,
            "time": 21.176,
            "length": 0.03,
            "cost": 21.176,
            "begin_shape_index": 110,
            "end_shape_index": 112,
            "rough": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 15,
            "instruction": "Turn left onto the walkway.",
            "verbal_transition_alert_instruction": "Turn left onto the walkway.",
            "verbal_succinct_transition_instruction": "Turn left.",
            "verbal_pre_transition_instruction": "Turn left onto the walkway.",
            "verbal_post_transition_instruction": "Continue for 20 meters.",
            "bearing_before": 28,
            "bearing_after": 316,
            "time": 17.235,
            "length": 0.023,
            "cost": 17.235,
            "begin_shape_index": 112,
            "end_shape_index": 119,
            "rough": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 15,
            "instruction": "Turn left onto the walkway.",
            "verbal_transition_alert_instruction": "Turn left onto the walkway.",
            "verbal_succinct_transition_instruction": "Turn left. Then Continue on Jenny Hemstads gate.",
            "verbal_pre_transition_instruction": "Turn left onto the walkway. Then Continue on Jenny Hemstads gate.",
            "verbal_post_transition_instruction": "Continue for less than 10 meters.",
            "bearing_before": 344,
            "bearing_after": 270,
            "time": 0.705,
            "length": 0.001,
            "cost": 0.705,
            "begin_shape_index": 119,
            "end_shape_index": 120,
            "verbal_multi_cue": true,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 8,
            "instruction": "Continue on Jenny Hemstads gate.",
            "verbal_transition_alert_instruction": "Continue on Jenny Hemstads gate.",
            "verbal_pre_transition_instruction": "Continue on Jenny Hemstads gate.",
            "verbal_post_transition_instruction": "Continue for 50 meters.",
            "street_names": [
              "Jenny Hemstads gate"
            ],
            "bearing_before": 270,
            "bearing_after": 267,
            "time": 33.176,
            "length": 0.047,
            "cost": 38.176,
            "begin_shape_index": 120,
            "end_shape_index": 123,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 10,
            "instruction": "Turn right onto Grundingen.",
            "verbal_transition_alert_instruction": "Turn right onto Grundingen.",
            "verbal_succinct_transition_instruction": "Turn right.",
            "verbal_pre_transition_instruction": "Turn right onto Grundingen.",
            "verbal_post_transition_instruction": "Continue for 30 meters.",
            "street_names": [
              "Grundingen"
            ],
            "bearing_before": 248,
            "bearing_after": 319,
            "time": 19.476,
            "length": 0.027,
            "cost": 24.476,
            "begin_shape_index": 123,
            "end_shape_index": 124,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          },
          {
            "type": 4,
            "instruction": "You have arrived at your destination.",
            "verbal_transition_alert_instruction": "You will arrive at your destination.",
            "verbal_pre_transition_instruction": "You have arrived at your destination.",
            "bearing_before": 319,
            "time": 0.0,
            "length": 0.0,
            "cost": 0.0,
            "begin_shape_index": 124,
            "end_shape_index": 124,
            "travel_mode": "pedestrian",
            "travel_type": "foot"
          }
        ],
        "summary": {
          "has_time_restrictions": false,
          "has_toll": false,
          "has_highway": false,
          "has_ferry": true,
          "min_lat": 59.867881,
          "min_lon": 10.65636,
          "max_lat": 59.910597,
          "max_lon": 10.728944,
          "time": 2230.608,
          "length": 7.152,
          "cost": 2272.248
        },
        "shape": "u|ceqBkjwiS|Ao@jEuCvDgFbDyAzAkC^eC`DaCrEeIxDuBf@gEz@zB~@Xh@dBxAXtA_AbAXApCj@rCfBhDxAlCxArAtBPlCm@`J}GlDmBxEu@nAIbB^t@dCl@~C~@h]KdC@xBZnN@d@BlF]~Dg@|AgAx@y@P@tBMrAYz@_ArAkBlBQb@SrA@xAJn@Vn@|ApCjDdFh@`Bd@rDx@tNL`ATl@l@hAeDbL_D`MsEtQiL~e@mAnFoDbNgCpI_EpKqChG{L~WmCtHsCvJqCpLeDrOwC`IyDtO}B_DmDaCyFeC_Jh@wEp@aE{F}BgG}BkEiIgMqIwLwAr@IVs@dDGgA{DqFCCo@i@u@|C{AjGi@vBc@hBW]aCeDuTg]kL{HoXsKojA}k@knC{vAwfCsxAyvBwpBc{BitCejSmup@icGcyXyfQabl@kpGqy@cVgO{KiHcAcC_@|@eA~B}ArAyA`D]PONUD?VJ~OtFfY`@fDwJhS"
      }
    ],
    "summary": {
      "has_time_restrictions": false,
      "has_toll": false,
      "has_highway": false,
      "has_ferry": true,
      "min_lat": 59.867881,
      "min_lon": 10.65636,
      "max_lat": 59.910597,
      "max_lon": 10.728944,
      "time": 2230.608,
      "length": 7.152,
      "cost": 2272.248
    },
    "status_message": "Found route between points",
    "status": 0,
    "units": "kilometers",
    "language": "en-US"
  }
}
```


post 

```json
{
  "locations":[
    {"lat":59.8709,"lon":10.6637,"name":"Øvre Movei 23, 1450 Nesoddtangen"},
    {"lat":59.9106,"lon":10.7276,"name":"Aker Brygge, Oslo"}
  ],
  "costing":"multimodal",
  "costing_options":{"transit":{"use_bus":1.0,"use_transfers":0.5,"use_rail":0.0,"use_ferry":1.0}},
  "date_time":{"type":1,"value":"2025-10-05T08:00"},
  "units":"kilometers",
  "directions_options":{"narrative":true}
}
```

Result
Feiler:

Vil du bruke multimodal, må du bygge transit-tiles med GTFS (valhalla_build_transit + oppdatert config) og inkludere dem i tile-extract. Ellers hold deg til ikke-transit costings.
Yes, the problem is costing:"multimodal". Multimodal requires transit tiles built from GTFS. You currently have onl


```log
400 “Locations are in unconnected regions” betyr at punktene ikke er koblet via nettverket i dine tiles.
Sjekk/rett:
- Bruk “pedestrian” eller “auto” først for å verifisere grunnruting:
    - Sett costing: "pedestrian" og prøv samme punkter.

- Multimodal krever transit-data (GTFS) bygget inn. Du har kun OSM-tiles, ingen GTFS, derfor feiler ofte multimodal. Bruk "pedestrian"/"auto"/"bicycle", eller bygg egne transit-tiles med GTFS før multimodal.
- Verifiser at begge punkter snapper til veinettet i dine tiles:
    - Kall /locate for hvert punkt:
        - POST [http://localhost:8002/locate](http://localhost:8002/locate) with { "locations":[{"lat":59.8709,"lon":10.6637},{"lat":59.9106,"lon":10.7276}] }

    - Hvis ett punkt ikke får et “edge”/“node”, flytt koordinaten litt eller oppdater OSM-tiles.

- Bruk samme region som PBF-en dekker. Du har Norway PBF; punktene er i Oslo, så det skal være OK. Om feilen vedvarer:
    - Slett tiles og bygg på nytt (kan ha blitt avbrutt): rm -rf valhalla/valhalla_tiles*; docker compose up -d; følg logger til “Tile extract … loaded”.

- Test en kortere rute i nærheten (f.eks. to punkter 100–500 m fra hverandre) for å utelukke lokale databrudd.
```

```text
* Routing tiles” are small binary files Valhalla builds from data:
- OSM tiles: built from OpenStreetMap (roads/paths).
- Transit tiles: built from GTFS (public transit schedules, stops, routes).

* Multimodal routing = walk/bike + public transit. It requires both:
- OSM tiles (you already built from norway-latest.osm.pbf)
- Transit tiles (you must build from GTFS zips)

*To enable multimodal:
- Obtain GTFS zip files for your area (from transit agencies or aggregators).
- Put them in your volume, e.g. host: valhalla/gtfs → container: /data/gtfs
- Build tiles in this order:
-- valhalla_build_config … > /data/valhalla.json
-- valhalla_build_tiles -c /data/valhalla.json /data/osm/norway-latest.osm.pbf
-- valhalla_build_transit -c /data/valhalla.json /data/gtfs/*.zip
-- tar -C /data -cf /data/valhalla_tiles.tar valhalla_tiles

* Start valhalla_service and call /route with costing: "multimodal".

If you don’t have GTFS, use costing: "pedestrian"/"auto"/"bicycle" instead.

```