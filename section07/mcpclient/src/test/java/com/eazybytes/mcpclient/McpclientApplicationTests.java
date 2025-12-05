package com.eazybytes.mcpclient;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpclientApplicationTests {

	@Test
	void contextLoads(ApplicationContext appCtx) {
        assertThat(appCtx).isNotNull();
	}

}
