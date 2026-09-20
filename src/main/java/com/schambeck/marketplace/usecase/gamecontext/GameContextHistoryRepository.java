package com.schambeck.marketplace.usecase.gamecontext;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameContextHistoryRepository extends JpaRepository<GameContextHistory, Integer> {
    java.util.List<GameContextHistory> findAllByGameContextIdOrderByIdAsc(Integer gameContextId);
}
