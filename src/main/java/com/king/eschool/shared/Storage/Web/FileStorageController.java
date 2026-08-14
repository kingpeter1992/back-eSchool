package com.king.eschool.shared.Storage.Web;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.shared.Storage.Services.FileStorageService;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileStorageController {

    private final FileStorageService service;
    public FileStorageController(FileStorageService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public FileDocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("module") String module,
            @RequestParam("referenceId") Long referenceId
    ) {
        return service.uploadFile(file, module, referenceId);
    }

    @GetMapping
    public List<FileDocumentResponse> getFiles(
            @RequestParam String module,
            @RequestParam Long referenceId
    ) {
        return service.getFiles(module, referenceId);
    }
}
