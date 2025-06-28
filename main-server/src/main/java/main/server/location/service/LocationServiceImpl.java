package main.server.location.service;

import main.server.location.Location;
import main.server.location.LocationRepository;

public class LocationServiceImpl implements LocationService {
    LocationRepository locationRepository;

    @Override
    public Location save(Location location) {
        return locationRepository.save(location);
    }
}
