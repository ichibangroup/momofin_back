package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.service.HmacService;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class HmacWebController {
    private final HmacService hmacService;

    String currentHash = "";

    @Value("${hmac.secret.key}")
    private String secretKey;

    public HmacWebController(HmacService hmacService) {
        this.hmacService = hmacService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("hash", currentHash);
        return "uploadForm";
    }

    @PostMapping("/upload-hmac")
    public String uploadFileAndHash(@RequestParam("file") MultipartFile file) throws Exception {
        // Step 1: Get file input stream
        InputStream inputStream = file.getInputStream();

        // Step 2: Calculate the HMAC hash of the file
        currentHash = hmacService.calculateHmac(inputStream, secretKey, "HmacSHA256");

        // Step 3: Return the hash value as response
        return "redirect:/";
    }
}
