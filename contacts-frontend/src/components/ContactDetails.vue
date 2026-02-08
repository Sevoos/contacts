<script setup lang="ts">

import type {Contact} from "@/contact/Contact.ts";
import {
  type ComponentPublicInstance,
  computed,
  onMounted,
  reactive,
  ref, toRaw,
  watch
} from "vue";
import {
  acquireIconFullQualityIfNeeded,
  acquireIconPreviewIfNeeded
} from "@/composables/useImageCache.ts";
import {myStore} from "@/store";
import {myGuiStore} from "@/store/GuiStore.ts";
import {
  type ContactKeyName,
  emojiBirthdayNow,
  emojiBirthdaySoon,
  formatBirthdayDate
} from "@/utils/ContactUtils.ts";
import {effectiveTimezone, formatZonedDateTime} from "@/utils/TimezoneUtils.ts";
import {getChannelSrc} from "@/utils/CommunicationChannelUtils.ts";
import {getCommunicationChannelLink} from "@/lib";
import type {CommunicationChannel} from "@/contact/CommunicationChannel.ts";
import {Temporal} from "@js-temporal/polyfill";
import ConfigRow from "@/components/ConfigRow.vue";
import {
  type ContactGeneralPatchKeyName, birthdayDate, category, communicationChannels, defaultIconId,
  displayName, firstName, icons, lastName, patronymicName, timezone
} from "@/contact/ContactGeneralPatch.ts";
import {getTargetFromEvent} from "@/utils/Utils.ts";

const props = defineProps<{
  contact: Contact
  getArrowLeftToDetails: (contactId: bigint) => boolean
  resetArrowLeft: (contactId: bigint) => void
  alive: boolean
}>()

const savedContact = ref(props.contact)

const preventFocusing = computed(() => savedContact.value.icons.length === 1)

const store = myStore()

const isLoadingInitialIcons = ref(true)

const focusedIconId = ref<bigint | null>(savedContact.value.defaultIconId)

const focusedIconUrl = ref<string | null>(null)

function moveFocus(delta: 1 | -1) {
  const currentFocusedIcon = focusedIconId.value
  if (!currentFocusedIcon) {
    return
  }
  const index = savedContact.value.icons.findIndex(iconId => {
    return iconId === currentFocusedIcon
  })
  const next = savedContact.value.icons[index + delta]
  if (!next) {
    return;
  }
  focusedIconId.value = next
}

function onKeyDown(e: KeyboardEvent) {

  if (e.key === "ArrowRight") {
    e.preventDefault()
    moveFocus(1)
  } else if (e.key === "ArrowLeft") {
    e.preventDefault()
    moveFocus(-1)
  }
}

onMounted(async () => {
  await Promise.allSettled(
    props.contact.icons.map(iconId => {
      if (!props.alive) {
        return
      }
      return acquireIconPreviewIfNeeded(iconId)
    })
  )

  const currentFocusedIconId = focusedIconId.value
  if (currentFocusedIconId) {
    await acquireIconFullQualityIfNeeded(currentFocusedIconId)
    await setFocusedIconUrl(currentFocusedIconId)
  }

  isLoadingInitialIcons.value = false
})

const elementRegistry = reactive(new Map<string, HTMLElement>())

const registryReady = computed(() => elementRegistry.size > 0)

function registerElementRef(iconId: bigint, el: Element | ComponentPublicInstance | null) {
  if (el instanceof HTMLElement) {
    elementRegistry.set(iconId.toString(), el)
  }
}

function focusIconIdElement(iconId: bigint) {
  if (preventFocusing.value)
    return
  elementRegistry.get(iconId.toString())?.focus()
}

let firstTimeWatchFocusedIcon = true

watch(
  [focusedIconId, registryReady],
  ([iconId, ready]) => {
    if (firstTimeWatchFocusedIcon) {
      firstTimeWatchFocusedIcon = false
      return
    }
    if (!iconId || !ready) {
      return
    }
    focusIconIdElement(iconId)
  }
)

const loadingFocusedIcon = ref(false)

async function setFocusedIconUrl(iconId: bigint) {
  function setValueToCurrentUrl() {
    focusedIconUrl.value = store.iconIdToFullQualityUrl.get(iconId)!
  }

  if (store.iconIdToFullQualityUrl.get(iconId)) {
    setValueToCurrentUrl()
  } else {
    loadingFocusedIcon.value = true
    await acquireIconFullQualityIfNeeded(iconId)
    loadingFocusedIcon.value = false
    setValueToCurrentUrl()
  }
}

watch(focusedIconId,async (iconId) => {
  if (iconId) {
    await setFocusedIconUrl(iconId)
  }
})

const contactId = computed(() => savedContact.value.id)

watch(
  () => props.getArrowLeftToDetails(contactId.value),
  (newValue, oldValue) => {
    if (!oldValue && newValue) {
      const currentFocusedIcon = focusedIconId.value
      if (currentFocusedIcon && !loadingFocusedIcon.value && !isLoadingInitialIcons.value) {
        focusIconIdElement(currentFocusedIcon)
      }
      props.resetArrowLeft(contactId.value)
    }
  }
)

const guiStore = myGuiStore()

const prefix = 'Icon '

function onLoad(e: Event) {
  const img = e.target as HTMLImageElement
  const iconID = BigInt(img.alt.substring(prefix.length))
  guiStore.iconIdToDimensionSize.set(iconID, img.naturalWidth)
}

const standardDimension = 360

function getDimensionSize(iconId: bigint): number {
  return Math.min(guiStore.iconIdToDimensionSize.get(iconId) ?? standardDimension, standardDimension)
}

const contactBirthdayBeginning = store.contactIdToNextBirthdayInstant.get(contactId.value)
  ?.toZonedDateTimeISO(props.contact.timezone)

function getAlt(communicationChannel: CommunicationChannel): string {
  return communicationChannel.comment ?? communicationChannel.type
}

const isEditingContact = ref(false)

function getDateFromMilliseconds(milliseconds: bigint): string {
  return Temporal.Instant.fromEpochMilliseconds(Number(milliseconds))
    .round('second')
    .toZonedDateTimeISO(effectiveTimezone()).toString({
        timeZoneName: 'never',
        offset: 'never'
      }
    )
}

const proposedPut = reactive(structuredClone(toRaw(savedContact.value)))
const unsavedProposedPut = reactive(structuredClone(toRaw(proposedPut)))

const editing = reactive<Record<ContactGeneralPatchKeyName, boolean>>({
  birthdayDate: false,
  category: false,
  communicationChannels: false,
  defaultIconId: false,
  displayName: false,
  firstName: false,
  icons: false,
  lastName: false,
  patronymicName: false,
  timezone: false,
})

function startEditing(key: ContactGeneralPatchKeyName) {
  editing[key] = true
}

function finishEditing(key: ContactGeneralPatchKeyName) {
  editing[key] = false
}

function save<K extends ContactGeneralPatchKeyName>(key: K) {
  proposedPut[key] = unsavedProposedPut[key]
  finishEditing(key)
}

function discard<K extends ContactGeneralPatchKeyName>(key: K) {
  unsavedProposedPut[key] = proposedPut[key]
  finishEditing(key)
}

const error = reactive<Record<ContactGeneralPatchKeyName, string | null>>({
  birthdayDate: null,
  category: null,
  communicationChannels: null,
  defaultIconId: null,
  displayName: null,
  firstName: null,
  icons: null,
  lastName: null,
  patronymicName: null,
  timezone: null,
})

function hasChanged<K extends ContactGeneralPatchKeyName>(key: K): boolean {
  return proposedPut[key] !== savedContact.value[key]
}

function onNullableStringInput(key: K, e: InputEvent) {
  const input = getTargetFromEvent(e).value
  unsavedProposedPut[key] = input ? input : null
}

</script>

<template>
  <div v-if="isLoadingInitialIcons">
    <a>Loading...</a>
  </div>
  <div v-else-if="isEditingContact">

    <ConfigRow
      label="Display name"
      :editing="editing.displayName"
      :changed="hasChanged(displayName)"
      :error="error.displayName"
      @edit="startEditing(displayName)"
      @save="save(displayName)"
      @discard="discard(displayName)"
    >
      <template #view>
        {{ unsavedProposedPut.displayName }}
      </template>
      <template #edit>
        <input type="text" v-model="proposedPut.displayName">
      </template>
    </ConfigRow>

  </div>
  <div v-else>
    <span
      v-for="iconId in contact.icons"
      v-if="!preventFocusing"
      :key="iconId.toString()"
      class="icon-preview"
      @keydown="onKeyDown"
      :tabindex="0"
      :ref="el => registerElementRef(iconId, el)"
    >
      <img
        :src="store.iconIdToPreviewUrl.get(iconId)"
        :alt="'Icon preview ' + iconId.toString()"
        @click="focusedIconId = iconId"
      >
    </span>
    <div
      v-if="focusedIconId"
    >
      <div
        v-if="loadingFocusedIcon"
      >
        <a>Loading icon...</a>
      </div>
      <div
        v-else
      >
        <img
          @load="onLoad"
          :width="getDimensionSize(focusedIconId)"
          :height="getDimensionSize(focusedIconId)"
          :src="focusedIconUrl ?? undefined"
          :alt="prefix + focusedIconId.toString()"
        >
      </div>
    </div>

    <div>
      Category: {{contact.category}}
    </div>

    <div v-if="contact.nameToActuallyDisplay !== contact.firstName">
      Displayed name: {{ contact.nameToActuallyDisplay }}
    </div>

    <div>
      First name: {{contact.firstName}}
    </div>

    <div>
      Last name: {{contact.lastName}}
    </div>

    <div v-if="contact.patronymicName">
      Patronymic name: {{contact.patronymicName}}
    </div>

    <div v-if="contact.birthdayDate">
      Birthday date: {{formatBirthdayDate(contact.birthdayDate)}}
    </div>

    <div v-if="contact.birthdayDate && contactBirthdayBeginning">
      The next birthday: {{
        formatZonedDateTime(contactBirthdayBeginning.withTimeZone(effectiveTimezone()))
        + (
          contact.birthdayDate.year === null || contact.birthdayDate.year === undefined ?
            ""
            : ` (turning ${contactBirthdayBeginning.year - contact.birthdayDate.year})`
        )
        + (
          store.contactsWithBirthdayNow.has(contactId)
            ? emojiBirthdayNow
            : store.contactsWithBirthdaySoon.has(contactId)
              ? emojiBirthdaySoon
              : ""
        )
      }}
    </div>

    <div>
      Timezone: {{contact.timezone}}
    </div>

    <div>
      Created at: {{ getDateFromMilliseconds(contact.creationDate) }}
    </div>

    <div>
      Modified at: {{ getDateFromMilliseconds(contact.modificationDate) }}
    </div>

    <div v-for="communicationChannel in contact.communicationChannels" class="imageFollowedByCenteredText gap">
      <img
        :alt="getAlt(communicationChannel)"
        :src="getChannelSrc(communicationChannel)"
        width="72"
      >
      <a
        :href="getCommunicationChannelLink(communicationChannel.type, communicationChannel.value)"
        target="_blank"
        class="hyperlink"
      >
        {{ communicationChannel.comment ??
            (
              communicationChannel.type === "Discord" ?
                communicationChannel.value : communicationChannel.type
            )
        }}
      </a>
    </div>

    <div class="withEmptyLineBelow"></div>

    <div>
      <button @click="isEditingContact = true">Edit</button>
    </div>

  </div>
</template>

<style scoped>

.icon-preview {
  margin-right: 16px;
  display: inline-block;
  padding: 10px;
  border-radius: 16px;
}

.icon-preview:focus {
  background: #eef;
}

.hyperlink {
  color: #551A8B;
}

</style>
