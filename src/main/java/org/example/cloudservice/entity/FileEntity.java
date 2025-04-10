package org.example.cloudservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "files", schema = "cloud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "filesize_bytes", nullable = false)
    private Long filesizeBytes;

    @Column(name = "hash")
    private String hash;

    @Column(name = "s3_link", nullable = false)
    private String s3Link;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (deleted == null) {
            deleted = false;
        }
    }
}
