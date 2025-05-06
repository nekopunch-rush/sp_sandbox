package nekopunch_rush.testjava21;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestTestjava21Application {

	public static void main(String[] args) {
		SpringApplication.from(Testjava21Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
