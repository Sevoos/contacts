<script setup lang="ts">

import type {Contact} from "@/contact/Contact.ts";
import {
  type ComponentPublicInstance,
  computed, onBeforeUnmount,
  onMounted,
  type Reactive,
  reactive,
  ref,
  watch
} from "vue";
import {
  acquireIconFullQualityIfNeeded,
  acquireIconPreviewIfNeeded
} from "@/composables/useImageCache.ts";
import {myStore} from "@/store";
import {myGuiStore} from "@/store/GuiStore.ts";
import {
  deleteContactIfSaved,
  emojiBirthdayNow,
  emojiBirthdaySoon,
  findContactById,
  formatBirthdayDate, simplifyOtherContactsDisplayedNames
} from "@/utils/ContactUtils.ts";
import {effectiveTimezone, formatZonedDateTime} from "@/utils/TimezoneUtils.ts";
import {getChannelSrc} from "@/utils/CommunicationChannelUtils.ts";
import {
  CommunicationChannelCreationDto,
  CommunicationChannelPatchDtoJs, ContactPatchDtoJs,
  EnumCommunicationChannel,
  getCommunicationChannelLink, patchCommunicationChannel, patchContact, removeAtIfNeeded
} from "@/lib";
import {
  CommunicationChannel,
  communicationChannelTypes,
  performErrorCheck
} from "@/contact/CommunicationChannel.ts";
import {Temporal} from "@js-temporal/polyfill";
import ConfigRow from "@/components/ConfigRow.vue";
import {
  doObjectsStringsEqual,
  getFile,
  isValidImage, removeAllSpaces,
  sanitizeNonNegativeNumber,
  sanitizeSpaces
} from "@/utils/Utils.ts";
import {ConfigItemPatch} from "@/utils/ConfigItemPatch.ts";
import {
  birthdayDateCreationFromDto, birthdayDateCreationToDto,
  emptyBirthdayDateCreation,
  getBirthdayDateCreationErrorCheck,
  isBirthdayCreationEmpty
} from "@/contact/BirthdayDateCreation.ts";
import {BirthdayDatePatch, doBirthdaysEqual} from "@/contact/BirthdayDatePatch.ts";
import {timeZonesNames} from "@vvo/tzdb";
import {InputArray} from "@/utils/InputArray.ts";
import {Icon} from "@/contact/Icon.ts";
import {getFileBytes} from "@/utils/IconhandlingApiUtils.ts";
import {
  birthdayDate, category, communicationChannels,
  type ContactGeneralPatchKey, contactGeneralPatchKeys, type ContactPatchKey,
  contactPatchKeys, defaultIconId, displayName, firstName, icons, lastName, patronymicName, timezone
} from "@/contact/ContactGeneralPatch.ts";
import {pullContactDto} from "@/utils/ContactApiUtils.ts";
import YesNoDialog from "@/components/YesNoDialog.vue";
import {readConfig} from "@/config/settings.ts";

const props = defineProps<{
  contact: Contact
  getArrowLeftToDetails: (contactId: bigint) => boolean
  alive: boolean
  moveFocus: (delta: 1 | -1) => void
}>()

const savedContact = ref(props.contact)

const preventFocusing = computed(() => savedContact.value.icons.length === 1)

const store = myStore()

const isLoadingInitialIcons = ref(true)

const focusedIconId = ref<bigint | null>(savedContact.value.defaultIconId)

const focusedIconUrl = ref<string | null>(null)

function focusIconIdElement(iconId: bigint) {
  elementRegistry.get(iconId.toString())?.focus()
}

function focusCurrentIcon() {
  const currentFocusedIcon = focusedIconId.value
  if (currentFocusedIcon && !loadingFocusedIcon.value && !isLoadingInitialIcons.value) {
    focusIconIdElement(currentFocusedIcon)
  }
}

function replaceContact(contact: Contact) {
  store.hasContactJustBeenReplaced = true
  const index = store.contacts.findIndex(value => value.id === contact.id)
  store.contacts[index] = contact
  if (!preventFocusing.value) {
    focusCurrentIcon()
  }
  // contactIdToArrowLeftToDetails.set(contact.id, true)
}

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
  focusIcon(next)
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

async function startLoadingInitialIcons(contact: Contact) {
  isLoadingInitialIcons.value = true
  await Promise.allSettled(
    contact.icons.map(iconId => {
      if (!props.alive) {
        return
      }
      return acquireIconPreviewIfNeeded(iconId)
    })
  )
  const currentFocusedIconId = focusedIconId.value
  if (currentFocusedIconId) {
    await acquireIconFullQualityIfNeeded(currentFocusedIconId)
  }
  isLoadingInitialIcons.value = false
}

onMounted(async () => {
  const contact = savedContact.value
  await startLoadingInitialIcons(contact)
  if (contact.defaultIconId) {
    await focusIcon(contact.defaultIconId)
  }
})

const elementRegistry = reactive(new Map<string, HTMLElement>())

const registryReady = computed(() => elementRegistry.size > 0)

function registerElementRef(iconId: bigint, el: Element | ComponentPublicInstance | null) {
  if (el instanceof HTMLElement) {
    elementRegistry.set(iconId.toString(), el)
  }
}

let firstTimeWatchFocusedIcon = true

watch(
  [focusedIconId, registryReady],
  ([iconId, ready]) => {
    if (!iconId || !ready) {
      return
    }
    if (firstTimeWatchFocusedIcon) {
      firstTimeWatchFocusedIcon = false
      return
    }
    focusIconIdElement(iconId)
  }
)

const loadingFocusedIcon = ref(false)

async function focusIcon(iconId: bigint) {
  if (focusedIconId.value === iconId) {
    focusedIconId.value = null
  }
  focusedIconId.value = iconId
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

const contactId = props.contact.id

watch(
  () => props.getArrowLeftToDetails(contactId),
  (newValue, oldValue) => {
    if (!oldValue && newValue) {
      focusCurrentIcon()
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

const contactBirthdayBeginning = computed(() => store.contactIdToNextBirthdayInstant.get(contactId)
  ?.toZonedDateTimeISO(savedContact.value.timezone))

function getAlt(communicationChannel: CommunicationChannel): string {
  return communicationChannel.comment ?? communicationChannel.type
}

const isEditingContact = ref(false)

// 0: not opened
// 1: opened
// 2: already deleting
const deleteDialogStage = ref(0)

function deleteContact(id: bigint) {
  simplifyOtherContactsDisplayedNames(savedContact.value)
  store.contactApiService.deleteContact(id).then(response => {
    if (response.status === 200) {
      deleteContactIfSaved(id)
      props.moveFocus(-1)
      store.contacts.splice(store.contacts.findIndex(contact => contact.id === id), 1)
    }
  })
}

function confirmDelete() {
  deleteDialogStage.value = 2
  deleteContact(contactId)
}

function cancelDeleting() {
  deleteDialogStage.value = 0
}

function isAlreadyDeleting(): boolean {
  return deleteDialogStage.value === 2
}

const delay = readConfig().foolproofDeleteDelay ? 3000 : 0

function stopEditingContact() {
  isEditingContact.value = false
  deleteDialogStage.value = 0
}

function getDateFromMilliseconds(milliseconds: bigint): string {
  return Temporal.Instant.fromEpochMilliseconds(Number(milliseconds))
    .round('second')
    .toZonedDateTimeISO(effectiveTimezone()).toString({
        timeZoneName: 'never',
        offset: 'never'
      }
    )
}

const birthdayDateAlreadyPresent = computed(() => savedContact.value.birthdayDate !== null)

const generalKeyToSavedValueGetter: Record<ContactGeneralPatchKey, () => any> = {
  birthdayDate: () => new BirthdayDatePatch(
    birthdayDateAlreadyPresent.value,
    birthdayDateAlreadyPresent.value ?
      birthdayDateCreationFromDto(savedContact.value.birthdayDate!) :
      emptyBirthdayDateCreation()
  ),
  category: () => new InputArray(
    () => "",
    savedContact.value.category.slice(),
    (categoryLevel: string) => !!(categoryLevel),
    () => null,
    (value: string) => sanitizeSpaces(value)
  ),
  communicationChannels: () => new InputArray<CommunicationChannel>(
    () => {
      let idNumber = -1
      while (negativeComChannelsIds.has(idNumber)) {
        idNumber--
      }
      return new CommunicationChannel(BigInt(idNumber), "", "", "")
    },
    savedContact.value.communicationChannels.slice(),
    comChan => !!(comChan.type && comChan.value),
    comChan => performErrorCheck(comChan.type, comChan.value),
    comChan => {
      const comment = comChan.comment === null ? "" : sanitizeSpaces(comChan.comment)
      return new CommunicationChannel(
        comChan.id,
        comment ? comment : null,
        comChan.type,
        removeAtIfNeeded(removeAllSpaces(comChan.value))
      )
    },
    (a, b) => {
      return doObjectsStringsEqual(a, b)
    }
  ),
  defaultIconId: () => savedContact.value.defaultIconId,
  displayName: () => savedContact.value.displayName ?? "",
  firstName: () => savedContact.value.firstName,
  icons: () => savedContact.value.icons.map((id: bigint) => new Icon(id, store.iconIdToPreviewUrl.get(id)!)),
  lastName: () => savedContact.value.lastName,
  patronymicName: () => savedContact.value.patronymicName ?? "",
  timezone: () => savedContact.value.timezone
}

const iconsPatch = reactive(
  new ConfigItemPatch<Icon[]>(
    () => true,
    generalKeyToSavedValueGetter[icons](),
    null,
    value => value.length === 0,
    value => value.slice(),
    () => null,
    (a, b) => a.length === b.length && a.every((value, index) => value.id === b[index]!.id)
  )
)

onBeforeUnmount(() => {
  iconsPatch.editedPatch.filter(it => it.id < 0).forEach(it => URL.revokeObjectURL(it.url))
 }
)

const iconInput = ref<HTMLInputElement | null>(null)

const iconInputError = ref<string | null>(null)

const defaultIconPatch = reactive(
  new ConfigItemPatch<bigint | null>(
    () => true,
    generalKeyToSavedValueGetter[defaultIconId](),
    null,
    value => value === null
  )
)

const negativeIconsIds = new Set<number>()

const negativeIdToIconBytes = new Map<bigint, Int8Array>

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = getFile(input)
  if (!file) {
    return
  }
  iconInputError.value = null

  isValidImage(file).then(isImage => {
    if (!isImage) {
      iconInputError.value = "If the file is selected, it has to be an image"
      return
    }
    let idNumber = -1
    while (negativeIconsIds.has(idNumber)) {
      idNumber--
    }
    negativeIconsIds.add(idNumber)
    const id = BigInt(idNumber)
    iconsPatch.editedPatch.push(new Icon(
      id,
      URL.createObjectURL(file)
    ))
    if (iconsPatch.editedPatch.length === 1) {
      defaultIconPatch.editedPatch = id
    }
    getFileBytes(file).then(fileBytes => negativeIdToIconBytes.set(id, fileBytes))
  })
}

function deleteIcon(id: bigint) {
  const index = iconsPatch.editedPatch.findIndex(value => value.id === id)
  if (index === -1) return
  const icon = iconsPatch.editedPatch[index]!
  if (id < 0) {
    URL.revokeObjectURL(icon.url)
  }
  iconsPatch.editedPatch.splice(index, 1)
  if (defaultIconPatch.editedPatch === id) {
    defaultIconPatch.editedPatch = iconsPatch.editedPatch.length === 0 ?
      null :
      iconsPatch.editedPatch[(index === 0 ? 0 : index - 1)]!.id
  }
}

const categoryPatch = reactive(
  new ConfigItemPatch<InputArray<string>>(
    () => true,
    generalKeyToSavedValueGetter[category](),
    null,
    value => value.array.length === 0,
    value => value.copy(),
    () => null,
    (a, b) => a.array.length === b.array.length && a.array.every((value, index) => value.savedValue === b.array[index]!.savedValue)
  )
)

function deleteErrorFromCategoryItem(index: number) {
  categoryPatch.editedPatch.getItem(index).error = null
}

const displayNamePatch = reactive(
  new ConfigItemPatch<string>(
    () => true,
    generalKeyToSavedValueGetter[displayName](),
    (value: string) => sanitizeSpaces(value)
  )
)

const firstNamePatch = reactive(
  new ConfigItemPatch<string>(
    () => false,
    generalKeyToSavedValueGetter[firstName](),
    (value: string) => sanitizeSpaces(value)
  )
)

const lastNamePatch = reactive(
  new ConfigItemPatch<string>(
    () => false,
    generalKeyToSavedValueGetter[lastName](),
    (value: string) => sanitizeSpaces(value)
  )
)

const patronymicNamePatch = reactive(
  new ConfigItemPatch<string>(
    () => true,
    generalKeyToSavedValueGetter[patronymicName](),
    (value: string) => sanitizeSpaces(value)
  )
)

const birthdayDatePatch = reactive(
  new ConfigItemPatch<BirthdayDatePatch>(
    (editedValue) => !editedValue.include,
    generalKeyToSavedValueGetter[birthdayDate](),
    null,
    (value) => !value.include || isBirthdayCreationEmpty(value.date),
    (value) => value.copy(),
    (value) => getBirthdayDateCreationErrorCheck(value.date, timezonePatch.currentPatch)(),
    doBirthdaysEqual
  )
)

function onYearInput(e: InputEvent) {
  birthdayDatePatch.removeError()
  sanitizeNonNegativeNumber(e, (numberUnlessEmpty: number | null) => {
    birthdayDatePatch.editedPatch.date.year = numberUnlessEmpty
  })
}

function onMonthInput(e: InputEvent) {
  birthdayDatePatch.removeError()
  sanitizeNonNegativeNumber(e, (numberUnlessEmpty: number | null) => {
    birthdayDatePatch.editedPatch.date.month = numberUnlessEmpty
  })
}

function onDayInput(e: InputEvent) {
  birthdayDatePatch.removeError()
  sanitizeNonNegativeNumber(e, (numberUnlessEmpty: number | null) => {
    birthdayDatePatch.editedPatch.date.day = numberUnlessEmpty
  })
}

const timezonePatch = reactive(
  new ConfigItemPatch<string>(() => false, generalKeyToSavedValueGetter[timezone]())
)

function revertDefaultIcon() {
  defaultIconPatch.revert()
  if (defaultIconPatch.editedPatch !== null) {
    if (!iconsPatch.editedPatch.find(icon => icon.id === defaultIconPatch.editedPatch)) {
      revertIconsNoCheck()
    }
  }
}

function revertIconsNoCheck() {
  iconInputError.value = null
  iconsPatch.revert()
}

function revertIcons() {
  revertIconsNoCheck()
  if (defaultIconPatch.editedPatch !== null) {
    if (!iconsPatch.editedPatch.find(icon => icon.id === defaultIconPatch.editedPatch)) {
      defaultIconPatch.revert()
    }
  }
}

const negativeComChannelsIds = new Set<number>

const comChannelsPatch = reactive(new ConfigItemPatch<InputArray<CommunicationChannel>>(
    value => value.array.length === 0,
    generalKeyToSavedValueGetter[communicationChannels](),
    null,
    value => value.array.length === 0,
    value => value.copy(),
    () => null,
    (a, b) => a.array.length === b.array.length && a.array.every((value, index) =>
      doObjectsStringsEqual(value.savedValue, b.array[index]!.savedValue)
    )
  )
)

const generalKeyToPatch: Record<ContactGeneralPatchKey, Reactive<ConfigItemPatch<any>>> = {
  birthdayDate: birthdayDatePatch,
  category: categoryPatch,
  communicationChannels: comChannelsPatch,
  defaultIconId: defaultIconPatch,
  displayName: displayNamePatch,
  firstName: firstNamePatch,
  icons: iconsPatch,
  lastName: lastNamePatch,
  patronymicName: patronymicNamePatch,
  timezone: timezonePatch
}

function forEachPatch(consumer: (patch: Reactive<ConfigItemPatch<any>>) => void) {
  contactGeneralPatchKeys.forEach(key => {
    consumer(generalKeyToPatch[key])
  })
}

function updateInitialValue(key: ContactGeneralPatchKey) {
  generalKeyToPatch[key].initialValue = generalKeyToSavedValueGetter[key]()
  generalKeyToPatch[key].currentPatch = generalKeyToSavedValueGetter[key]()
  generalKeyToPatch[key].editedPatch = generalKeyToSavedValueGetter[key]()
}

function deleteErrorFromComChanItem(index: number) {
  comChannelsPatch.editedPatch.getItem(index).error = null
}

const hasContactPatchKeyChanged = computed<Record<ContactPatchKey, boolean>>(() => {
  return {
    birthdayDate: birthdayDatePatch.isDifferentFromInitialValue(),
    category: categoryPatch.hasEditedPatchChanged(),
    defaultIconId: defaultIconPatch.hasEditedPatchChanged(),
    displayName: displayNamePatch.isDifferentFromInitialValue(),
    firstName: firstNamePatch.isDifferentFromInitialValue(),
    lastName: lastNamePatch.isDifferentFromInitialValue(),
    patronymicName: patronymicNamePatch.isDifferentFromInitialValue(),
    timezone: timezonePatch.isDifferentFromInitialValue()
  }
})

const hasContactPatchChanged = computed<boolean>(() => contactPatchKeys.some(key => hasContactPatchKeyChanged.value[key]))

const hasComChannelsPatchChanged = computed<boolean>(() => comChannelsPatch.hasEditedPatchChanged())
const hasIconsPatchChanged = computed<boolean>(() => iconsPatch.hasEditedPatchChanged())

function revertAll() {
  forEachPatch(patch => patch.revert())
}

const hasAnythingChanged = computed<boolean>(
  () => hasContactPatchChanged.value || hasComChannelsPatchChanged.value || hasIconsPatchChanged.value
)

const isSaving = ref(false)

function saveChanges() {
  isSaving.value = true
  const iconPromises: Promise<any>[] = []
  if (iconsPatch.hasEditedPatchChanged()) {
    const savedIconsIds = savedContact.value.icons
    const editedIcons = iconsPatch.editedPatch
    const editedIconsIds = new Set(editedIcons.map(icon => icon.id))
    editedIcons.forEach(value => {
      const id = value.id
      if (id < 0) {
        iconPromises.push(
          store.iconHandlingApiService.uploadIcon(
            contactId,
            "icon.png",
            negativeIdToIconBytes.get(id)!
          ).then(response => {
            if (defaultIconPatch.editedPatch === id) {
              defaultIconPatch.editedPatch = response.body!
            }
          })
        )
      }
    })
    savedIconsIds.forEach(id => {
      if (!editedIconsIds.has(id)) {
        iconPromises.push(
          store.iconApiService.deleteIconEntity(id)
        )
      }
    })
  }
  Promise.allSettled(iconPromises).then(() => {
    const promises: Promise<any>[] = []
    if (comChannelsPatch.hasEditedPatchChanged()) {
      const saved = savedContact.value.communicationChannels
      const edited = comChannelsPatch.editedPatch.getOnlySavedValues()
      const editedIds = new Set(edited.map(value => value.id))
      const savedCompare: CommunicationChannel[] = []
      saved.forEach(value => {
        if (editedIds.has(value.id)) {
          savedCompare.push(value)
        } else {
          promises.push(store.communicationChannelApiService.deleteCommunicationChannel(value.id))
        }
      })
      const editedCompare: CommunicationChannel[] = []
      edited.forEach(value => {
        if (value.id < 0) {
          promises.push(
            store.communicationChannelApiService.createCommunicationChannel(
              new CommunicationChannelCreationDto(
                value.comment,
                EnumCommunicationChannel.valueOf(value.type),
                contactId,
                value.value
              )
            )
          )
        } else {
          editedCompare.push(value)
        }
      })
      savedCompare.forEach((savedValue, index) => {
        const editedValue = editedCompare[index]!
        if (!doObjectsStringsEqual(savedValue, editedValue)) {
          const comChannelPatch = new CommunicationChannelPatchDtoJs()
          if (savedValue.type !== editedValue.type) {
            comChannelPatch.setType(EnumCommunicationChannel.valueOf(editedValue.type))
          }
          if (savedValue.value !== editedValue.value) {
            comChannelPatch.setValue(editedValue.value)
          }
          const editedComment = editedValue.comment ? editedValue.comment : null
          if (savedValue.comment !== editedComment) {
            comChannelPatch.setComment(editedComment)
          }
          promises.push(
            patchCommunicationChannel(savedValue.id, comChannelPatch, store.communicationChannelApiService)
          )
        }
      })
    }

    const contactPatch = new ContactPatchDtoJs()
    let contactPatchChanged = false

    if (birthdayDatePatch.isDifferentFromInitialValue()) {
      contactPatchChanged = true
      contactPatch.setBirthdayDate(
        birthdayDatePatch.currentPatch.include ?
          birthdayDateCreationToDto(birthdayDatePatch.currentPatch.date) :
          null
      )
    }
    if (categoryPatch.hasEditedPatchChanged()) {
      contactPatchChanged = true
      contactPatch.setCategory(categoryPatch.editedPatch.getOnlyEditedValue())
    }
    if (defaultIconPatch.hasEditedPatchChanged()) {
      contactPatchChanged = true
      contactPatch.setDefaultIconId(defaultIconPatch.editedPatch)
    }
    if (displayNamePatch.isDifferentFromInitialValue()) {
      contactPatchChanged = true
      contactPatch.setDisplayName(displayNamePatch.currentPatch ? displayNamePatch.currentPatch : null)
    }
    if (firstNamePatch.isDifferentFromInitialValue()) {
      contactPatchChanged = true
      contactPatch.setFirstName(firstNamePatch.currentPatch)
    }
    if (lastNamePatch.isDifferentFromInitialValue()) {
      contactPatchChanged = true
      contactPatch.setLastName(lastNamePatch.currentPatch)
    }
    if (patronymicNamePatch.isDifferentFromInitialValue()) {
      contactPatchChanged = true
      contactPatch.setPatronymicName(patronymicNamePatch.currentPatch ? patronymicNamePatch.currentPatch : null)
    }
    if (timezonePatch.isDifferentFromInitialValue()) {
      contactPatchChanged = true
      contactPatch.setTimezone(timezonePatch.currentPatch)
    }

    if (contactPatchChanged) {
      promises.push(
        patchContact(contactId, contactPatch, store.contactApiService)
      )
    }

    Promise.allSettled(promises).then(() => {
      simplifyOtherContactsDisplayedNames(savedContact.value)
      pullContactDto(contactId).then(() => {
        const newContact = findContactById(contactId)!
        savedContact.value = newContact
        isEditingContact.value = false
        firstTimeWatchFocusedIcon = true
        startLoadingInitialIcons(newContact).then(() => {
          elementRegistry.clear()
          function proceed() {
            replaceContact(newContact)
          }
          if (newContact.defaultIconId) {
            focusIcon(newContact.defaultIconId).then(() => proceed())
          } else {
            proceed()
          }
        })
        contactGeneralPatchKeys.forEach(key => updateInitialValue(key))
        isSaving.value = false
      })
    })
  })
}

</script>

<template>
  <div v-if="isLoadingInitialIcons">
    <a>Loading...</a>
  </div>
  <div v-else-if="isEditingContact">
    <div class="icon-row" v-if="!iconsPatch.isEditedEmpty()">
      <span></span>
      <span>
        Default icon:
        <a
          class="clickable"
          title="Revert default icon"
          v-if="defaultIconPatch.hasEditedPatchChanged() && defaultIconPatch.currentPatch !== null"
          @click="revertDefaultIcon()"
        >↩️</a>
      </span>
    </div>
    <div
      v-for="icon in iconsPatch.editedPatch"
      :key="String(icon.id)"
      class="icon-row"
    >
      <span>
        <img
          :alt="`Preview ${icon.id}`"
          :src="icon.id > 0 ? store.iconIdToPreviewUrl.get(icon.id) : icon.url"
          class="preview"
        >
      </span>
      <span>
        <input type="radio" :value="icon.id" v-model="defaultIconPatch.editedPatch">
      </span>
      <span>
        <a @click="deleteIcon(icon.id)" class="clickable">🗑️</a>
      </span>
    </div>
    <div class="withEmptyLineBelow">
      <input
        ref="iconInput"
        type="file"
        accept="image/*"
        @change="onFileChange"
        style="display: none"
      />
      <button @click="iconInput?.click()">Add new icon</button>
      <span class="error">{{ iconInputError }}</span>
      <a
        class="clickable"
        title="Revert icons"
        v-if="iconsPatch.hasEditedPatchChanged()"
        @click="revertIcons()"
      >↩️</a>
    </div>
    <div>
      Category:
      <a
        class="clickable"
        title="Revert changes"
        v-if="categoryPatch.hasEditedPatchChanged()"
        @click="categoryPatch.revert()"
      >↩️</a>
    </div>
    <div
      v-for="(item, index) in categoryPatch.editedPatch.array"
      :key="item.savedValue"
    >
      <ConfigRow
        :label="String(index + 1)"
        postLabelChar="."
        :editing="item.isEditing"
        :changed="categoryPatch.editedPatch.hasItemChanged(index)"
        @discard="categoryPatch.editedPatch.discardEditedItem(index)"
        @edit="categoryPatch.editedPatch.startEditing(index)"
        @save="categoryPatch.editedPatch.saveItem(index)"
        :error="categoryPatch.editedPatch.getItem(index).error"
      >
        <template #view>
          {{ item.savedValue }}
        </template>
        <template #edit>
          <input
            type="text"
            v-model="categoryPatch.editedPatch.getItem(index).editedValue"
            @input="deleteErrorFromCategoryItem(index)"
          >
        </template>
      </ConfigRow>
      <a @click="categoryPatch.editedPatch.deleteItem(index)" v-if="!item.isEditing" class="clickable">🗑️</a>
    </div>
    <div>
      Name:
      <input type="text" v-model="categoryPatch.editedPatch.currentInput">
      <button
        @click="categoryPatch.editedPatch.addCurrentInputToArray"
        :class="{ grayed: !categoryPatch.editedPatch.canAddNewItem() }"
      >Add category level</button>
    </div>
    <div class="withEmptyLineBelow">

    </div>
    <div>
      <ConfigRow
        label="Display name"
        :editing="displayNamePatch.isEditing"
        :changed="displayNamePatch.hasEditedPatchChanged()"
        :error="displayNamePatch.error"
        @discard="displayNamePatch.discard()"
        @edit="displayNamePatch.startEditing()"
        @save="displayNamePatch.save()"
      >
        <template #view>
          <span v-if="displayNamePatch.getCurrentPatchUnlessEmpty()">
            {{ displayNamePatch.getCurrentPatchUnlessEmpty() }}
          </span>
          <span v-else class="value--null">
            empty
          </span>
        </template>
        <template #edit>
          <input type="text" v-model="displayNamePatch.editedPatch" @input="displayNamePatch.removeError()">
        </template>
      </ConfigRow>
      <a
        class="clickable"
        title="Revert changes"
        v-if="displayNamePatch.isDifferentFromInitialValue() && !displayNamePatch.isEditing"
        @click="displayNamePatch.revert()"
      >↩️</a>
    </div>

    <div>
      <ConfigRow
        label="First name"
        :editing="firstNamePatch.isEditing"
        :changed="firstNamePatch.hasEditedPatchChanged()"
        :error="firstNamePatch.error"
        @discard="firstNamePatch.discard()"
        @edit="firstNamePatch.startEditing()"
        @save="firstNamePatch.save()"
      >
        <template #view>
          {{ firstNamePatch.getCurrentPatchUnlessEmpty() }}
        </template>
        <template #edit>
          <input type="text" v-model="firstNamePatch.editedPatch" @input="firstNamePatch.removeError()">
        </template>
      </ConfigRow>
      <a
        class="clickable"
        title="Revert changes"
        v-if="firstNamePatch.isDifferentFromInitialValue() && !firstNamePatch.isEditing"
        @click="firstNamePatch.revert()"
      >↩️</a>
    </div>

    <div>
      <ConfigRow
        label="Last name"
        :editing="lastNamePatch.isEditing"
        :changed="lastNamePatch.hasEditedPatchChanged()"
        :error="lastNamePatch.error"
        @discard="lastNamePatch.discard()"
        @edit="lastNamePatch.startEditing()"
        @save="lastNamePatch.save()"
      >
        <template #view>
          {{ lastNamePatch.getCurrentPatchUnlessEmpty() }}
        </template>
        <template #edit>
          <input type="text" v-model="lastNamePatch.editedPatch" @input="lastNamePatch.removeError()">
        </template>
      </ConfigRow>
      <a
        class="clickable"
        title="Revert changes"
        v-if="lastNamePatch.isDifferentFromInitialValue() && !lastNamePatch.isEditing"
        @click="lastNamePatch.revert()"
      >↩️</a>
    </div>

    <div class="withEmptyLineBelow">
      <ConfigRow
        label="Patronymic name"
        :editing="patronymicNamePatch.isEditing"
        :changed="patronymicNamePatch.hasEditedPatchChanged()"
        :error="patronymicNamePatch.error"
        @discard="patronymicNamePatch.discard()"
        @edit="patronymicNamePatch.startEditing()"
        @save="patronymicNamePatch.save()"
      >
        <template #view>
          <span v-if="patronymicNamePatch.getCurrentPatchUnlessEmpty()">
            {{ patronymicNamePatch.getCurrentPatchUnlessEmpty() }}
          </span>
          <span v-else class="value--null">
            empty
          </span>
        </template>
        <template #edit>
          <input type="text" v-model="patronymicNamePatch.editedPatch" @input="patronymicNamePatch.removeError()">
        </template>
      </ConfigRow>
      <a
        class="clickable"
        title="Revert changes"
        v-if="patronymicNamePatch.isDifferentFromInitialValue() && !patronymicNamePatch.isEditing"
        @click="patronymicNamePatch.revert()"
      >↩️</a>
    </div>

    <div :class="{ grayed: !birthdayDatePatch.isEditing }">
      Include birthday date:
      <input type="checkbox" v-model="birthdayDatePatch.editedPatch.include">
    </div>
    <div class="withEmptyLineBelow">
      <ConfigRow
        label="Birthday date"
        :editing="birthdayDatePatch.isEditing"
        :changed="birthdayDatePatch.hasEditedPatchChanged()"
        :error="birthdayDatePatch.error"
        @discard="birthdayDatePatch.discard()"
        @edit="birthdayDatePatch.startEditing()"
        @save="birthdayDatePatch.save()"
      >
        <template #view>
          <div v-if="!birthdayDatePatch.currentPatch.include" class="value--null">empty</div>
          <div v-else>
            <div v-if="birthdayDatePatch.currentPatch.date.year !== null">Year: {{  birthdayDatePatch.currentPatch.date.year}}</div>
            <div>Month: {{  birthdayDatePatch.currentPatch.date.month}}</div>
            <div>Day: {{  birthdayDatePatch.currentPatch.date.day}}</div>
          </div>
        </template>
        <template #edit>
          <div :class="{ grayed: !birthdayDatePatch.editedPatch.include }">
            <div>
              Year:
              <input type="text" placeholder="(optional)" :value="birthdayDatePatch.editedPatch.date.year" @input="onYearInput">
            </div>
            <div>
              Month:
              <input type="text" :value="birthdayDatePatch.editedPatch.date.month" @input="onMonthInput">
            </div>
            <div>
              Day:
              <input type="text" :value="birthdayDatePatch.editedPatch.date.day" @input="onDayInput">
            </div>
          </div>
        </template>
      </ConfigRow>
      <a
        class="clickable"
        title="Revert changes"
        v-if="birthdayDatePatch.isDifferentFromInitialValue() && !birthdayDatePatch.isEditing"
        @click="birthdayDatePatch.revert()"
      >↩️</a>
    </div>
    <div class="withEmptyLineBelow">
      <ConfigRow
        label="Timezone"
        :editing="timezonePatch.isEditing"
        :changed="timezonePatch.hasEditedPatchChanged()"
        @discard="timezonePatch.discard()"
        @edit="timezonePatch.startEditing()"
        @save="timezonePatch.save()"
      >
        <template #view>
          {{ timezonePatch.currentPatch }}
        </template>
        <template #edit>
          <select v-model="timezonePatch.editedPatch">
            <option v-for="timezone in timeZonesNames" :key="timezone">{{ timezone }}</option>
          </select>
        </template>
      </ConfigRow>
      <a
        class="clickable"
        title="Revert changes"
        v-if="timezonePatch.isDifferentFromInitialValue() && !timezonePatch.isEditing"
        @click="timezonePatch.revert()"
      >↩️</a>
    </div>

    <div v-if="comChannelsPatch.editedPatch.array.length > 0">
      Communication channels:
      <a
        class="clickable"
        title="Revert changes"
        v-if="comChannelsPatch.hasEditedPatchChanged()"
        @click="comChannelsPatch.revert()"
      >↩️</a>
    </div>

    <div v-for="(item, index) in comChannelsPatch.editedPatch.array" :key="index">
      <ConfigRow
        :label="String(index + 1)"
        postLabelChar="."
        :editing="item.isEditing"
        :changed="comChannelsPatch.editedPatch.hasItemChanged(index)"
        @edit="comChannelsPatch.editedPatch.startEditing(index)"
        @discard="comChannelsPatch.editedPatch.discardEditedItem(index)"
        @save="comChannelsPatch.editedPatch.saveItem(index)"
        :error="comChannelsPatch.editedPatch.getItem(index).error"
      >
        <template #view>
          {{
            `Type: ${item.savedValue.type}; value: ${item.savedValue.value}` +
            (item.savedValue.comment ? `; comment: ${item.savedValue.comment}` : '')
          }}
        </template>

        <template #edit>
          Type:
          <select
            v-model="comChannelsPatch.editedPatch.array[index]!.editedValue.type"
            @input="deleteErrorFromComChanItem(index)"
          >
            <option disabled value="">Select...</option>
            <option v-for="item in communicationChannelTypes" :key="item">{{ item }}</option>
          </select>
          <div>
            value:
            <input
              type="text"
              v-model="comChannelsPatch.editedPatch.getItem(index).editedValue.value"
              @input="deleteErrorFromComChanItem(index)"
            >
          </div>
          <div>
            comment:
            <input type="text" v-model="comChannelsPatch.editedPatch.getItem(index).editedValue.comment">
          </div>
        </template>
      </ConfigRow>
      <a @click="comChannelsPatch.editedPatch.deleteItem(index)" v-if="!item.isEditing" class="clickable">🗑️</a>
    </div>
    <div class="withEmptyLineBelow"></div>

    <div>
      Type:
      <select v-model="comChannelsPatch.editedPatch.currentInput.type" @input="comChannelsPatch.editedPatch.currentError = null">
        <option disabled value="">Select...</option>
        <option v-for="item in communicationChannelTypes" :key="item">{{ item }}</option>
      </select>
    </div>
    <div>
      value:
      <input
        type="text"
        v-model="comChannelsPatch.editedPatch.currentInput.value"
        @input="comChannelsPatch.editedPatch.currentError = null"
      >
    </div>
    <div>
      comment:
      <input type="text" v-model="comChannelsPatch.editedPatch.currentInput.comment">
    </div>

    <div class="withEmptyLineBelow">
      <button
        @click="comChannelsPatch.editedPatch.addCurrentInputToArray"
        :class="{ grayed: !comChannelsPatch.editedPatch.canAddNewItem() }"
      >Add communication channel</button>
      <span class="error">{{ comChannelsPatch.editedPatch.currentError }}</span>
    </div>

    <div :class="{ grayed: isAlreadyDeleting() }">
      <span v-if="hasAnythingChanged">
        <button @click="revertAll()">Revert↩️</button>
        <button @click="saveChanges" :class="{ grayed: isSaving }">Save✅</button>
      </span>
      <span v-else>
        <button @click="stopEditingContact">Close</button>
      </span>
    </div>
    <div class="withEmptyLineBelow"></div>

    <div>
      <span v-if="deleteDialogStage > 0">
        Are you sure you want to delete "{{contact.nameToActuallyDisplay}}"?
        <YesNoDialog
          :delay="delay"
          :yes-callback="confirmDelete"
          :no-callback="cancelDeleting"
          :style="{
            '--yes-bg': 'red',
            '--yes-color': 'white'
          }"
          :yesGrayed="isAlreadyDeleting()"
        >

        </YesNoDialog>
      </span>
      <span v-else>
        <button @click="deleteDialogStage = 1" class="redButton">Delete🗑️</button>
      </span>
    </div>

  </div>
  <div v-else>
    <span
      v-for="iconId in savedContact.icons"
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
        @click="focusIcon(iconId)"
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
      Category: {{savedContact.category}}
    </div>

    <div v-if="savedContact.nameToActuallyDisplay !== savedContact.firstName">
      Displayed name: {{ savedContact.nameToActuallyDisplay }}
    </div>

    <div>
      First name: {{savedContact.firstName}}
    </div>

    <div>
      Last name: {{savedContact.lastName}}
    </div>

    <div v-if="savedContact.patronymicName">
      Patronymic name: {{savedContact.patronymicName}}
    </div>

    <div v-if="savedContact.birthdayDate">
      Birthday date: {{formatBirthdayDate(savedContact.birthdayDate)}}
    </div>

    <div v-if="savedContact.birthdayDate && contactBirthdayBeginning">
      The next birthday: {{
        formatZonedDateTime(contactBirthdayBeginning.withTimeZone(effectiveTimezone()))
        + (
          savedContact.birthdayDate.year === null || savedContact.birthdayDate.year === undefined ?
            ""
            : ` (turning ${contactBirthdayBeginning.year - savedContact.birthdayDate.year})`
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
      Timezone: {{savedContact.timezone}}
    </div>

    <div>
      Created at: {{ getDateFromMilliseconds(savedContact.creationDate) }}
    </div>

    <div>
      Modified at: {{ getDateFromMilliseconds(savedContact.modificationDate) }}
    </div>

    <div v-for="communicationChannel in savedContact.communicationChannels" class="centeredVertically gap">
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
      <button v-if="!isLoadingInitialIcons" @click="isEditingContact = true">Edit</button>
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

.icon-row {
  display: grid;
  grid-template-columns: 45px 130px auto;
  align-items: center;
}

.preview {
  width: 45px;
  height: 45px;
  overflow: hidden;
}

</style>
