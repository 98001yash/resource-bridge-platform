package com.resourcebridge.auth_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Point getLocation(String address) {

        log.info("Geocoding address: {}", address);

        try {

            String url = "https://nominatim.openstreetmap.org/search"
                    + "?q=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
                    + "&format=json&limit=1";

            log.debug("Calling Geocoding API: {}", url);

            String response =
                    restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                log.error("Empty response from Geo API");
                throw new RuntimeException("Geo API failed");
            }

            JSONArray arr = new JSONArray(response);

            if (arr.isEmpty()) {
                log.warn("No location found for: {}", address);
                throw new RuntimeException("Invalid address");
            }

            JSONObject obj = arr.getJSONObject(0);

            double lat = obj.getDouble("lat");
            double lon = obj.getDouble("lon");

            log.info("Geocoded [{}] → lat={}, lon={}",
                    address, lat, lon);

            GeometryFactory factory =
                    new GeometryFactory(new PrecisionModel(), 4326);

            return factory.createPoint(
                    new Coordinate(lon, lat));

        } catch (Exception e) {

            log.error("Geocoding failed for: {}", address, e);

            throw new RuntimeException(
                    "Failed to process address");
        }
    }
}
