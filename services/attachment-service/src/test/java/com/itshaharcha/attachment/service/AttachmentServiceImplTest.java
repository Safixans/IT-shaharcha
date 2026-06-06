package com.itshaharcha.attachment.service;

import com.itshaharcha.attachment.config.MinioProperties;
import com.itshaharcha.attachment.entity.Attachment;
import com.itshaharcha.attachment.repository.AttachmentRepository;
import com.itshaharcha.attachment.service.impl.AttachmentServiceImpl;
import com.itshaharcha.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock private AttachmentRepository attachmentRepository;
    @Mock private StorageService storage;
    private final MinioProperties props =
            new MinioProperties("http://minio:9000", "http://localhost:9000", "k", "s", "bucket", 600);

    private AttachmentServiceImpl service;

    private final UUID owner = UUID.randomUUID();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new AttachmentServiceImpl(attachmentRepository, storage, props);
    }

    @Test
    void upload_rejectsEmptyFile() {
        var empty = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> service.upload(empty, owner))
                .isInstanceOf(ApplicationException.class);
        verify(storage, never()).put(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void upload_storesObjectAndPersistsMetadata() {
        var file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "bytes".getBytes());
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(i -> {
            Attachment a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        var ref = service.upload(file, owner);

        assertThat(ref.fileId()).isNotNull();
        assertThat(ref.contentType()).isEqualTo("audio/mpeg");
        assertThat(ref.originalName()).isEqualTo("audio.mp3");
        verify(storage).put(anyString(), any(), eq(5L), eq("audio/mpeg"));
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void download_returnsPresignedUrl() {
        UUID id = UUID.randomUUID();
        Attachment a = new Attachment();
        a.setId(id);
        a.setObjectKey("key/obj.mp3");
        a.setContentType("audio/mpeg");
        a.setOriginalName("obj.mp3");
        a.setSizeBytes(123);
        when(attachmentRepository.findById(id)).thenReturn(Optional.of(a));
        when(storage.presignedGet(eq("key/obj.mp3"), anyInt())).thenReturn("http://localhost:9000/bucket/key/obj.mp3?X-Amz=sig");

        var dl = service.download(id);

        assertThat(dl.url()).contains("X-Amz");
        assertThat(dl.expiresInSeconds()).isEqualTo(600);
        assertThat(dl.contentType()).isEqualTo("audio/mpeg");
    }

    @Test
    void download_notFound() {
        UUID id = UUID.randomUUID();
        when(attachmentRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.download(id)).isInstanceOf(ApplicationException.class);
    }
}
