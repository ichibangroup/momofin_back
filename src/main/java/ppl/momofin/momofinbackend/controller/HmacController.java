package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.service.HmacService;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class HmacController {

    private final HmacService hmacService;

    @Value("${hmac.secret.key}")
    private String secretKey;

    public HmacController(HmacService hmacService) {
        this.hmacService = hmacService;
    }

    // API endpoint to get HMAC of a file
    @GetMapping("/hmac")
    public String getHmac(@RequestParam String filePath) throws Exception {
        File file = new File(filePath);

        if (!file.exists()) {
            return "File does not exist!";
        }

        // Calculate HMAC for the file
        String hmacHash = hmacService.calculateHmac(file, secretKey, "HmacSHA256");
        return "HMAC: " + hmacHash;
    }
}
