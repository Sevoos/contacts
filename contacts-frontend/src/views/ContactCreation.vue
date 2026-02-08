<script setup lang="ts">

import {ContactCreationDto, EnumCommunicationChannel} from "@/lib";
import {computed, onBeforeUnmount, reactive, ref} from "vue";
import {
  defaultTimezoneForContactCreation,
  latestFetchAllContacts,
  readConfig,
  validateTimezone
} from "@/config/settings.ts";
import {
  birthdayDate,
  type ContactCreationDtoKeyName,
  copyContactCreationDto,
  displayName,
  firstName,
  getMaxDay,
  isLeapYear,
  isRequired,
  lastName,
  patronymicName,
  timezone
} from "@/utils/ContactCreationUtils.ts";
import FormInputRow from "@/components/FormInputRow.vue";
import {timeZonesNames} from "@vvo/tzdb";
import ConfigRow from "@/components/ConfigRow.vue";
import {communicationChannelTypes} from "@/contact/CommunicationChannel.ts";
import {
  comChanContactCreationFromDto,
  comChanContactCreationToDto,
  CommunicationChannelContactCreation,
  doComChansContactCreationsEqual
} from "@/contact/CommunicationChannelContactCreation.ts";
import {detectedTimezone} from "@/utils/TimezoneUtils.ts";
import {Temporal} from "@js-temporal/polyfill";
import {myStore} from "@/store";
import {uploadIcon} from "@/utils/IconhandlingApiUtils.ts";
import router from "@/router";
import {homePath} from "@/router/paths.ts";
import {InputArray} from "@/utils/InputArray.ts";
import {
  deletePatternFromInputEvent,
  getFile,
  isImageMime, nonDigitRegExp,
  sanitizeSpaces
} from "@/utils/Utils.ts";
import {
  BirthdayDateCreation,
  birthdayDateCreationToDto,
  isBirthdayEmpty
} from "@/contact/BirthdayDateCreation.ts";

function emptyDto(): ContactCreationDto {
  return new ContactCreationDto(
    null, [], [], "", "", "", "",
    localStorage.getItem(defaultTimezoneForContactCreation) ?? detectedTimezone
  )
}

const currentDto = reactive(emptyDto())
const currentBirthdayDate = reactive(new BirthdayDateCreation(null, null, null))
const keys = Object.keys(currentDto) as Array<keyof typeof currentDto>;

function errorCheck(): Record<ContactCreationDtoKeyName, (dto: ContactCreationDto) => string | null> {
  return {
    birthdayDate: () => {
      if (!includeBirthdayDate.value) {
        return null
      }
      if (isBirthdayEmpty(currentBirthdayDate)) {
        return "Birthday date should be included yet is absent"
      }
      const year = currentBirthdayDate.year
      const yearNotNullish = year !== null && year !== undefined

      if (yearNotNullish) {
        if (year > Temporal.Now.plainDateISO(currentDto.timezone).year) {
          return "Year is too great"
        }
        if (year < 1900 && year !== 0) {
          return "Year is too long ago"
        }
      }

      const month = currentBirthdayDate.month
      if (month === null) {
        return "Month can't be empty"
      }
      if (month < 1 || month > 12) {
        return "Month should belong to [1; 12]"
      }
      const day = currentBirthdayDate.day
      if (day === null) {
        return "Day can't be empty"
      }
      if (day < 1) {
        return "Day must be a positive number"
      }

      return day <= getMaxDay(month, yearNotNullish ? isLeapYear(year) : true) ? null : "Day is too large"
    },
    category: () => null,
    communicationChannels: () => null,
    displayName: () => null,
    firstName: () => null,
    lastName: () => null,
    patronymicName: () => null,
    timezone: (dto: ContactCreationDto) => {
      const timezone = dto.timezone
      return validateTimezone(timezone) ? null : `Invalid timezone: \"${timezone}\"`
    },
  }
}

function getError(dto: ContactCreationDto, key: ContactCreationDtoKeyName): string | null {
  if (isRequired(key) && !dto[key]) {
    return key + " can't be empty"
  }
  return errorCheck()[key](dto)
}

const includeBirthdayDate = ref(true)

function sanitizeNonNegativeNumber(e: InputEvent, callback: (numberUnlessEmpty: number | null) => void) {
  const result = deletePatternFromInputEvent(e, nonDigitRegExp)
  callback(result === '' ? null : Number(result))
}

function onYearInput(e: InputEvent) {
  sanitizeNonNegativeNumber(e, (numberUnlessEmpty: number | null) => {
    currentBirthdayDate.year = numberUnlessEmpty
  })
}

function onMonthInput(e: InputEvent) {
  sanitizeNonNegativeNumber(e, (numberUnlessEmpty: number | null) => {
    currentBirthdayDate.month = numberUnlessEmpty
  })
}

function onDayInput(e: InputEvent) {
  sanitizeNonNegativeNumber(e, (numberUnlessEmpty: number | null) => {
    currentBirthdayDate.day = numberUnlessEmpty
  })
}

const categoryLevelsInputArray = reactive(
  new InputArray(
    () => "",
    currentDto.category,
    (categoryLevel: string) => !!(categoryLevel),
    () => null,
    (value: string) => sanitizeSpaces(value)
  )
)

const comChanInputArray = reactive(
  new InputArray(
    () => new CommunicationChannelContactCreation("", "", ""),
    currentDto.communicationChannels.map(dto => comChanContactCreationFromDto(dto)),
    (comChan: CommunicationChannelContactCreation) => {
      return !!(comChan.type && comChan.value)
    },
    (comChan: CommunicationChannelContactCreation) => {
      if (comChan.type === EnumCommunicationChannel.Email.name) {
        function isValidEmail(email: string): boolean {
          const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
          return pattern.test(email);
        }
        if (!isValidEmail(comChan.value)) {
          return  "Invalid email address"
        }
      }
      return null
    },
    (comChan: CommunicationChannelContactCreation) => {
      const comment = comChan.comment === null ? "" : sanitizeSpaces(comChan.comment)
      return new CommunicationChannelContactCreation(
        comment ? comment : null,
        sanitizeSpaces(comChan.type),
        sanitizeSpaces(comChan.value)
      )
    }
  )
)

function deleteErrorFromComChanItem(index: number) {
  comChanInputArray.getItem(index).error = null
  console.log(comChanInputArray.getItem(index).editedValue.value)
}

function deleteComChanInputArrayCurrentError() {
  comChanInputArray.currentError = null
}

const memorizeTimezoneSelection = ref(readConfig().memorizeTimezoneDuringContactCreation)

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = getFile(input)
  if (!file) {
    iconExistsButInvalid.value = false
    return
  }

  const isImage = isImageMime(file)
  iconExistsButInvalid.value = !isImage
  iconInputError.value = isImage ? null : "If the file is selected, it has to be an image"
  if (!isImage) {
    return
  }
  if (iconPreviewUrl.value) {
    URL.revokeObjectURL(iconPreviewUrl.value)
  }
  iconPreviewUrl.value = URL.createObjectURL(file)
}

const iconInput = ref<HTMLInputElement | null>(null)

function resetIconInput() {
  iconInput.value!.value = ""
  if (iconPreviewUrl.value) {
    URL.revokeObjectURL(iconPreviewUrl.value)
  }
  iconPreviewUrl.value = null
  iconInputError.value = null
  iconExistsButInvalid.value = false
}

const iconInputError = ref<string | null>(null)

const iconPreviewUrl = ref<string | null>(null)

onBeforeUnmount(() => {
  if (iconPreviewUrl.value) {
    URL.revokeObjectURL(iconPreviewUrl.value)
  }
})

const displayError = ref<string | null>(null)
const formItemWithError = ref<ContactCreationDtoKeyName | null>(null)

const noFieldErrors = computed<boolean>(() => {
  const errorOrNullValues = keys.map(key => getError(currentDto, key))
  const result = errorOrNullValues.every(value => value === null)
  const index = errorOrNullValues.findIndex(value => value !== null)!
  if (index === -1) {
    formItemWithError.value = null
    displayError.value = null
  } else {
    formItemWithError.value = keys[index]!
    displayError.value = errorOrNullValues[index]!
  }
  return result
})

const iconExistsButInvalid = ref(false)

const canSubmit = computed<boolean>(() => {
  if (!noFieldErrors.value) {
    return false
  }
  return !iconExistsButInvalid.value;
})

const isSubmitting = ref(false)

function submit() {
  isSubmitting.value = true
  const store = myStore()
  const partialDto = copyContactCreationDto(currentDto)
  partialDto.category = categoryLevelsInputArray.array.map(value => value.savedValue)
  partialDto.communicationChannels = comChanInputArray.array.map(value => comChanContactCreationToDto(value.savedValue))
  if (includeBirthdayDate.value) {
    partialDto.birthdayDate = birthdayDateCreationToDto(currentBirthdayDate)
  }
  if (!partialDto.displayName) {
    partialDto.displayName = null
  }
  if (!partialDto.patronymicName) {
    partialDto.patronymicName = null
  }
  store.contactApiService.createContact(partialDto).then(response => {
    localStorage.setItem(defaultTimezoneForContactCreation, partialDto.timezone)
    const status = response.status
    if (status === 200) {
      const contactId = response.body!
      function goToHome() {
        localStorage.removeItem(latestFetchAllContacts)
        router.push(homePath)
      }
      const file = getFile(iconInput.value!)
      if (!file) {
        goToHome()
        return;
      }
      file.arrayBuffer().then(buffer => {
        const fileBytes = new Int8Array(buffer)
        uploadIcon(contactId, file.name, fileBytes).then(() => {
          goToHome()
        }).catch(reason => console.error(reason))
      })
    } else {
      console.error(`Status ${status}`)
    }
  }).catch(reason => console.error(reason))
}

</script>

<template>

  <RouterLink :to="homePath" class="noTextDecoration">📒Home</RouterLink>

  <h3>Contact creation</h3>

  <div>
    Icon:
    <input
      ref="iconInput"
      type="file"
      accept="image/*"
      @change="onFileChange"
    />
    <a @click="resetIconInput" v-if="iconInput?.value" class="clickable">🗑️</a>
  </div>

  <div class="preview" v-if="iconPreviewUrl">
    <img alt="Preview" :src="iconPreviewUrl">
  </div>

  <div class="withEmptyLineBelow"></div>

  <div>Category:</div>
  <div v-for="(item, index) in categoryLevelsInputArray.array" :key="index">
    <ConfigRow
      :label="String(index + 1)"
      postLabelChar="."
      :editing="item.isEditing"
      :changed="item.savedValue !== item.editedValue"
      @edit="categoryLevelsInputArray.startEditing(index)"
      @discard="categoryLevelsInputArray.discardEditedItem(index)"
      @save="categoryLevelsInputArray.saveItem(index)"
      :error="categoryLevelsInputArray.getItem(index).error"
    >
      <template #view>
        {{ item.savedValue }}
      </template>

      <template #edit>
        <input
          type="text"
          v-model="categoryLevelsInputArray.getItem(index).editedValue"
          @input="categoryLevelsInputArray.getItem(index).error = null"
        >
      </template>
    </ConfigRow>
    <a @click="categoryLevelsInputArray.deleteItem(index)" v-if="!item.isEditing" class="clickable">🗑️</a>
  </div>
  <div>
    Name:
    <input type="text" v-model="categoryLevelsInputArray.currentInput">
    <button
      @click="categoryLevelsInputArray.addCurrentInputToArray"
      :class="{ grayed: !categoryLevelsInputArray.canAddNewItem() }"
    >Add category level</button>
  </div>

  <div class="withEmptyLineBelow"></div>

  <FormInputRow
    label="Display name"
    :required="isRequired(displayName)"
    class="input-row"
    :hasError="formItemWithError === displayName"
  >
    <input
      type="text"
      v-model="currentDto.displayName"
    >
  </FormInputRow>

  <FormInputRow
    label="First name"
    :required="isRequired(firstName)"
    class="input-row"
    :hasError="formItemWithError === firstName"
  >
    <input
      type="text"
      v-model="currentDto.firstName"
    >
  </FormInputRow>

  <FormInputRow
    label="Last name"
    :required="isRequired(lastName)"
    class="input-row"
    :hasError="formItemWithError === lastName"
  >
    <input
      type="text"
      v-model="currentDto.lastName"
    >
  </FormInputRow>

  <FormInputRow
    label="Patronymic name"
    :required="isRequired(patronymicName)"
    class="input-row"
    :hasError="formItemWithError === patronymicName"
  >
    <input
      type="text"
      v-model="currentDto.patronymicName"
    >
  </FormInputRow>

  <FormInputRow
    label="Birthday date (YYYY-MM-DD)"
    :required="includeBirthdayDate"
    class="input-row"
    :hasError="formItemWithError === birthdayDate"
  >
      <span :class="{ 'grayed': !includeBirthdayDate }">
        <input
          type="text"
          inputmode="numeric"
          pattern="[0-9]*"
          placeholder="Year (optional)"
          :value="currentBirthdayDate.year"
          @input="onYearInput"
          size="10"
        />

        <input
          type="text"
          inputmode="numeric"
          pattern="[0-9]*"
          placeholder="Month"
          :value="currentBirthdayDate.month"
          @input="onMonthInput"
          size="4"
        />

        <input
          type="text"
          inputmode="numeric"
          pattern="[0-9]*"
          placeholder="Day"
          :value="currentBirthdayDate.day"
          @input="onDayInput"
          size="4"
        />
      </span>
    <label>
      <input type="checkbox" v-model="includeBirthdayDate">Include birthday date
    </label>
  </FormInputRow>

  <FormInputRow
    label="Timezone"
    :required="isRequired(timezone)"
    class="input-row"
    :hasError="formItemWithError === timezone"
  >
    <select v-model="currentDto.timezone">
      <option v-for="timezone in timeZonesNames" :key="timezone">
        {{ timezone }}
      </option>
    </select>
    <input
      type="checkbox"
      v-model="memorizeTimezoneSelection"
      :class="{ 'grayed': !currentDto.timezone }"
    >Memorize timezone selection
  </FormInputRow>

  <div class="withEmptyLineBelow"></div>

  <div>Communication channels:</div>

  <div v-for="(item, index) in comChanInputArray.array" :key="index">
    <ConfigRow
      :label="String(index + 1)"
      postLabelChar="."
      :editing="item.isEditing"
      :changed="!doComChansContactCreationsEqual(item.savedValue, comChanInputArray.getItem(index).editedValue)"
      @edit="comChanInputArray.startEditing(index)"
      @discard="comChanInputArray.discardEditedItem(index)"
      @save="comChanInputArray.saveItem(index)"
      :error="comChanInputArray.getItem(index).error"
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
          v-model="comChanInputArray.array[index]!.editedValue.type"
          @input="deleteErrorFromComChanItem(index)"
        >
          <option disabled value="">Select...</option>
          <option v-for="item in communicationChannelTypes" :key="item">{{ item }}</option>
        </select>
        value:
        <input
          type="text"
          v-model="comChanInputArray.getItem(index).editedValue.value"
          @input="deleteErrorFromComChanItem(index)"
        >
        comment:
        <input type="text" v-model="comChanInputArray.getItem(index).editedValue.comment">
      </template>
    </ConfigRow>
    <a @click="comChanInputArray.deleteItem(index)" v-if="!item.isEditing" class="clickable">🗑️</a>
  </div>
  <div>
    Type:
    <select v-model="comChanInputArray.currentInput.type" @input="deleteComChanInputArrayCurrentError">
      <option disabled value="">Select...</option>
      <option v-for="item in communicationChannelTypes" :key="item">{{ item }}</option>
    </select>
    value:
    <input type="text" v-model="comChanInputArray.currentInput.value" @input="deleteComChanInputArrayCurrentError">
    comment:
    <input type="text" v-model="comChanInputArray.currentInput.comment">
    <button @click="comChanInputArray.addCurrentInputToArray" :class="{ grayed: !comChanInputArray.canAddNewItem() }">Add communication channel</button>
    <span class="error">{{ comChanInputArray.currentError }}</span>
  </div>

  <div class="withEmptyLineBelow"></div>

  <button @click="submit" v-if="canSubmit" :class="{ grayed: isSubmitting }">Create contact</button>
  <a v-else-if="displayError" class="error">{{ displayError }}</a>
  <a v-else class="error">{{ iconInputError }}</a>

</template>

<style scoped>

.input-row {
  display: grid;
  grid-template-columns: 340px 500px auto;
  align-items: center;
  gap: 0.5rem;
}

.preview {
  width: 200px;
  height: 200px;
  overflow: hidden;
}

.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* centered square preview */
  object-position: center;
}


</style>
