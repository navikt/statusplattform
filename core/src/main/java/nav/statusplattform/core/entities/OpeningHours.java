package nav.statusplattform.core.entities;

import java.time.LocalTime;

public class OpeningHours {

    private LocalTime startTime;

    private LocalTime endTime;

    public OpeningHours() {
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
