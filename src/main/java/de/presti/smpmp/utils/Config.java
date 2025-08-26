package de.presti.smpmp.utils;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    long guildId;
    long channelId;
    long forumChannelId;
    boolean sendInitialMessage;
    long currentIndex;

}
