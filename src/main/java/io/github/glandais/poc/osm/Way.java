package io.github.glandais.poc.osm;

import java.util.List;
import java.util.Map;

public record Way(long id, Map<String, String> tags, List<SubWay> subWays) {
}
