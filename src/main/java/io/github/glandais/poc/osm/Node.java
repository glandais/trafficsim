package io.github.glandais.poc.osm;

import de.topobyte.osm4j.core.model.iface.OsmNode;

import java.util.List;
import java.util.Set;

public record Node(OsmNode osmNode, Set<Long> wayIds, List<SubWay> subWays) {
}
