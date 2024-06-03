package io.github.glandais.poc.osm;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Convert {

    @SneakyThrows
    public static void main(String[] args) {
        try (InputStream input = new FileInputStream("map2.osm");
             OutputStream output = new FileOutputStream("osm2.pbf")) {
            // Create a reader for XML data
            OsmIterator iterator = new OsmXmlIterator(input, true);

            // Create an output stream
            OsmOutputStream osmOutput = new PbfWriter(output, true);

            // Iterate objects and copy them to the output
            for (EntityContainer container : iterator) {
                switch (container.getType()) {
                    default:
                    case Node:
                        osmOutput.write((OsmNode) container.getEntity());
                        break;
                    case Way:
                        osmOutput.write((OsmWay) container.getEntity());
                        break;
                    case Relation:
                        osmOutput.write((OsmRelation) container.getEntity());
                        break;
                }
            }

            // Close output
            osmOutput.complete();
        }
    }

}
