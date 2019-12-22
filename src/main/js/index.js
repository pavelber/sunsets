import "ol/ol.css";
import Map from "ol/Map";
import View from "ol/View";
import GeoJSON from "ol/format/GeoJSON";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import { Fill, Stroke, Style } from "ol/style";
import TileLayer from "ol/layer/Tile";
import { OSM, TileDebug } from "ol/source";

var geojsonObject = {
  type: "FeatureCollection",
  crs: {
    type: "name",
    properties: {
      name: "EPSG:3857"
    }
  },
  features: [
    {
      type: "Feature",
      properties: { color: "rgba(0, 0, 255,0.4)" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [[-5e6, 6e6], [-5e6, 8e6], [-3e6, 8e6], [-3e6, 6e6], [-5e6, 6e6]]
        ]
      }
    },
    {
      type: "Feature",
      properties: { color: "rgba(0, 0, 255,0.6)" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [[-2e6, 6e6], [-2e6, 8e6], [0, 8e6], [0, 6e6], [-2e6, 6e6]]
        ]
      }
    },
    {
      type: "Feature",
      geometry: {
        type: "Polygon",
        coordinates: [
          [[1e6, 6e6], [1e6, 8e6], [3e6, 8e6], [3e6, 6e6], [1e6, 6e6]]
        ]
      },
      style: {
        //all SVG styles allowed
        fill: "red",
        "stroke-width": "3",
        "fill-opacity": 0.6
      }
    },
    {
      type: "Feature",
      geometry: {
        type: "Polygon",
        coordinates: [[[-2e6, -1e6], [-1e6, 1e6], [0, -1e6], [-2e6, -1e6]]]
      },
      style: {
        //all SVG styles allowed
        fill: "red",
        "stroke-width": "3",
        "fill-opacity": 0.6
      },
      className: {
        baseVal: "highway_primary"
      }
    }
  ]
};

var source = new VectorSource({
  features: new GeoJSON().readFeatures(geojsonObject)
});

var styleFunction = function(feature, resolution) {
  //console.log(feature.getType());
  return new Style({
    stroke: new Stroke({
      color: "blue",
      width: 3
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
    center: [0, 3000000],
    zoom: 2
  })
});
