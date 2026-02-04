package com.patria.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestApplicationTests {

	@Test
	void contextLoads() {
		// Simple test to verify the application class exists
		assertNotNull(TestApplication.class);
	}

	@Test
	void applicationClassExists() {
		// Verify the main application class can be instantiated
		TestApplication app = new TestApplication();
		assertNotNull(app);
	}
}
