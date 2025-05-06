package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class MealResponse {

    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    private String content;
    private int calories;
    private String photoUrl;
}

