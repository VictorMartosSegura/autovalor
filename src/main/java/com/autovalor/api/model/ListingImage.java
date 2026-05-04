package com.autovalor.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "listing_images")
public class ListingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false, length = 80)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected ListingImage() {
    }

    public ListingImage(Listing listing, String fileName, String url, String contentType, Long sizeBytes) {
        this.listing = listing;
        this.fileName = fileName;
        this.url = url;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public Long getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
