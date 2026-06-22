<script setup lang="ts">
import { formatCardLabel } from '../../composables/useReplayReducer'
import type { DoudizhuBoardState } from '../../composables/useReplayReducer'

defineProps<{
  board: DoudizhuBoardState
}>()
</script>

<template>
  <div class="doudizhu-board">
    <div v-for="(seat, index) in board.seats" :key="index" class="seat-row">
      <div class="seat-info">
        <el-tag v-if="seat.isLandlord" type="warning" size="small">地主</el-tag>
        <span>座位 {{ index }} · 玩家 {{ seat.userId }}</span>
        <span class="hand-count">剩 {{ seat.handCount }} 张</span>
      </div>
    </div>

    <div v-if="board.bottomCards.length" class="bottom-cards">
      <span class="label">底牌：</span>
      <span v-for="card in board.bottomCards" :key="card" class="card">{{ formatCardLabel(card) }}</span>
    </div>

    <div v-if="board.lastPlay" class="last-play">
      <span class="label">上家出牌（座位 {{ board.lastPlay.seat }}）：</span>
      <span v-for="card in board.lastPlay.cards" :key="card" class="card red">{{
        formatCardLabel(card)
      }}</span>
    </div>

    <div v-if="!board.seats.length" class="empty">暂无牌桌数据，请查看下方 action 列表</div>
  </div>
</template>

<style scoped>
.doudizhu-board {
  min-height: 200px;
  padding: 16px;
  background: #1a472a;
  border-radius: 8px;
  color: #fff;
}
.seat-row {
  margin-bottom: 12px;
}
.seat-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hand-count {
  opacity: 0.85;
  font-size: 13px;
}
.bottom-cards,
.last-play {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}
.label {
  margin-right: 8px;
  opacity: 0.9;
}
.card {
  display: inline-block;
  margin-right: 6px;
  padding: 2px 6px;
  background: #fff;
  color: #333;
  border-radius: 4px;
  font-size: 13px;
}
.card.red {
  color: #c0392b;
}
.empty {
  opacity: 0.8;
  text-align: center;
  padding: 40px 0;
}
</style>
