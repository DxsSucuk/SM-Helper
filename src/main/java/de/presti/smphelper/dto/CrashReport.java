package de.presti.smphelper.dto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CrashReport {

    @Id
    @Column(nullable = false)
    long channelId;

    @Column(nullable = false)
    long ownerId;
}
