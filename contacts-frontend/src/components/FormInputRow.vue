<script setup lang="ts">

defineProps<{
  label: string
  required: boolean
  hasError: boolean
}>()

</script>

<template>

  <div>
    <span>
      <span :class="{ error: hasError }">{{ label }}</span><span v-if="required" class="justRed">*</span>:
    </span>

    <span>
      <slot />
    </span>

  </div>

</template>

<style scoped>

</style>
