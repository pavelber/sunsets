import "ol/ol.css";
import Map from "ol/Map";
import View from "ol/View";
import GeoJSON from "ol/format/GeoJSON";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import {Fill, Stroke, Style} from "ol/style";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import * as olProj from 'ol/proj';
import Overlay from 'ol/Overlay';
import $ from "jquery";

let dayGlobal = 0;
let mapTypeGlobal = 'low';
let geoJSON = new GeoJSON();

var vectorLoader = function (extent, resolution, projection) {
    var url = '/sunset/source?day=' + dayGlobal + '&map=' + mapTypeGlobal;
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState === XMLHttpRequest.DONE) {   // XMLHttpRequest.DONE == 4
            if (xmlhttp.status === 200) {
                let features = geoJSON.readFeatures(xmlhttp.responseText, {featureProjection: projection});
                source.addFeatures(features);
            }
        }
    };

    xmlhttp.open("GET", url, true);
    xmlhttp.send();
};

var source = new VectorSource({
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


var container = document.getElementById('popup');
var content = document.getElementById('popup-content');
var closer = document.getElementById('popup-closer');


/**
 * Create an overlay to anchor the popup to the map.
 */
var overlay = new Overlay({
    element: container,
    autoPan: true,
    autoPanAnimation: {
        duration: 250
    }
});


/**
 * Add a click handler to hide the popup.
 * @return {boolean} Don't follow the href.
 */
closer.onclick = function () {
    overlay.setPosition(undefined);
    closer.blur();
    return false;
};

var map = new Map({
    layers: [
        new TileLayer({
            source: new OSM()
        }),
        layer
    ],
    target: "map",
    overlays: [overlay],
    view: new View({
        center: [4000000, 3800000],
        zoom: 8
    })
});

module.exports.changeMap = function changeMap(mapType) {
    mapTypeGlobal = mapType;
    document.getElementById('mapbutton').textContent = 'Map ' + mapType;
    layer.getSource().clear();
    layer.getSource().refresh();
};

module.exports.changeDay = function changeDay(day) {
    dayGlobal = day;
    document.getElementById('daybutton').textContent = 'Day ' + day;
    layer.getSource().clear();
    layer.getSource().refresh();
};
map.on('click', function (evt) {
    var coordinates = evt.coordinate;
    var longLat = olProj.toLonLat(evt.coordinate)
    var feature = map.forEachFeatureAtPixel(evt.pixel,
        function (feature, layer) {
            return feature;
        });
    if (typeof feature !== 'undefined' && mapTypeGlobal === "sunset") {
        $.get("/sunset/sunset?lat=" + longLat[1] + "&long=" + longLat[0], function (data) {
            var daydata = data[dayGlobal];
            content.innerHTML = "<B>" + daydata["mark"] + ":" + daydata["maxMark"] + "</B><P>" + daydata["description"] + "</P>";

            overlay.setPosition(coordinates);
        });
    }
});
