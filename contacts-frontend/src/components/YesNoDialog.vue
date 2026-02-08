<script setup lang="ts">

import {computed, onMounted, onUnmounted, ref} from "vue";

const props = withDefaults(
  defineProps<{
    delay: number
    yesCallback: () => void
    noCallback: () => void
    yesGrayed?: boolean
  }>(),
  {
    yesGrayed: false
  }
)

defineOptions({ inheritAttrs: false })

const now = ref(Date.now())

let timer: number

function getCurrentMilliseconds(): number {
  return Date.now()
}

onMounted(() => {
  timer = window.setInterval(() => {
    now.value = getCurrentMilliseconds()
  }, 250)
})

onUnmounted(() => {
  clearInterval(timer)
})

const startTime = getCurrentMilliseconds()

const remainingTime = computed(() => {
  return props.delay - (now.value - startTime)
})

function getYesButtonPostfix(): string {
  return remainingTime.value > 0 ? ` (${Math.ceil(((remainingTime.value) / 1000))})` : ""
}

</script>

<template>
  <span v-bind="$attrs">
    <button
      class="yesButton buttonRightSpace"
      :class="{ grayed: remainingTime > 0 || yesGrayed }"
      @click="yesCallback"
    >Yes {{ getYesButtonPostfix() }}</button>
    <button class="noButton" @click="noCallback">No</button>
  </span>
</template>

<style scoped>

.yesButton {
  --yes-bg: unset;
  --yes-color: unset;

  background-color: var(--yes-bg, revert);
  color: var(--yes-color, revert);
}

.noButton {
  --no-bg: unset;
  --no-color: unset;

  background-color: var(--no-bg, revert);
  color: var(--no-color, revert);
}

</style>
