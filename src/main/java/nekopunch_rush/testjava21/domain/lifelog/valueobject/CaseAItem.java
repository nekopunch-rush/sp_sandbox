package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.Getter;
import lombok.Value;

@Value(staticConstructor = "of")
@Getter
public class CaseAItem {
    String name;

}
