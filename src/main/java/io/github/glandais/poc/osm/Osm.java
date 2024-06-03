package io.github.glandais.poc.osm;

import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;
import io.github.glandais.poc.Point;
import io.github.glandais.poc.lane.Lane;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;

public class Osm {

    private static final double R = 6_371_000;

    private static final Random RANDOM = new SecureRandom();

//    private static Set<String> validHighways = new HashSet<>(
//            Arrays.asList("primary", "secondary", "tertiary",
//                    "residential", "living_street", "unclassified", "trunk", "trunk_link"));
    private static Set<String> validHighways = new HashSet<>(
            Arrays.asList(
                    "primary", "secondary", "tertiary",
                    "residential", "living_street", "unclassified",
                    "trunk", "trunk_link"
            ));
    private static Set<String> oneWays = new HashSet<>(
            Arrays.asList("motorway", "trunk", "motorway_link", "trunk_link", "primary_link", "secondary_link"));

    @SneakyThrows
    public static List<Lane> loadLanes(String s) {
        long subWayId = 0;
        try (InputStream input = new FileInputStream(s)) {
            // Create a reader and read all data into a data set
            OsmReader reader = new OsmXmlReader(input, false);
            InMemoryMapDataSet data = MapDataSetLoader.read(reader, true, true,
                    true);

            // First pass
            Map<Long, Way> firstPassWays = new HashMap<>();
            Map<Long, Node> firstPassnodes = new HashMap<>();
            for (OsmWay osmWay : data.getWays().valueCollection()) {
                Map<String, String> tags = new HashMap<>(OsmModelUtil.getTagsAsMap(osmWay));

                String highway = tags.get("highway");
                if (highway == null) {
                    continue;
                }

                if (!validHighways.contains(highway)) {
                    continue;
                }
                if (oneWays.contains(highway)) {
                    tags.put("oneway", "true");
                }
                if ("roundabout".equals(tags.get("junction"))) {
                    tags.put("oneway", "true");
                }

                long wayId = osmWay.getId();
                Way way = new Way(wayId, tags, new ArrayList<>());
                SubWay subWay = new SubWay(subWayId++, way, new ArrayList<>());
                for (int i = 0; i < osmWay.getNumberOfNodes(); i++) {
                    try {
                        long nodeId = osmWay.getNodeId(i);
                        OsmNode osmNode = data.getNode(nodeId);
                        Node node = firstPassnodes.computeIfAbsent(nodeId, l -> new Node(osmNode, new HashSet<>(), new ArrayList<>()));
                        node.wayIds().add(wayId);
                        node.subWays().add(subWay);
                        subWay.nodes().add(node);
                    } catch (EntityNotFoundException e) {
                        // missing node, reset way
                        way.subWays().add(subWay);
                        subWay = new SubWay(subWayId++, way, new ArrayList<>());
                    }
                }
                way.subWays().add(subWay);
                way.subWays().removeIf(t -> t.nodes().size() < 2);

                if (!way.subWays().isEmpty()) {
                    firstPassWays.put(way.id(), way);
                }
            }

            // Second pass
            Map<Long, Way> ways = new HashMap<>();
            Map<Long, Node> nodes = new HashMap<>();
            for (Way firstPassWay : firstPassWays.values()) {
                Way way = new Way(firstPassWay.id(), firstPassWay.tags(), new ArrayList<>());
                SubWay subWay = new SubWay(subWayId++, way, new ArrayList<>());
                for (SubWay firstPassSubWay : firstPassWay.subWays()) {
                    for (Node firstPassNode : firstPassSubWay.nodes()) {
                        Node node = nodes.computeIfAbsent(firstPassNode.osmNode().getId(), l -> new Node(firstPassNode.osmNode(), new HashSet<>(), new ArrayList<>()));
                        node.wayIds().add(firstPassWay.id());
                        node.subWays().add(subWay);
                        subWay.nodes().add(node);
                        if (firstPassNode.wayIds().size() > 1) {
                            way.subWays().add(subWay);
                            subWay = new SubWay(subWayId++, way, new ArrayList<>());
                            node.subWays().add(subWay);
                            subWay.nodes().add(node);
                        }
                    }
                }
                way.subWays().add(subWay);
                way.subWays().removeIf(t -> t.nodes().size() < 2);
                if (!way.subWays().isEmpty()) {
                    ways.put(way.id(), way);
                }
            }

            Double minLat = Double.MAX_VALUE;
            Double maxLat = -Double.MAX_VALUE;
            Double minLon = Double.MAX_VALUE;
            Double maxLon = -Double.MAX_VALUE;
            for (Node node : firstPassnodes.values()) {
                double latitude = node.osmNode().getLatitude();
                double longitude = node.osmNode().getLongitude();
                minLat = Math.min(minLat, latitude);
                maxLat = Math.max(maxLat, latitude);
                minLon = Math.min(minLon, longitude);
                maxLon = Math.max(maxLon, longitude);
            }

            double rx = R * Math.cos(Math.toRadians((minLat + maxLat) / 2));
            double x0 = rx * Math.toRadians(minLon);
            double y0 = R * Math.toRadians(minLat);

            List<OsmLane> lanes = new ArrayList<>();
            Map<Long, List<OsmLane>> ends = new HashMap<>();
            Map<Long, List<OsmLane>> starts = new HashMap<>();
            for (Way way : ways.values()) {
                for (SubWay subWay : way.subWays()) {
                    OsmLane lane = new OsmLane(subWay, false, osmNode -> project(rx, x0, y0, osmNode.getLongitude(), osmNode.getLatitude()));
                    lanes.add(lane);
                    if (!"true".equals(way.tags().get("oneway")) && !"yes".equals(way.tags().get("oneway"))) {
                        OsmLane reversedLane = new OsmLane(subWay, true, osmNode -> project(rx, x0, y0, osmNode.getLongitude(), osmNode.getLatitude()));
                        lanes.add(reversedLane);
                    }
                }
            }

            for (OsmLane lane : lanes) {
                long firstId = lane.getFirst().osmNode().getId();
                long lastId = lane.getLast().osmNode().getId();
                starts.computeIfAbsent(firstId, l -> new ArrayList<>()).add(lane);
                ends.computeIfAbsent(lastId, l -> new ArrayList<>()).add(lane);
            }

            List<OsmLane> noPrevious = new ArrayList<>();
            for (Map.Entry<Long, List<OsmLane>> entry : starts.entrySet()) {
                List<OsmLane> previous = ends.get(entry.getKey());
                List<OsmLane> next = entry.getValue();
                if (previous == null || previous.isEmpty() ||
                        (
                                previous.size() == 1 && next.size() == 1 &&
                                        previous.getFirst().getSubWay().id() == next.getFirst().getSubWay().id()
                        )
                ) {
                    noPrevious.addAll(next);
                }
            }

            for (Map.Entry<Long, List<OsmLane>> entry : ends.entrySet()) {
                List<OsmLane> previous = entry.getValue();
                List<OsmLane> next = starts.get(entry.getKey());
                if (next != null) {
                    for (OsmLane osmLane : previous) {
                        for (OsmLane lane : next) {
                            if (lane.getSubWay().id() != osmLane.getSubWay().id()) {
                                osmLane.getNextLanes().add(lane);
                            }
                        }
                    }
                }
            }

            for (Lane lane : lanes) {
                if (lane.getNextLanes().isEmpty()) {
                    lane.getNextLanes().add(noPrevious.get(RANDOM.nextInt(noPrevious.size())));
                }
            }


            /*
            List<OsmLane> noPrevious = new ArrayList<>();
            for (Map.Entry<Long, List<OsmLane>> entry : starts.entrySet()) {
                List<OsmLane> previous = ends.get(entry.getKey());
                if (previous == null || previous.isEmpty()) {
                    noPrevious.addAll(entry.getValue());
                } else if (
                        previous.size() == 1 &&
                                entry.getValue().size() == 1 &&
                                previous.getFirst().getSubWay().id() == entry.getValue().getFirst().getSubWay().id()) {
                    noPrevious.addAll(entry.getValue());
                }
            }

            for (Map.Entry<Long, List<OsmLane>> entry : ends.entrySet()) {
                List<OsmLane> next = starts.get(entry.getKey());
                if (next == null || next.isEmpty() ||
                        (
                                next.size() == 1 &&
                                        entry.getValue().size() == 1 &&
                                        next.getFirst().getSubWay().id() == entry.getValue().getFirst().getSubWay().id()
                        )) {
                    next = noPrevious;
                }
                for (OsmLane osmLane : entry.getValue()) {
//                    osmLane.getNextLanes().addAll(next);
                }
            }
            */

            /*
            List<OsmLane> noPrevious = starts.values()
                    .stream()
                    .filter(l -> ends.get())
                    .map(List::getFirst)
                    .toList();
            for (Map.Entry<Long, List<OsmLane>> entry : ends.entrySet()) {
                List<OsmLane> next = starts.get(entry.getKey());
                if (next == null || next.isEmpty()) {
                    next = noPrevious;
                }
                for (OsmLane osmLane : entry.getValue()) {
                    osmLane.getNextLanes().addAll(next);
                }
            }
             */
            return (List) lanes;
        }
//        return List.of();
    }

    private static Point project(double rx, double x0, double y0, double lon, double lat) {
        double x = rx * Math.toRadians(lon);
        double y = R * Math.toRadians(lat);
        if (x < x0 || y < y0) {
            System.err.println("!!!");
        }
        return new Point(
                x - x0,
                y - y0
        );
    }

}
