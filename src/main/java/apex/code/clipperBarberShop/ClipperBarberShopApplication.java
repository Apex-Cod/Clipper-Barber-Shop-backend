package apex.code.clipperBarberShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClipperBarberShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClipperBarberShopApplication.class, args);
	}

}
