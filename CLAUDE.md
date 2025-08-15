# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Traffic simulation application using Java Swing for visualization. Simulates vehicle movement on road networks loaded from OpenStreetMap (OSM) data with realistic physics including acceleration, braking, and collision avoidance.

## Architecture

### Core Components

- **Lane System**: Abstract `Lane` class with implementations (`StraigthLane`, `CircleLane`, `BezierLane`, `OsmLane`) representing road segments. Lanes maintain connections to next lanes for routing.

- **Car Physics**: `Car` class handles vehicle movement with speed control, acceleration/deceleration, and collision detection. Cars follow lanes and make routing decisions at intersections.

- **OSM Integration**: `Osm` class loads and converts OpenStreetMap data into lane network. Handles way splitting at intersections, bidirectional roads, and coordinate projection.

- **Simulation Loop**: `Sim` class manages the simulation state, updates all cars, and provides car positions. Uses parallel processing for performance.

- **Visualization**: `DrawingCanvas` renders the simulation using Java Swing with a 60 FPS target.

### Key Design Patterns

- Lanes store references to cars currently on them for efficient collision detection
- Cars transition between lanes automatically when reaching lane endpoints
- OSM ways are split into SubWays at intersection nodes
- Coordinate system uses Mercator projection for OSM data

## Development Commands

### Build and Run
```bash
# Compile the project
mvn compile

# Run the application
mvn exec:java -Dexec.mainClass="io.github.glandais.poc.Main"

# Package as JAR
mvn package

# Clean build artifacts
mvn clean
```

### Testing
```bash
# Run all tests (if tests exist)
mvn test

# Run with specific OSM file (modify map file in Sim.java constructor)
# Current file: map5.osm
```

## Key Files and Entry Points

- `Main.java`: Application entry point, creates Swing UI
- `Sim.java:29`: OSM file loading configuration
- `Sim.java:38`: Number of cars in simulation
- `Osm.java:29-34`: Highway types included in simulation
- `DrawingCanvas.java`: Rendering and frame rate control

## Common Modifications

- **Change map**: Edit `Sim.java` line 29 to load different OSM file
- **Adjust car count**: Modify loop at `Sim.java:38`
- **Change road types**: Update `validHighways` set in `Osm.java:29-34`
- **Modify physics**: Adjust acceleration/deceleration values in `Car.java:52-61`