# doc

## Geocoding with Nominatim

Short comparison for Norway geocoding:
- LocationIQ
    - Free: yes (small free tier with API key)
    - Data: OSM-based, good Norway coverage via OSM
    - Pros: Simple, affordable paid tiers, permissive usage
    - Cons: Lower free quota than big vendors

- Mapbox
    - Free: yes (monthly free tier with API key)
    - Data: Mapbox data (includes OSM), strong quality in Norway’s urban areas
    - Pros: Good SDKs, stable, decent quotas
    - Cons: Terms on caching, attribution; may differ slightly from raw OSM

- Google Maps Geocoding
    - Free: $200 monthly credit (effectively free for low usage), requires billing
    - Data: Very strong global coverage and quality
    - Pros: High quality, robust; reverse geocoding is excellent
    - Cons: Requires billing; terms on data usage/caching

Recommendations:
- Low/medium volume, OSM-aligned: LocationIQ or Mapbox.
- Max quality and reliability: Google.
- Heavy/batch use: self-host Nominatim or pay for higher-tier API.

Tip: Whichever you choose, cache results to minimize calls and cost.


## Nominatim example

```terminal
https://nominatim.openstreetmap.org/search?q= Øvre Movei 23, 1450 Nesoddtangen&format=jsonv2&addressdetails=1&limit=1
```

Create a new request
Method: GET
URL: https://nominatim.openstreetmap.org/search
Params tab (add rows)
q = Øvre Movei 23, 1450 Nesoddtangen
format = jsonv2
addressdetails = 1
limit = 1
Headers tab
User-Agent: optimize-demo/1.0 (you@example.com)
Accept: application/json

result: 
```json
[
    {
        "place_id": 149861440,
        "licence": "Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
        "osm_type": "node",
        "osm_id": 3122916070,
        "lat": "59.8607627",
        "lon": "10.6694777",
        "category": "place",
        "type": "house",
        "place_rank": 30,
        "importance": 5.4744029907637405e-05,
        "addresstype": "place",
        "name": "",
        "display_name": "23, Øvre Movei, Oksval, Helgolf, Nesoddtangen, Nesodden, Akershus, 1450, Norge",
        "address": {
            "house_number": "23",
            "road": "Øvre Movei",
            "quarter": "Oksval",
            "isolated_dwelling": "Helgolf",
            "town": "Nesoddtangen",
            "municipality": "Nesodden",
            "county": "Akershus",
            "ISO3166-2-lvl4": "NO-32",
            "postcode": "1450",
            "country": "Norge",
            "country_code": "no"
        },
        "boundingbox": [
            "59.8607127",
            "59.8608127",
            "10.6694277",
            "10.6695277"
        ]
    }
]


```