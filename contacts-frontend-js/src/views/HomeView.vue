<script setup lang="ts">

import {computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch} from "vue";
import {latestFetchAllContacts, readConfig} from "@/config/settings.ts";
import {
  findAllContacts, getBirthdayStart, searchTokens,
} from "@/utils/ContactUtils.ts";
  import type {CategoryNodeObject} from "@/utils/CategoryNodeObject.ts";
  import CategoryNode from "@/components/CategoryNode.vue";
import {
  type ContactItemIdentifier,
  flattenVisibleTree, keyToString, type NodeItemIdentifier
} from "@/utils/NavigationUtils.ts";
import ContactDetails from "@/components/ContactDetails.vue";
import {myGuiStore} from "@/store/GuiStore.ts";
import {storeToRefs} from "pinia";
import {myStore} from "@/store";
import {useImageObjectUrlCache} from "@/composables/useImageObjectUrlCache.ts";
import type {Contact} from "@/contact/Contact.ts";
import {getIconFullQualityKey, getIconPreviewKey} from "@/composables/useImageCache.ts";
import {Temporal} from "@js-temporal/polyfill";
import type {BirthdayDateDto} from "@/lib";
import {updateEverything} from "@/utils/ContactApiUtils.ts";
import {createContactPath, settingsPath} from "@/router/paths.ts";
import {readExpandedNodes, saveExpandedNodes} from "@/utils/Utils.ts";

const store = myStore()

const { contacts } = storeToRefs(store)

const stringComparator = new Intl.Collator()

const searchQuery = ref("")

function analyzeBirthday(contact: Contact) {
  if (contact.birthdayDate) {
    const contactId = contact.id
    const getNextBirthday = (birthdayDate: BirthdayDateDto) => {
      const currentNextBirthday = store.contactIdToNextBirthdayInstant.get(contactId)
      if (currentNextBirthday) {
        return currentNextBirthday
      }
      const newNextBirthday = getBirthdayStart(
        birthdayDate, contact.timezone
      )
      store.contactIdToNextBirthdayInstant.set(contactId, newNextBirthday)
      return newNextBirthday
    }
    const nextBirthday = getNextBirthday(contact.birthdayDate)

    const now = Temporal.Now.instant()
    const untilDuration = now.until(nextBirthday)
    if (untilDuration.seconds < 86400 && untilDuration.sign === 1
      // || contact.nameToActuallyDisplay === "Папа"
      // || contact.nameToActuallyDisplay === "Абзал"
    ) {
      store.contactsWithBirthdaySoon.add(contactId)
    } else {
      store.contactsWithBirthdaySoon.delete(contactId)
      const sinceDuration = now.since(nextBirthday)
      if (sinceDuration.seconds < 86400 && sinceDuration.sign >= 0) {
        store.contactsWithBirthdayNow.add(contactId)
      } else {
        store.contactsWithBirthdayNow.delete(contactId)
      }
    }
  }
}

const contactsToDisplay = computed(() => {
  const currentContacts = [...contacts.value]
  const currentSearchQuery = searchQuery.value
  return currentSearchQuery ? searchTokens(currentContacts, currentSearchQuery) : currentContacts
})

const rootNode = computed<CategoryNodeObject>(() => {
  const root: CategoryNodeObject = {
    name: "__root__",
    key: [],
    children: [],
    contacts: []
  }

  contactsToDisplay.value.forEach(contact => {
    let node = root

    for (const segment of contact.category) {
      let child = node.children.find(c => c.name === segment)

      if (!child) {
        const parentKey = node.key.slice()
        parentKey.push(segment)
        child = {
          name: segment,
          key: parentKey,
          children: [],
          contacts: []
        }
        node.children.push(child)
        node.children.sort((a, b) => stringComparator.compare(a.name, b.name))
      }

      allNodes.add(keyToString(child.key))

      node = child
    }

    node.contacts.push(contact)
    node.contacts.sort((a, b) => stringComparator.compare(a.nameToActuallyDisplay, b.nameToActuallyDisplay))
  })

  return root
})

const isFetchingContacts = ref(true)
const isLoading = computed(() => isFetchingContacts.value)

async function updateContacts() {
  isFetchingContacts.value = true
  try {
    await updateEverything()
    localStorage.setItem(latestFetchAllContacts, String(Date.now()))
  } catch (e) {
    console.error(e)
  } finally {
    store.contacts.splice(0, store.contacts.length)
    store.contacts.push(...findAllContacts())
    isFetchingContacts.value = false
  }
}

const config = ref(readConfig())

onMounted(async () => {
  const currentConfig = config.value

  if (currentConfig.fetchAllContactsOnlyManually) {
    isFetchingContacts.value = false
    return
  }
  const savedLatestFetchAllContacts = localStorage.getItem(latestFetchAllContacts)
  const now = Date.now()
  if (
    savedLatestFetchAllContacts === null
    || now - Number(savedLatestFetchAllContacts) >= currentConfig.fetchAllContactsPeriod
  ) {
    await updateContacts()
  } else {
    isFetchingContacts.value = false
  }
})

onBeforeUnmount(() => {
  guiStore.elementRegistry.clear()
  alive.value = false

  for (const iconId of store.iconIdToFullQualityUrl.keys()) {
    release(getIconFullQualityKey(iconId))
  }
  store.iconIdToFullQualityUrl.clear()

  for (const iconId of store.iconIdToPreviewUrl.keys()) {
    release(getIconPreviewKey(iconId))
  }
  store.iconIdToPreviewUrl.clear()
})

const allNodes = reactive(new Set<string>)
const expandedNodes = reactive(new Set<string>)

watch(
  expandedNodes,
  currentExpandedNodes => {
    saveExpandedNodes(Array.from(currentExpandedNodes))
  }
)

watch(
  rootNode,
  root => {
    expandedNodes.clear()
    const previousExpandedNodes = readExpandedNodes()
    if (previousExpandedNodes === null) {
      function expand(node: CategoryNodeObject) {
        for (const child of node.children) {
          expandedNodes.add(keyToString(child.key))
          expand(child)
        }
      }

      expand(root)
    } else {previousExpandedNodes.forEach(it => expandedNodes.add(it))
    }
  },
  { immediate: true }
)

const visibleItems = computed(() =>
  flattenVisibleTree(rootNode.value, expandedNodes)
)

const guiStore = myGuiStore()
const { focusedItem } = storeToRefs(guiStore)

const registryReady = computed(() => guiStore.elementRegistry.size > 0)

const {release} = useImageObjectUrlCache()

function moveFocus(delta: 1 | -1) {
  const currentFocusedItem = focusedItem.value

  if (!currentFocusedItem)
    return

  const index = visibleItems.value.findIndex(treeNavItem =>
    treeNavItem.type === currentFocusedItem.type &&
    (treeNavItem.type === "node"
      ? treeNavItem.key === (<NodeItemIdentifier>currentFocusedItem).key
      : treeNavItem.id === (<ContactItemIdentifier>currentFocusedItem).contactId)
  )

  const next = visibleItems.value[index + delta]
  if (!next) return

  focusedItem.value =
    next.type === "node"
      ? { type: "node", key: next.key }
      : { type: "contact", contactId: next.id }

}

function onTreeKeyDown(e: KeyboardEvent) {
  if (e.key === "ArrowDown") {
    e.preventDefault()
    moveFocus(+1)
  } else if (e.key === "ArrowUp") {
    e.preventDefault()
    moveFocus(-1)
  } else if (focusedItem.value?.type === "contact" && e.key === "ArrowLeft") {
    e.preventDefault()
    contactIdToArrowLeftToDetails.set(focusedItem.value.contactId, true)
  }
}

let queuedFocusId: string | null = null

watch(guiStore.elementRegistry, currentValue => {
  if (queuedFocusId) {
    const el = currentValue.get(queuedFocusId)
    if (el) {
      queuedFocusId = null
      el.focus()
    }
  }
})

watch(
  [focusedItem, visibleItems, registryReady],
  async ([item, _, ready]) => {
    if (!item || !ready) return
    if (store.hasContactJustBeenReplaced) {
      store.hasContactJustBeenReplaced = false
      return
    }

    await nextTick()

    const id =
      item.type === "node"
        ? `node:${item.key}`
        : `contact:${item.contactId}`

    const el = guiStore.elementRegistry.get(id)
    if (!el) {
      queuedFocusId = id
      return
    }

    el.focus()
  },
  { flush: "post", immediate: true }
)

function focusFirst() {
  const currentVisibleItems = visibleItems.value
  if (/*!focusedItem.value && */currentVisibleItems.length > 0) {
    const first = currentVisibleItems[0]!
    focusedItem.value =
      first.type === "node"
        ? { type: "node", key: first.key }
        : { type: "contact", contactId: first.id }
  }
}

watch(
  contacts,
  currentContacts => {
    currentContacts.forEach(contact => {
      analyzeBirthday(contact)
    })
  },
  { immediate: true }
)

watch(
  [visibleItems, searchQuery],
  (currentValue, previousValue) => {
    const previousSearchQuery = previousValue[1]
    const currentSearchQuery = currentValue[1]
    if (previousSearchQuery === currentSearchQuery || previousSearchQuery === undefined) {
      const currentVisibleItems = currentValue[0]
      if (!currentVisibleItems.length) return
      if (focusedItem.value) return

      focusFirst()
    } else {
      focusedItem.value = null
    }
  },
  { immediate: true }
)

const contactIdToArrowLeftToDetails = reactive(new Map<bigint, boolean>)

function getArrowLeftToDetails(contactId: bigint): boolean {
  const result = contactIdToArrowLeftToDetails.get(contactId) ?? false
  if (result) {
    contactIdToArrowLeftToDetails.delete(contactId)
  }
  return result
}

const alive = ref(true)

function localFindContactById(id: bigint): Contact | null {
  return contactsToDisplay.value.find(contact => contact.id === id) ?? null
}

function clickContactLink(contactId: bigint) {
  let categoryNode = localFindContactById(contactId)!.category
  while (categoryNode.length > 0) {
    expandedNodes.add(keyToString(categoryNode))
    categoryNode = categoryNode.slice(0, categoryNode.length - 1)
  }
  guiStore.focusedItem = { type: 'contact', contactId: contactId }
}

</script>

<template>
  <div v-if="isLoading">
    <a>Loading...</a>
  </div>
  <div v-else :class="{ disabled: isLoading }">

    <div class="layout">
      <section
        class="left"
      >
        <div
          v-if="focusedItem?.type === 'contact'"
        >
          <ContactDetails
            v-if="localFindContactById(focusedItem.contactId)"
            :key="focusedItem.contactId.toString()"
            :contact="localFindContactById(focusedItem.contactId)!"
            :getArrowLeftToDetails="(contactId: bigint) => getArrowLeftToDetails(contactId)"
            :alive="alive"
            :moveFocus="moveFocus"
          />
        </div>

      </section>

      <section
        class="right"
        tabindex="0"
        @keydown="onTreeKeyDown"
      >
        <div v-if="store.contactsWithBirthdayNow.size > 0">
          <span>
            People with birthday at the moment:
          </span>
          <span v-for="(contactId, index) in Array.from(store.contactsWithBirthdayNow)">
            <span class="contactLink" @click="clickContactLink(contactId)">
              {{localFindContactById(contactId)!.nameToActuallyDisplay}}
            </span>
            <a>{{(index === store.contactsWithBirthdayNow.size - 1 ? "" : ", ")}}</a>
          </span>
        </div>
        <div v-if="store.contactsWithBirthdaySoon.size > 0">
          <span>
            People with birthday soon:
          </span>
          <span v-for="(contactId, index) in Array.from(store.contactsWithBirthdaySoon)">
            <span class="contactLink" @click="clickContactLink(contactId)">
              {{localFindContactById(contactId)!.nameToActuallyDisplay}}
            </span>
            <a>{{(index === store.contactsWithBirthdaySoon.size - 1 ? "" : ", ")}}</a>
          </span>
        </div>
        <div class="row">
          <span
            class="reload"
            @click="updateContacts()"
          >
              <a>🗘</a>
          </span>
          <input type="text" v-model="searchQuery"/>
          <RouterLink :to="createContactPath" class="noTextDecoration">➕</RouterLink>
          <span class="clickable alignToRight">
            <RouterLink class="noTextDecoration" :to="settingsPath">⚙️</RouterLink>
          </span>
        </div>
        <CategoryNode
          :expanded-nodes="searchQuery ? allNodes : expandedNodes"
          :node="rootNode"
          :alive="alive"
        />

      </section>
    </div>

  </div>
</template>

<style scoped>

.reload {
  width: 1em;
  text-align: center;
  margin-right: 4px;
  cursor: pointer;
}

.contactLink {
  cursor: pointer;
  color: #1a0dab;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 65dvh) 1fr;
  height: 100dvh;
  width: 100%;
}

.left {
  height: 100%;
  overflow: auto;
  border-right: 1px solid #ccc;
}

.right {
  overflow: auto;
}

.left,
.right {
  box-sizing: border-box;
}

.disabled {
  pointer-events: none;
}

</style>

