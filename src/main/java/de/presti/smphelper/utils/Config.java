package de.presti.smphelper.utils;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    long guildId;
    long channelId;
    long testerReportChannelId;
    long forumChannelId, testerForumChannelId;
    long respondToMessageCategory;
    boolean sendInitialMessage;
    boolean setInitialForumTag;
    long currentIndex;
    String devUserId;

    List<CrashReport> reportList;

}
