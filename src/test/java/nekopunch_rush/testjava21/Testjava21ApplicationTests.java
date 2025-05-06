package nekopunch_rush.testjava21;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
class Testjava21ApplicationTests {

	@Test
	void contextLoads() {
	}

}
