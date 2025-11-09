package com.zyna.dev.ecommerce.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * ✅ Tự động tạo thư mục uploads khi ứng dụng khởi động lần đầu.
 */
@Slf4j
@Configuration
public class StartupInitializer implements CommandLineRunner {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    @Override
    public void run(String... args) {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("📁 Created upload directory at: {}", uploadDir.getAbsolutePath());
            } else {
                log.error("❌ Failed to create upload directory at: {}", uploadDir.getAbsolutePath());
            }
        } else {
            log.info("✅ Upload directory already exists: {}", uploadDir.getAbsolutePath());
        }
    }
}
