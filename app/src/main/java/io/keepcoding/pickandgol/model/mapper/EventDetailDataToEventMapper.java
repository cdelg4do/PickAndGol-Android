package io.keepcoding.pickandgol.model.mapper;

import io.keepcoding.pickandgol.manager.net.response.EventDetailResponse.EventDetailData;
import io.keepcoding.pickandgol.model.Event;

/**
 * This class is used to map an EventDetailResponse.EventDetailData object to an Event model object.
 */
public class EventDetailDataToEventMapper {

    public Event map(EventDetailData data) {

        Event event = new Event(
                data.getId(),
                data.getName(),
                data.getDate(),
                data.getDescription(),
                data.getPhotoUrl(),
                data.getCategories().get(0),
                data.getPubs()
        );

        return event;
    }
}
