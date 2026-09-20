package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameContextRepository extends JpaRepository<GameContext, Integer> {
    @Query("""
            select context from GameContext context
            where context.status = :status
              and (:game is null or lower(context.game) like lower(concat('%', :game, '%')))
              and (:platform is null or lower(context.platform) like lower(concat('%', :platform, '%')))
              and (:region is null or lower(context.region) like lower(concat('%', :region, '%')))
            order by context.id asc
            """)
    Page<GameContext> searchByStatusAndAttributes(@Param("status") GameContextStatus status,
                                                    @Param("game") String game,
                                                    @Param("platform") String platform,
                                                    @Param("region") String region,
                                                    Pageable pageable);

    default GameContext findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(GameContext.class, id));
    }
}
