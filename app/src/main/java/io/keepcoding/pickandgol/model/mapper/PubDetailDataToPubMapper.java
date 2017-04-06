package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.PubDetailResponse.PubDetailData;
import io.keepcoding.pickandgol.model.Pub;

/**
 * This class is used to map an PubDetailResponse.PubDetailData object to a Pub model object.
 */
public class PubDetailDataToPubMapper {

    public Pub map(PubDetailData data) {

        Double latitude, longitude;
        boolean hasLocation;

        if ( data.getLocation() != null &&
                data.getLocation().getCoordinates() != null &&
                data.getLocation().getCoordinates().size() == 2 ) {

            longitude = data.getLocation().getCoordinates().get(0);
            latitude = data.getLocation().getCoordinates().get(1);

            hasLocation = true;
        }
        else {
            latitude = longitude = 0.0;
            hasLocation = false;
        }

        Pub pub = new Pub(
                data.getId(),
                data.getName(),
                hasLocation,
                latitude,
                longitude,
                data.getUrl(),
                data.getOwner(),
                data.getEvents(),
                data.getPhotos()
        );

        return pub;
    }
}
