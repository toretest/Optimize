# Route Optimization Options: OSRM, Valhalla, and More

This document outlines three common open-source routing engines you can use for route optimization on OpenStreetMap (OSM) data, compares their differences, and suggests how to move towards a Valhalla-based demo.

- OSRM (Open Source Routing Machine)
- Valhalla
- GraphHopper (as an additional alternative)

## Matrix support — what is it?
“Matrix support” (also called distance or time matrix) is the ability to compute pairwise travel times/distances between multiple origins and destinations in one request. It returns an N×M matrix where cell (i, j) is the travel cost from origin i to destination j. This is essential for:
- Vehicle Routing Problems (VRP) and route optimization.
- Clustering and nearest-depot selection.
- Batch ETA calculations.

You typically compute the matrix first, then feed it into an optimizer/solver (e.g., for TSP/VRP). All three engines offer a matrix API; solvers are external.

## Feature comparison (table)
| Aspect | OSRM | Valhalla | GraphHopper |
|---|---|---|---|
| Primary focus | Ultra-fast routing with CH | Flexible multi-modal navigation | Fast routing with flexible profiles |
| Data source | OSM | OSM (+ optional elevation/admin/timezones) | OSM |
| Modes | Car/Bike/Foot | Car/Bike/Foot/Transit (multi-modal) | Car/Bike/Foot |
| Turn-by-turn instructions | Basic | Rich, with maneuvers | Good |
| Customization model | Lua profiles; re-contract to change | Costing models; dynamic-friendly | Custom profiles; CH/LM trade-offs |
| Preprocessing | Heavy (extract/partition/customize/contract) | Tile build; no CH by default | Graph import; CH and/or LM optional |
| Speed (single route) | Fastest for static profiles | Competitive; favors flexibility | Very fast with CH/LM |
| Matrix API | Table API (fast) | Matrix API (time/distance) | Matrix API (fast; scalable) |
| Isochrones | Not native | Yes | Via addons/tools |
| Multi-modal | Limited | Strong | Limited in OSS |
| Traffic/time-dependent | Limited community solutions | Supported approaches | Possible with extensions |
| Licensing | BSD-2-Clause | MIT | Apache 2.0 (commercial add-ons exist) |
| Typical use | High-QPS routing, stable profiles | Navigation, isochrones, dynamic costs | Fast + flexible, strong matrix workloads |

## 1) OSRM (Open Source Routing Machine)
- Focus: Very fast shortest-path routing on preprocessed OSM graphs.
- Profiles: Car, bike, foot via Lua profiles; customizations through profile tuning and preprocessing.
- Strengths:
  - Extremely fast query times thanks to Contraction Hierarchies (CH).
  - Mature project, widely adopted, good for high-QPS routing.
- Limitations:
  - Requires heavy preprocessing (osrm-extract, osrm-contract).
  - Profile changes often require re-preprocessing.
  - Less flexible for multi-modal or turn-by-turn customization compared to Valhalla.

Typical use cases:
- Service that needs ultra-low latency single-route queries at scale.
- Static-ish road rules where profile doesn’t change frequently.

## 2) Valhalla
- Focus: Flexible, multi-modal, turn-by-turn navigation with rich features.
- Modes: Car, bicycle, pedestrian, transit; supports time-dependent routing and costing models.
- Strengths:
  - Flexible costing models (penalties, priorities, time-of-day).
  - Turn-by-turn instructions, elevation, and isochrones.
  - Edge-based routing without CH by default, enabling dynamic adjustments.
- Limitations:
  - Generally slower than highly contracted OSRM for single static routes (though still performant).
  - More moving parts to configure (tiles build, costing, admins, elevation, etc.).

Typical use cases:
- Navigation apps with instructions, multi-modal transport, isochrones, and custom constraints.
- Scenarios needing dynamic costs (traffic, time windows) or richer outputs.

## 3) GraphHopper
- Focus: Fast routing with flexible profiles; supports CH and LM (Landmarks) for speed/coverage trade-offs.
- Modes: Car, bike, foot; commercial add-ons exist.
- Strengths:
  - Good balance between speed and customization (CH + LM, custom profiles).
  - Matrix routing, VRP (via separate tools) integration paths.
- Limitations:
  - Advanced features may require commercial extensions.
  - Multi-modal and instruction detail not as deep as Valhalla in open edition.

Typical use cases:
- When you need both fast routing and custom profiles with fewer rebuilds than OSRM.
- Matrix APIs at scale and good developer ergonomics.

## Feature Comparison (High-Level)
- Data source: All primarily use OSM.
- Profiles/customization:
  - OSRM: Strong but rebuild-heavy; Lua profiles.
  - Valhalla: Highly flexible costing, dynamic-friendly.
  - GraphHopper: Custom profiles with CH/LM trade-offs.
- Speed:
  - OSRM: Fastest for static profiles (CH).
  - GraphHopper: Very fast with CH/LM; flexible.
  - Valhalla: Competitive, favors flexibility over CH speed.
- Multi-modal and instructions:
  - Valhalla: Best-in-class for open-source.
  - GraphHopper: Good; improving.
  - OSRM: Basic instructions; car/bike/foot focus.
- Matrix/Optimization:
  - OSRM: Has Table API; external VRP solvers needed.
  - GraphHopper: Matrix API; VRP extensions available.
  - Valhalla: Has matrix/isochrones; VRP via external solver.

## Choosing between them
- Need ultra-low latency and stable profiles: OSRM.
- Need multi-modal, rich turn-by-turn, time-dependent costs, isochrones: Valhalla.
- Need fast routing with flexible profiles and strong matrix support: GraphHopper.

## Next steps — towards a Valhalla demo
Goal: Show end-to-end routing with OSM data using Valhalla.

Plan:
1. Data preparation: obtain a regional OSM extract (.pbf).
2. Tile build: build Valhalla tiles (admin/timezone/elevation optional).
3. Run Valhalla service: start the API with tiles and desired costing.
4. Test queries: call route, matrix, and isochrone endpoints for car/bike/foot.
5. Demo script: prepare routes showing instructions, alternatives, and isochrones.

Deliverables:
- Running Valhalla instance on a sample region.
- Example API calls and results (route, matrix, isochrone).
- Brief notes on costing settings for the demo.

## SPIDER (SINTEF) vs. Valhalla — what SPIDER 2 adds
- Purpose focus:
  - Valhalla: General-purpose, open-source routing (point-to-point, matrix, isochrones) for navigation and analytics on OSM.
  - SINTEF SPIDER (SPIDER/Spider 2): Research/industrial tooling for transportation analytics and optimization workflows, centered on OD-demand modeling, accessibility studies, scenario analysis, and planning support.

- What SPIDER typically adds beyond Valhalla:
  - Demand- and population-weighted accessibility metrics (cumulative opportunities, gravity measures).
  - Batch, large-scale OD computations with temporal scenarios (peak/off-peak, schedules).
  - KPI dashboards and reproducible pipelines for planning (multi-scenario comparisons, equity analysis, coverage).
  - Integration with statistical workflows (R/Python) for calibration, uncertainty, and sensitivity analysis.
  - Custom network pre/post-processing beyond default OSM (e.g., cycling infra classification, impedance tuning, zoning).
  - Experiment design for before/after infrastructure changes and policy what-if analyses at city/region scale.

- What Valhalla already does well:
  - Turn-by-turn navigation and multi-modal routing APIs.
  - On-demand isochrones, matrices, and routes for apps/services.
  - Costing models and rich instructions for end-user navigation.

- When to use which:
  - Need navigation, APIs, or app integration: Valhalla.
  - Need planning analytics at scale (accessibility KPIs, scenario comparisons, policy evaluation): SPIDER workflows, possibly using Valhalla (or another engine) as a routing backend inside the pipeline.





