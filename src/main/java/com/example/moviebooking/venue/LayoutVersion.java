package com.example.moviebooking.venue;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="layout_versions", uniqueConstraints=@UniqueConstraint(columnNames={"screen_id","version_number"}))
public class LayoutVersion {
 @Id @Column(length=36) public String id=UUID.randomUUID().toString();
 @Column(nullable=false,length=36) public String screenId;
 @Column(nullable=false) public int versionNumber;
 @Column(nullable=false,length=120) public String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) public LayoutVersionStatus status=LayoutVersionStatus.DRAFT;
 public Instant publishedAt;
 @Lob @Column(nullable=false,columnDefinition="LONGTEXT") public String snapshot;
 @Column(nullable=false) public Instant createdAt=Instant.now();
}
