package io.keepcoding.pickandgol.model.mapper;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.manager.net.response.PubDetailResponse;
import io.keepcoding.pickandgol.manager.net.response.PubListResponse;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;


/**
 * This class is used to map a PubListResponse.PubListData object to a PubAggregate object.
 */
public class PubListDataToPubAggregateMapper {

    public PubAggregate map(PubListResponse.PubListData data) {

        PubAggregate mappedPubs = PubAggregate.buildEmpty();
        mappedPubs.setTotalResults(data.getTotal());

        if (data.getPubList().size() > 0) {

            List<Pub> pubList = new ArrayList<>();
            for (PubDetailResponse.PubDetailData pubData: data.getPubList()) {

                Double latitude, longitude;
                boolean hasLocation;

                if ( pubData.getLocation() != null &&
                     pubData.getLocation().getCoordinates() != null &&
                     pubData.getLocation().getCoordinates().size() == 2 ) {

                    longitude = pubData.getLocation().getCoordinates().get(0);
                    latitude = pubData.getLocation().getCoordinates().get(1);

                    hasLocation = true;
                }
                else {
                    latitude = longitude = 0.0;
                    hasLocation = false;
                }

                Pub newPub = new Pub(
                        pubData.getId(),
                        pubData.getName(),
                        hasLocation,
                        latitude,
                        longitude,
                        pubData.getUrl(),
                        pubData.getOwner(),
                        pubData.getEvents(),
                        pubData.getPhotos()
                );

                pubList.add(newPub);
            }

            mappedPubs.setAll(pubList);
        }

        return mappedPubs;
    }
}
