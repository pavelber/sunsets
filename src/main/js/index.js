import "ol/ol.css";
import Map from "ol/Map";
import View from "ol/View";
import GeoJSON from "ol/format/GeoJSON";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import {Fill, Stroke, Style} from "ol/style";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";

let dayGlobal = 0;
let mapTypeGlobal = 'low';
let geoJSON = new GeoJSON();

var vectorLoader = function (extent, resolution, projection) {
    var url = '/sunset/source?day=' + dayGlobal + '&map=' + mapTypeGlobal;
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.onreadystatechange = function() {
//        console.log(xmlhttp.readyState);
        if (xmlhttp.readyState === XMLHttpRequest.DONE) {   // XMLHttpRequest.DONE == 4
  //          console.log(xmlhttp.status);
            if (xmlhttp.status === 200) {
                let features = geoJSON.readFeatures( xmlhttp.responseText, {featureProjection: projection});
    //            console.log(features);
                source.addFeatures(features);
            }
        }
    };

    xmlhttp.open("GET", url, true);
    xmlhttp.send();
};

var source = new VectorSource({
    //url: 'http://localhost:8080/source?day='+day+'&map='+mapTypeGlobal,
    loader: vectorLoader,
    format: geoJSON
});

var styleFunction = function (feature, resolution) {
    //console.log(feature);
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
        zoom: 7
    })
});

module.exports.changeMap = function changeMap(mapType) {
    mapTypeGlobal = mapType;
    document.getElementById('mapbutton').textContent = 'Map '+mapType;
    layer.getSource().clear();
    layer.getSource().refresh();
};

module.exports.changeDay = function changeDay(day) {
    dayGlobal = day;
    document.getElementById('daybutton').textContent = 'Day '+day;
    layer.getSource().clear();
    layer.getSource().refresh();
};

