package com.mashup.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TicketmasterResponse {

    @JsonProperty("_embedded")
    private Embedded embedded;

    @Data
    public static class Embedded {
        private List<Event> events;
    }

    @Data
    public static class Event {
        private String id;
        private String name;
        private Dates dates;
        @JsonProperty("_embedded")
        private EventEmbedded embedded;
        private List<Classification> classifications;

        @Data
        public static class Dates {
            private Start start;

            @Data
            public static class Start {
                private String localDate;
                private String localTime;
            }
        }

        @Data
        public static class EventEmbedded {
            private List<Venue> venues;
        }

        @Data
        public static class Venue {
            private String name;
        }

        @Data
        public static class Classification {
            private Segment segment;

            @Data
            public static class Segment {
                private String name;
            }
        }
    }
}