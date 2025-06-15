package org.L2.untitled.model;

import lombok.Data;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @ElementCollection
    private List<String> answer = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<GameRecord> records = new ArrayList<>();

    private boolean completed;
}