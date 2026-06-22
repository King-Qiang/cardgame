package io.github.kingqiang.cardgame.cardgamebackend.record.repository;

import io.github.kingqiang.cardgame.cardgamebackend.record.entity.GameSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JPA 仓储：GameSettlement 数据访问。
 */
public interface GameSettlementRepository extends JpaRepository<GameSettlement, Long> {

    List<GameSettlement> findByRecordId(String recordId);

    Optional<GameSettlement> findByRecordIdAndUserId(String recordId, long userId);

    boolean existsByRecordId(String recordId);

    boolean existsByRecordIdAndUserId(String recordId, long userId);

    @Query(value = """
            SELECT gr.record_id, gr.room_id, gr.game_type, gr.status, gr.start_at, gr.end_at,
                   gs.gold_delta, gs.score, gr.result_json, rm.mode
            FROM game_settlement gs
            INNER JOIN game_record gr ON gr.record_id = gs.record_id
            LEFT JOIN game_room rm ON rm.room_id = gr.room_id
            WHERE gs.user_id = :userId AND gr.status = 'FINISHED'
            AND (:gameType IS NULL OR :gameType = '' OR gr.game_type = :gameType)
            AND (:mode IS NULL OR :mode = ''
                 OR COALESCE(rm.mode, JSON_UNQUOTE(JSON_EXTRACT(gr.result_json, '$.mode'))) = :mode)
            ORDER BY gr.end_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM game_settlement gs
            INNER JOIN game_record gr ON gr.record_id = gs.record_id
            LEFT JOIN game_room rm ON rm.room_id = gr.room_id
            WHERE gs.user_id = :userId AND gr.status = 'FINISHED'
            AND (:gameType IS NULL OR :gameType = '' OR gr.game_type = :gameType)
            AND (:mode IS NULL OR :mode = ''
                 OR COALESCE(rm.mode, JSON_UNQUOTE(JSON_EXTRACT(gr.result_json, '$.mode'))) = :mode)
            """,
            nativeQuery = true)
    Page<Object[]> findPlayerFinishedRecords(
            @Param("userId") long userId,
            @Param("gameType") String gameType,
            @Param("mode") String mode,
            Pageable pageable);
}
