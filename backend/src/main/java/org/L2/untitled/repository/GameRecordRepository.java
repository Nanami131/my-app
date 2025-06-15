package org.L2.untitled.repository;

import org.L2.untitled.model.GameRecord;
import org.springframework.data.repository.CrudRepository;

public interface GameRecordRepository extends CrudRepository<GameRecord, Long> {
}