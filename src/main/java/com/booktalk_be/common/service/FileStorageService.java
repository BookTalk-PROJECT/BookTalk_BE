package com.booktalk_be.common.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.image-dir:uploads/images}")
    private String uploadDir;

    private Path fileStorageLocation;

    // 허용된 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    /**
     * 서비스 초기화 시 업로드 디렉토리 생성
     */
    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    /**
     * 파일 저장
     * @param file 업로드할 파일
     * @return 저장된 파일명
     * @throws IOException 파일 저장 실패 시
     */
    public String storeFile(MultipartFile file) throws IOException {
        // 파일명 검증
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("파일명에 부적절한 경로가 포함되어 있습니다: " + originalFileName);
        }

        // 파일 확장자 검증
        String fileExtension = getFileExtension(originalFileName);
        if (!isAllowedExtension(fileExtension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + fileExtension);
        }

        // 고유한 파일명 생성 (UUID + 원본 확장자)
        String uniqueFileName = generateUniqueFileName(fileExtension);

        // 파일 저장
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return uniqueFileName;
    }

    /**
     * 파일 경로 로드
     * @param fileName 파일명
     * @return 파일 경로
     */
    public Path loadFile(String fileName) {
        // 파일명 검증
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("부적절한 파일 경로입니다: " + fileName);
        }

        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    /**
     * 파일 존재 여부 확인
     * @param fileName 파일명
     * @return 존재 여부
     */
    public boolean fileExists(String fileName) {
        try {
            Path filePath = loadFile(fileName);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일 삭제
     * @param fileName 삭제할 파일명
     * @throws IOException 파일 삭제 실패 시
     */
    public void deleteFile(String fileName) throws IOException {
        Path filePath = loadFile(fileName);
        Files.deleteIfExists(filePath);
    }

    /**
     * 여러 파일 삭제
     * @param fileNames 삭제할 파일명 목록
     */
    public void deleteFiles(List<String> fileNames) {
        for (String fileName : fileNames) {
            try {
                deleteFile(fileName);
            } catch (IOException e) {
                // 로그만 남기고 계속 진행
                System.err.println("파일 삭제 실패: " + fileName + " - " + e.getMessage());
            }
        }
    }

    /**
     * 파일 크기 가져오기
     * @param fileName 파일명
     * @return 파일 크기 (bytes)
     * @throws IOException 파일 조회 실패 시
     */
    public long getFileSize(String fileName) throws IOException {
        Path filePath = loadFile(fileName);
        return Files.size(filePath);
    }

    /**
     * 파일 확장자 추출
     * @param fileName 파일명
     * @return 확장자 (소문자, 점 포함)
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex).toLowerCase();
        }
        return "";
    }

    /**
     * 허용된 확장자인지 확인
     * @param extension 확장자
     * @return 허용 여부
     */
    private boolean isAllowedExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 고유한 파일명 생성
     * @param extension 파일 확장자
     * @return UUID 기반의 고유한 파일명
     */
    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 업로드 디렉토리 경로 반환
     * @return 업로드 디렉토리 경로
     */
    public String getUploadDir() {
        return this.fileStorageLocation.toString();
    }
}
