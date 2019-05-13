package codepet;

import codepet.storage.StorageFileNotFoundException;
import codepet.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.split;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    private ImageClassifier imageClassifier;

    @Autowired
    public FileUploadController(StorageService storageService, ImageClassifier imageClassifier) {
        this.storageService = storageService;
        this.imageClassifier = imageClassifier;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        storageService.store(file);

        String predictions = "Error";
        try {
            predictions = imageClassifier.classify(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] predictionsArray = predictions.split(" ");
        List<String> predicList = new ArrayList<>();

        Arrays.stream(predictionsArray).forEach(token -> predicList.add(token));

        redirectAttributes.addFlashAttribute("image", "/files/" + file.getOriginalFilename());
        redirectAttributes.addFlashAttribute("predictions", predicList);
        redirectAttributes.addFlashAttribute("message", "Resultados:");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
