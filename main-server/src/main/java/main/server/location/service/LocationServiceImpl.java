package main.server.location.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.location.Location;
import main.server.location.LocationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class LocationServiceImpl implements LocationService {
    LocationRepository locationRepository;

    @Override
    public Location save(Location location) {
        return locationRepository.save(location);
    }
}
