package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ValidationErrorResponse {
    private Map<String, String> errors;
}
