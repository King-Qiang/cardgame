<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import DoudizhuBoard from './DoudizhuBoard.vue'
import ReplayControls from './ReplayControls.vue'
import { buildBoardAtStep } from '../../composables/useReplayReducer'
import type { GameActionLogItem } from '../../api/record'

const props = defineProps<{
  gameType: string
  actions: GameActionLogItem[]
}>()

const currentStep = ref(0)
const playing = ref(false)
const speed = ref<1 | 2 | 4>(1)
let timer: number | undefined

const totalSteps = computed(() => props.actions.length)
const board = computed(() => buildBoardAtStep(props.actions, currentStep.value))
const currentAction = computed(() => props.actions[currentStep.value])

watch(
  () => props.actions,
  () => {
    currentStep.value = 0
    playing.value = false
  },
)

watch(playing, (val) => {
  if (val) startPlay()
  else stopPlay()
})

function startPlay() {
  stopPlay()
  timer = window.setInterval(() => {
    if (currentStep.value >= totalSteps.value - 1) {
      playing.value = false
      return
    }
    currentStep.value++
  }, 800 / speed.value)
}

function stopPlay() {
  if (timer) {
    clearInterval(timer)
    timer = undefined
  }
}

function togglePlay() {
  playing.value = !playing.value
}

function prevStep() {
  playing.value = false
  if (currentStep.value > 0) currentStep.value--
}

function nextStep() {
  playing.value = false
  if (currentStep.value < totalSteps.value - 1) currentStep.value++
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowLeft') prevStep()
  if (e.key === 'ArrowRight') nextStep()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => {
  stopPlay()
  window.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <div class="replay-player">
    <div class="replay-header">
      <span>{{ gameType }} 回放</span>
      <el-tag size="small">{{ board.phase }}</el-tag>
    </div>

    <DoudizhuBoard v-if="gameType === 'DOUDIZHU'" :board="board" />

    <el-alert
      v-if="currentAction"
      :title="board.currentDescription || currentAction.action"
      type="info"
      :closable="false"
      show-icon
      style="margin-top: 12px"
    />

    <ReplayControls
      :current-step="currentStep"
      :total-steps="totalSteps"
      :playing="playing"
      :speed="speed"
      @prev="prevStep"
      @next="nextStep"
      @toggle="togglePlay"
      @update:speed="speed = $event"
      @update:current-step="currentStep = $event"
    />
  </div>
</template>

<style scoped>
.replay-player {
  margin-bottom: 20px;
}
.replay-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
}
</style>
