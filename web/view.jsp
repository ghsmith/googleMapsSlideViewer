<!doctype HTML>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
  <title>Slide Viewer</title>
  <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css"/>
  <script src="https://code.jquery.com/jquery-3.3.1.min.js" integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=" crossorigin="anonymous"></script>
  <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js" integrity="sha256-VazP97ZCwtekAsvgPBSUwPFKdrwD3unUfSGVYrahUqU=" crossorigin="anonymous"></script>
  <script type="text/javascript" src="http://maps.google.com/maps/api/js?key=AIzaSyC1NDnSq8uOABRmuNz7tK9dwh4e6-MSmoo&v=3"></script>
  <script type="text/javascript">

    var url = window.location.href;
    var baseUrl = url.substring(0, url.lastIndexOf("/"));

    var slideMap;
    var thumbnailMap;
    var xhair;

    var slideMapMoving = false;
    var thumbnailMapMoving = false;

    function move() {
      thumbnailMapMoving = true;
      if(slideMapMoving == false) {
        xhair.setPosition(slideMap.getCenter());
      }
      thumbnailMapMoving = false;
      var p = slideMap.getProjection().fromLatLngToPoint(slideMap.getCenter());
      $("#x").text(Math.round(p.x));
      $("#y").text(Math.round(p.y));
      $("#z").text(slideMap.getZoom());
    }

    function thumbnailMapClick(latLng) {
      slideMapMoving = true;
      if(thumbnailMapMoving == false) {
        if(latLng) {
            slideMap.setCenter(latLng);
            xhair.setPosition(latLng);
        }
      }
      slideMapMoving = false;
    }

    function xhairDrag(latLng) {
      slideMapMoving = true;
      if(thumbnailMapMoving == false) {
        slideMap.setCenter(latLng);
      }
      slideMapMoving = false;
    }

    function initialize() {

      function SlideProjection(map) {
        this.map = map;
      }
      SlideProjection.prototype.fromPointToLatLng = function(point) {
        var max = 256;
        var x = point.x;
        var y = point.y;
        var lng = (x / max) * 100 - 50;
        var lat = -(y / max) * 100 + 50;
        return new google.maps.LatLng(lat, lng);
      }
      SlideProjection.prototype.fromLatLngToPoint = function(latLng) {
        var max = 256;
        var x = max * ((latLng.lng() + 50) / 100);
        var y = -max * ((latLng.lat() - 50) / 100);
        return new google.maps.Point(x, y);
      }

      var slideMapType = new google.maps.ImageMapType( {
        getTileUrl: function(coord, zoom) {
          return "<%= request.getContextPath() %>/GetTile;JSESSIONID=<%= request.getSession().getId() %>?fileName=${requestScope.ndpiFileName}&x=" + coord.x + "&y=" + coord.y + "&z=" + zoom;
        },
        tileSize: new google.maps.Size(256, 256),
        isPng: false,
        maxZoom: ${maxZoom},
        minZoom: 0
      } );

      var thumbnailMapType = new google.maps.ImageMapType( {
        getTileUrl: function(coord, zoom) {
          return "<%= request.getContextPath() %>/GetTile;JSESSIONID=<%= request.getSession().getId() %>?fileName=${requestScope.ndpiFileName}&x=" + coord.x + "&y=" + coord.y + "&z=0";
        },
        tileSize: new google.maps.Size(256, 256),
        isPng: false,
        maxZoom: 0,
        minZoom: 0
      } );

      var latLng = new google.maps.LatLng(50, -50);

      var slideMapOptions = {
        center: latLng,
        zoom: 3,
        backgroundColor: "BLACK",
        mapTypeControl: false,
        navigationControlOptions: {style: google.maps.NavigationControlStyle.DEFAULT},
        streetViewControl: false,
        disableDoubleClickZoom: false
      };
      slideMap = new google.maps.Map(document.getElementById("slideMap"), slideMapOptions);
      slideMap.mapTypes.set("slide", slideMapType);
      slideMap.setMapTypeId("slide");
      slideMapType.projection = new SlideProjection(slideMap);
      <c:if test="${empty requestScope.zoom}">
        slideMap.panBy(document.getElementById("slideMap").offsetWidth / 2, document.getElementById("slideMap").offsetHeight / 2);
      </c:if>
      <c:if test="${!(empty requestScope.zoom)}">
        slideMap.setCenter(new google.maps.LatLng(${requestScope.lat}, ${requestScope.lng}));
        slideMap.setZoom(${requestScope.zoom});
      </c:if>

      var thumbnailMapOptions = {
        center: latLng,
        zoom: 0,
        draggable: false,
        backgroundColor: "BLACK",
        disableDoubleClickZoom: true,
        keyboardShortcuts: false,
        mapTypeControl: false,
        navigationControl: false,
        streetViewControl: false,
        panControl: false,
        zoomControl: false,
        mapTypeControl: false,
        scaleControl: false,
        streetViewControl: false,
        overviewMapControl: false,
        fullscreenControl: false
      };
      thumbnailMap = new google.maps.Map(document.getElementById("thumbnailMap"), thumbnailMapOptions);
      thumbnailMap.mapTypes.set("thumbnail", thumbnailMapType);
      thumbnailMap.setMapTypeId("thumbnail");
      thumbnailMapType.projection = new SlideProjection(thumbnailMap);
      thumbnailMap.panBy((document.getElementById("thumbnailMap").offsetWidth / 2) - 1, (document.getElementById("thumbnailMap").offsetHeight / 2) - 30);

      var xhairOptions = {
        draggable: true,
        map: thumbnailMap,
        position: slideMap.getCenter()
      };
      xhair = new google.maps.Marker(xhairOptions);

      google.maps.event.addListener(slideMap, "bounds_changed", function() { move(); } );
      google.maps.event.addListener(thumbnailMap, "click", function(event) { thumbnailMapClick(event.latLng); } );
      google.maps.event.addListener(xhair, "dragend", function(event) { xhairDrag(event.latLng); } );

      google.maps.event.addListener(slideMap, "zoom_changed", function() {
          if(slideMap.getZoom() == 0) { document.getElementById("objectivePower").innerHTML = "0.1x"; document.getElementById("objectivePower").style.backgroundColor = "gray"; }
          else if(slideMap.getZoom() == 1) { document.getElementById("objectivePower").innerHTML = "0.2x"; document.getElementById("objectivePower").style.backgroundColor = "gray"; }
          else if(slideMap.getZoom() == 2) { document.getElementById("objectivePower").innerHTML = "0.3x"; document.getElementById("objectivePower").style.backgroundColor = "gray"; }
          else if(slideMap.getZoom() == 3) { document.getElementById("objectivePower").innerHTML = "0.6x"; document.getElementById("objectivePower").style.backgroundColor = "gray"; }
          else if(slideMap.getZoom() == 4) { document.getElementById("objectivePower").innerHTML = "1x"; document.getElementById("objectivePower").style.backgroundColor = "gray"; }
          else if(slideMap.getZoom() == 5) { document.getElementById("objectivePower").innerHTML = "2x"; document.getElementById("objectivePower").style.backgroundColor = "gray"; }
          else if(slideMap.getZoom() == 6) { document.getElementById("objectivePower").innerHTML = "4x"; document.getElementById("objectivePower").style.backgroundColor = "red"; }
          else if(slideMap.getZoom() == 7) { document.getElementById("objectivePower").innerHTML = "10x"; document.getElementById("objectivePower").style.backgroundColor = "yellow"; }
          else if(slideMap.getZoom() == 8) { document.getElementById("objectivePower").innerHTML = "20x"; document.getElementById("objectivePower").style.backgroundColor = "green"; }
          else if(slideMap.getZoom() == 9) { document.getElementById("objectivePower").innerHTML = "40x"; document.getElementById("objectivePower").style.backgroundColor = "blue"; }
      });
  
    }

    var autoMode = true;
    var zoomSeq = [3, 4, 5, 6, 7, 8, 7, 6, 5, 4];
    var zoomSeqIndex = 0;
    var panByX = 0;
    var panByY = 0;
    var panCount = 0;
 
    function robotPan() {
      if(!autoMode) { setTimeout(robotPan, 1000); return; }
      if(panCount == 0) {
        var x = Math.floor(Math.random() * 3);
        if(x == 0) { panByX = -1; }
        else if(x == 1) { panByX = 0; }
        else if(x == 2) { panByX = 1; }
        var y = Math.floor(Math.random() * 3);
        if(y == 0) { panByY = -1; }
        else if(y == 1) { panByY = 0; }
        else if(y == 2) { panByY = 1; }
      }
      if(panCount < 500) {
        panCount++;
        slideMap.panBy(panByX, panByY);
        setTimeout(robotPan, 20);
      }
      else {
        panCount = 0;
        setTimeout(robotZoom, 2000);
      }
      return;
    }

    function robotZoom() {
      if(!autoMode) { setTimeout(robotZoom, 1000); return; }
      zoomSeqIndex = zoomSeqIndex < zoomSeq.length - 1 ? zoomSeqIndex + 1: 0;
      slideMap.setZoom(zoomSeq[zoomSeqIndex]);
      setTimeout(robotPan, 2000);
      return;
    }

    var timeout = null;
    $(document).on('mousemove', function() {
      autoMode = false;
      $("#mode").hide();
      if(timeout != null) {
        clearTimeout(timeout);
      }
      timeout = setTimeout(function() {
        autoMode = true;
        $("#mode").show();
      }, 10000);
    });

  </script>
  <style type="text/css">
    html, body {width: 100%; height: 100%; margin: 0px; overflow: hidden; font-family: arial;}
    #mainContainer {
      width: 100%;
      height: 100%;
      -webkit-box-sizing: border-box; /* Safari/WebKit */
      -moz-box-sizing: border-box; /* Firefox */
      -ms-box-sizing: border-box; /* IE8 */
      box-sizing: border-box; /* W3C Property */
    }
    #thumbnailMap {
      position: absolute;
      right: 0px;
      top: 0px;
      width: 256px;
      height: 256px;
      border-left: 1px solid blue;
      border-bottom: 1px solid blue;
    }
    #thumbnailBlank {
      position: absolute;
      right: 0px;
      top: 226px;
      width: 256px;
      height: 30px;
      color: white;
      background-color: black;
      text-align: center;
    }

    #objectivePower {
      position: absolute;
      left: 50%;
      top: 0px;
      margin-left: -35px;
      width: 70px;
      height: 30px;
      color: black;
      background-color: gray;
      text-align: center;
      font-size: x-large;
      font-weight: bold;
    }
    
    #coords {
      position: absolute;
      left: 0px;
      bottom: 50px;
      width: 50px;
      height: 30px;
      color: black;
      background-color: yellow;
      text-align: left;
      font-size: xx-small;
      font-weight: normal;
    }

    #mode {
      position: absolute;
      left: 50%;
      bottom: 50px;
      margin-left: -300px;
      width: 600px;
      height: 30px;
      color: black;
      background-color: yellow;
      text-align: center;
      font-size: x-large;
      font-weight: bold;
    }
  </style>
</head>
<body onload="{ initialize(); robotPan(); }">
  <div id="mainContainer">
    <div id="slideMap" style="width: 100%; height: 100%;"></div>
  </div>
  <div id="thumbnailMap"></div>
  <div id="thumbnailBlank"></div>
  <div id="objectivePower">0.6x</div>
  <div id="coords">
    X:<span id="x"></span><br/>
    Y:<span id="y"></span><br/>
    Z:<span id="z"></span>
  </div>
  <div id="mode">touch screen to take control</div>
</body>
</html>
