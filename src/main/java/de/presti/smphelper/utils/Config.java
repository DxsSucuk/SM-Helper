package de.presti.smphelper.utils;

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
    boolean setInitialForumTag;
    long currentIndex;
    String devUserId;

}
