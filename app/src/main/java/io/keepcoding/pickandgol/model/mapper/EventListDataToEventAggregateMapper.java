package io.keepcoding.pickandgol.model.mapper;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.manager.net.response.EventDetailResponse;
import io.keepcoding.pickandgol.manager.net.response.EventListResponse;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;


/**
 * This class is used to map an EventListResponse.EventListData object to a EventAggregate object.
 */
public class EventListDataToEventAggregateMapper {

    public EventAggregate map(EventListResponse.EventListData data) {

        EventAggregate mappedEvents = EventAggregate.buildEmpty();
        mappedEvents.setTotalResults(data.getTotal());

        if (data.getEventList().size() > 0) {

            List<Event> eventList = new ArrayList<>();
            for (EventDetailResponse.EventDetailData eventData: data.getEventList()) {

                Event newEvent = new Event(eventData.getId(),
                                           eventData.getName(),
                                           eventData.getDate(),
                                           eventData.getDescription(),
                                           eventData.getPhotoUrl(),
                                           eventData.getCategories().get(0),
                                           eventData.getPubs()
                );

                eventList.add(newEvent);
            }

            mappedEvents.setAll(eventList);
        }

        return mappedEvents;
    }
}
