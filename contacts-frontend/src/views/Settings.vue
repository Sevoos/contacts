<script setup lang="ts">

import {
  automatedSelection,
  availableExplicitTimezones,
  configsEqual,
  explicitTimezone,
  fetchAllContactsOnlyManually, fetchAllContactsPeriod,
  foolproofDeleteDelay, memorizeTimezoneDuringContactCreation,
  type MyConfigKeyName,
  readConfig, setCreatedIconAsDefault, toBoolean,
  writeConfig
} from "@/config/settings.ts";
import {computed, reactive, toRaw, watch} from "vue";
import router from "@/router";
import ConfigRow from "@/components/ConfigRow.vue";
import {detectedTimezone} from "@/utils/TimezoneUtils.ts";
import {homePath} from "@/router/paths.ts";
import {deletePatternFromInputEvent, nonDigitRegExp} from "@/utils/Utils.ts";

const savedConfig = reactive(readConfig())
const editedConfig = reactive(readConfig())

function onFetchAllContactsPeriodInput(e: InputEvent) {
  const result = deletePatternFromInputEvent(e, nonDigitRegExp)
  editedConfig.fetchAllContactsPeriod = Number(result)
}

const editing = reactive<Record<MyConfigKeyName, boolean>>({
  explicitTimezone: false,
  foolproofDeleteDelay: false,
  fetchAllContactsOnlyManually: false,
  fetchAllContactsPeriod: false,
  memorizeTimezoneDuringContactCreation: false,
  setCreatedIconAsDefault: false,
})

const hasAnythingChanged = computed(() => !configsEqual(savedConfig, editedConfig))

function resetEditing() {
  for (const key in editing) {
    editing[key as MyConfigKeyName] = false
  }
}

function discardAll() {
  resetEditing()
  Object.assign(editedConfig, readConfig())
}


function saveAll() {
  resetEditing()
  Object.assign(savedConfig, structuredClone(toRaw(editedConfig)))
}

watch(
  savedConfig,
  currentValue => writeConfig(currentValue),
  { deep: true }
)

const editedExplicitTimezoneUi = computed({
  get() {
    return editedConfig.explicitTimezone ?? automatedSelection
  },
  set(v) {
    editedConfig.explicitTimezone =
      v === automatedSelection ? null : v
  }
})

function hasChanged<K extends MyConfigKeyName>(key: K) {
  return editedConfig[key] !== savedConfig[key]
}

function startEditing<K extends MyConfigKeyName>(key: K) {
  editing[key] = true
}

function save<K extends MyConfigKeyName>(key: K) {
  savedConfig[key] = editedConfig[key]
  editing[key] = false
}

function discard<K extends MyConfigKeyName>(key: K) {
  editedConfig[key] = savedConfig[key]
  editing[key] = false
}

const shouldGrayOutPeriod = computed(() => toBoolean(String(savedConfig.fetchAllContactsOnlyManually)))

</script>

<template>

  <div>
  <ConfigRow
    label="Timezone"
    :editing="editing.explicitTimezone"
    :changed="hasChanged(explicitTimezone)"
    @edit="startEditing(explicitTimezone)"
    @save="save(explicitTimezone)"
    @discard="discard(explicitTimezone)"
    class="settingsConfigRow"
  >
    <template #view>
      {{
        editedExplicitTimezoneUi + (editedConfig.explicitTimezone ? "" : ` (${detectedTimezone})`)
      }}
    </template>

    <template #edit>
      <select v-model="editedExplicitTimezoneUi">
        <option v-for="timezone in availableExplicitTimezones" :key="timezone">
          {{ timezone }}
        </option>
      </select>
    </template>

  </ConfigRow>
  </div>

  <div>
  <ConfigRow
    label="Foolproof delete delay"
    :editing="editing.foolproofDeleteDelay"
    :changed="hasChanged(foolproofDeleteDelay)"
    @edit="startEditing(foolproofDeleteDelay)"
    @discard="discard(foolproofDeleteDelay)"
    @save="save(foolproofDeleteDelay)"
    class="settingsConfigRow"
  >
    <template #view>
      {{ editedConfig.foolproofDeleteDelay }}
    </template>

    <template #edit>
      <input type="checkbox" v-model="editedConfig.foolproofDeleteDelay">
    </template>
  </ConfigRow>
  </div>

  <div>
  <ConfigRow
    label="Fetch all contacts only manually"
    :editing="editing.fetchAllContactsOnlyManually"
    :changed="hasChanged(fetchAllContactsOnlyManually)"
    @edit="startEditing(fetchAllContactsOnlyManually)"
    @discard="discard(fetchAllContactsOnlyManually)"
    @save="save(fetchAllContactsOnlyManually)"
    class="settingsConfigRow"
  >

    <template #view>
      {{ editedConfig.fetchAllContactsOnlyManually }}
    </template>

    <template #edit>
      <input type="checkbox" v-model="editedConfig.fetchAllContactsOnlyManually">
    </template>

  </ConfigRow>
  </div>

  <div>
  <ConfigRow
    label="Period of fetching all contacts"
    :class="{ 'grayed': shouldGrayOutPeriod }"
    :editing="editing.fetchAllContactsPeriod"
    :changed="hasChanged(fetchAllContactsPeriod)"
    @edit="startEditing(fetchAllContactsPeriod)"
    @discard="discard(fetchAllContactsPeriod)"
    @save="save(fetchAllContactsPeriod)"
    class="settingsConfigRow"
  >
    <template #view>
      {{ editedConfig.fetchAllContactsPeriod }}
    </template>

    <template #edit>
      <input type="text" :value="editedConfig.fetchAllContactsPeriod" @input="onFetchAllContactsPeriodInput">
<!--      <input-->
<!--        type="number"-->
<!--        placeholder="Number of milliseconds"-->
<!--        v-model.number="editedConfig.fetchAllContactsPeriod"-->
<!--        min="1"-->
<!--      >-->
    </template>
  </ConfigRow>
  </div>

  <div>
  <ConfigRow
    label="Memorize timezone during contact creation"
    :editing="editing.memorizeTimezoneDuringContactCreation"
    :changed="hasChanged(memorizeTimezoneDuringContactCreation)"
    @edit="startEditing(memorizeTimezoneDuringContactCreation)"
    @discard="discard(memorizeTimezoneDuringContactCreation)"
    @save="save(memorizeTimezoneDuringContactCreation)"
    class="settingsConfigRow"
  >
    <template #view>
      {{ editedConfig.memorizeTimezoneDuringContactCreation }}
    </template>

    <template #edit>
      <input type="checkbox" v-model="editedConfig.memorizeTimezoneDuringContactCreation">
    </template>
  </ConfigRow>
  </div>

  <div>
  <ConfigRow
    label="Set created icon as default automatically"
    :editing="editing.setCreatedIconAsDefault"
    :changed="hasChanged(setCreatedIconAsDefault)"
    @edit="startEditing(setCreatedIconAsDefault)"
    @discard="discard(setCreatedIconAsDefault)"
    @save="save(setCreatedIconAsDefault)"
    class="settingsConfigRow"
  >

    <template #view>
      {{ editedConfig.setCreatedIconAsDefault }}
    </template>

    <template #edit>
      <input type="checkbox" v-model="editedConfig.setCreatedIconAsDefault">
    </template>

  </ConfigRow>
  </div>

  <div class="withEmptyLineBelow"></div>

  <div v-if="hasAnythingChanged">
    <button class="redButton buttonRightSpace" @click="discardAll()">Discard all</button>
    <button class="greenButton" @click="saveAll()">Save all</button>
  </div>

  <div v-else>
    <button @click="router.push(homePath)">Close</button>
  </div>

</template>

<style scoped>

.greenButton {
  background-color: green;
  color: white;
}

.settingsConfigRow {
  display: grid;
  grid-template-columns: 340px 280px auto;
  align-items: center;
  gap: 0.5rem;
}

</style>
