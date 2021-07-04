package com.deepdev.fordconnected.server.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "visits")
public class Visit {
    @Id
    private String id;
    private String ip;
    private String browserId;
    private String deviceId;
    private Boolean isMobile;
    private LocalDateTime createdAt;
    private LocalDateTime lastVisited;
    private List<LocalDateTime> previousDatesVisited;

    public boolean addDateVisited(LocalDateTime dateVisited) {
        // variables for throttling
        long maxNumberVisitDatesStored = 500;
        long visitThrottleInSeconds = 300;

        try {
            // init local var to store visit times
            if (previousDatesVisited == null) {
                previousDatesVisited = new ArrayList<LocalDateTime>();
            }

            // add visit time if did not visit website in last x seconds
            if (previousDatesVisited.size() > 0) {
                long timeSinceLastVisited = Duration
                        .between(previousDatesVisited.get(previousDatesVisited.size() - 1), dateVisited).toMillis();
                long timeSinceLastVisitedSeconds = TimeUnit.MILLISECONDS.toSeconds(timeSinceLastVisited);

                if (timeSinceLastVisitedSeconds > visitThrottleInSeconds) {
                    previousDatesVisited.add(dateVisited);
                }
            } else {
                previousDatesVisited.add(dateVisited);
            }

            // ensure less than 500 visit times are present
            while (previousDatesVisited.size() > maxNumberVisitDatesStored) {
                previousDatesVisited.remove(0);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}