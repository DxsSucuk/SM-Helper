package de.presti.smphelper.dto;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Punishments {
    long userId;
    long punishmentCount;

    public void addViolation(long amount) {
        setPunishmentCount(getPunishmentCount() + amount);
    }
}
