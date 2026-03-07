<script setup lang="ts">

withDefaults(
  defineProps<{
    label: string
    editing: boolean
    changed: boolean
    error?: string | null
    postLabelChar?: string
  }>(),
  {
    error: null,
    postLabelChar: ":"
  }
)

defineEmits(['discard', 'edit', 'save'])

</script>

<template>
  <span>
    <span class="label">
      {{ label }}{{ postLabelChar + ' ' }}
    </span>

    <span class="content">
      <template v-if="editing">
        <slot name="edit" />
      </template>
      <template v-else>
        <slot name="view" />
      </template>
    </span>

    <span class="actions">
      {{ ' ' }}
      <template v-if="editing">
        <a class="clickable" @click="$emit('discard')">❌</a>
        <a v-if="changed" class="clickable" @click="$emit('save')">✅</a>
      </template>
      <template v-else>
        <a class="clickable" @click="$emit('edit')">✏️</a>
      </template>
    </span>

    <span v-if="error" class="error">{{ error }}</span>
  </span>
</template>

<!--<style scoped>-->

<!--.label {-->
<!--  justify-self: start;-->
<!--}-->

<!--.content {-->
<!--  justify-self: start;-->
<!--}-->

<!--.actions {-->
<!--  justify-self: start;-->
<!--}-->

<!--</style>-->
