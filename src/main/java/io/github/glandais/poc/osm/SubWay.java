package io.github.glandais.poc.osm;

import java.util.List;

public record SubWay(long id, Way parent, List<Node> nodes) {
}
