import "ol/ol.css";
import Map from "ol/Map";
import View from "ol/View";
import GeoJSON from "ol/format/GeoJSON";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import { Fill, Stroke, Style } from "ol/style";
import TileLayer from "ol/layer/Tile";
import { OSM, TileDebug } from "ol/source";



var source = new VectorSource({
    url: 'http://localhost:8080/source',
    format: new GeoJSON()
});

var styleFunction = function(feature, resolution) {
  return new Style({
    stroke: new Stroke({
      color: "blue",
      width: 1
    }),
    fill: new Fill({
      color: feature.getProperties()["color"]
    })
  });
};

var layer = new VectorLayer({
  source: source,
  style: styleFunction
});

var map = new Map({
  layers: [
    new TileLayer({
      source: new OSM()
    }),
    layer
  ],
  target: "map",
  view: new View({
    center: [4000000, 3800000],
        zoom: 6
  })
});
