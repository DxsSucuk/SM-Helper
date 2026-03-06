package de.presti.smphelper.dto;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CrashReport {
    long channelId;
    long ownerId;
}
