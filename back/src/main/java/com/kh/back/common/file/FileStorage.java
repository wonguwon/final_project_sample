package com.kh.back.common.file;

import com.kh.back.common.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드 파일을 서버 폴더에 저장 · 조회 · 삭제한다. (게시판 외에 프로필 사진 등에서도 다시 쓸 수 있게 common 에 둔다)
 *
 * 핵심 규칙
 * 1. 디스크에는 사용자가 보낸 이름이 아니라 "UUID + 확장자" 로 저장한다 → 이름 충돌 · 경로 조작 방지
 * 2. 허용한 확장자만 받는다 → .html · .svg · .exe 같은 위험한 파일 차단
 * 3. DB 는 롤백되어도 디스크 파일은 롤백되지 않는다 → 트랜잭션 결과에 맞춰 파일을 정리한다
 */
@Slf4j
@Component
public class FileStorage {

    // 화면에 바로 보여 줘도 되는 이미지 확장자 (svg 는 스크립트를 담을 수 있어서 제외)
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    // 첨부를 허용하는 전체 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "txt", "csv", "xlsx", "docx", "pptx", "zip");

    private final Path uploadDir;

    public FileStorage(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir); // 폴더가 없으면 만든다
        log.info("업로드 폴더: {}", this.uploadDir);
    }

    // 파일을 저장하고, 디스크에 저장한 이름(UUID.확장자)을 돌려준다
    public String store(MultipartFile file) {
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_NOT_ALLOWED",
                    "첨부할 수 없는 파일 형식입니다: " + file.getOriginalFilename());
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = resolve(storedName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패: " + storedName, e);
        }

        // 이 요청의 트랜잭션이 롤백되면(DB 저장 실패 등) 방금 저장한 파일도 지운다 → 주인 없는 파일이 남지 않게
        afterTransaction(TransactionSynchronization.STATUS_ROLLED_BACK, storedName);
        return storedName;
    }

    // 파일을 지운다. 트랜잭션 안이면 "커밋에 성공한 뒤" 지운다 → DB 삭제가 실패했는데 파일만 사라지는 일이 없게
    public void delete(String storedName) {
        if (!afterTransaction(TransactionSynchronization.STATUS_COMMITTED, storedName)) {
            deleteNow(storedName); // 트랜잭션 밖이면 바로 지운다
        }
    }

    public Resource load(String storedName) {
        return new FileSystemResource(resolve(storedName));
    }

    // 브라우저에서 바로 보여 줘도 되는 이미지인가 (아니면 다운로드로 내려준다)
    public boolean isImage(String fileName) {
        return IMAGE_EXTENSIONS.contains(extensionOf(fileName));
    }

    // 트랜잭션이 끝났을 때 status(커밋 · 롤백)가 맞으면 파일을 지우도록 예약한다. 트랜잭션 밖이면 false
    private boolean afterTransaction(int status, String storedName) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int completedStatus) {
                if (completedStatus == status) {
                    deleteNow(storedName);
                }
            }
        });
        return true;
    }

    private void deleteNow(String storedName) {
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", storedName, e); // 파일 삭제 실패로 요청 전체를 실패시키지는 않는다
        }
    }

    // 저장 폴더 안의 경로로만 바꾼다. "../" 같은 이름으로 폴더 밖을 가리키면 거부
    private Path resolve(String storedName) {
        Path path = uploadDir.resolve(storedName).normalize();
        if (!path.startsWith(uploadDir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_INVALID_PATH", "잘못된 파일 경로입니다.");
        }
        return path;
    }

    private String extensionOf(String fileName) {
        String ext = StringUtils.getFilenameExtension(fileName);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }
}
