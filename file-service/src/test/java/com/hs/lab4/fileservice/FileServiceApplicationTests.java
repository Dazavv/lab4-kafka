package com.hs.lab4.fileservice;

import com.hs.lab4.userservice.FileServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FileServiceApplication.class)
@ActiveProfiles("test")
class FileServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
