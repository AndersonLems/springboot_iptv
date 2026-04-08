package com.anderson.iptv;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, // não sobe servidor HTTP
		properties = {
				"app.m3u.host=http://fake.host",
				"app.m3u.username=fakeuser",
				"app.m3u.password=fakepass",
				"app.tmdb.base-url=https://api.themoviedb.org/3",
				"app.tmdb.api-key=fake-tmdb-key"
		})
class IptvApplicationTests {

	@Test
	void contextLoads() {
	}

}
