package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "return_attachments")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReturnAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="return_id")
    @JsonBackReference
    private ReturnRequest returnRequest;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="return_item_id")
    private ReturnItem returnItem;

    private String fileUrl;
    private String fileType;
    private Long uploadedBy;
    private Instant uploadedAt = Instant.now();
}

