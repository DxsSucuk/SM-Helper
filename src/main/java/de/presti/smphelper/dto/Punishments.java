package de.presti.smphelper.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Punishments {

    @Id
    long userId;

    @Column(nullable = false)
    long punishmentCount;

    public void addViolation(long amount) {
        setPunishmentCount(getPunishmentCount() + amount);
    }
}
