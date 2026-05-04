package com.autovalor.api.model;

import com.autovalor.user.User;
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
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private User senderUser;

    @Column(nullable = false, length = 120)
    private String contactName;

    @Column(nullable = false, length = 160)
    private String contactEmail;

    @Column(length = 40)
    private String contactPhone;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected ContactMessage() {
    }

    public ContactMessage(
            Listing listing,
            User senderUser,
            String contactName,
            String contactEmail,
            String contactPhone,
            String message
    ) {
        this.listing = listing;
        this.senderUser = senderUser;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public User getSenderUser() {
        return senderUser;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
