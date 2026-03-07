<script setup lang="ts">
import type { CategoryNodeObject } from "@/utils/CategoryNodeObject.ts"
import {
  type ComponentPublicInstance,
  computed,
  onMounted
} from "vue";
import {keyToString} from "@/utils/NavigationUtils.ts";
import type {Contact} from "@/contact/Contact.ts";
import {acquireIconPreviewIfNeeded} from "@/composables/useImageCache.ts";
import {emojiBirthdayNow, emojiBirthdaySoon, formatBirthdayDate} from "@/utils/ContactUtils.ts";
import {myGuiStore} from "@/store/GuiStore.ts";
import {myStore} from "@/store";

defineOptions({ name: "CategoryNode" })

const props = defineProps<{
  expandedNodes: Set<string>
  node: CategoryNodeObject
  alive: boolean
}>()

const isRoot = props.node.key.length === 0

const guiStore = myGuiStore()
const store = myStore()

onMounted(async () => {
  await Promise.allSettled(
    previewIds.map(previewId => {
      if (!props.alive)
        return
      return acquireIconPreviewIfNeeded(previewId)
    })
  )
})

function key(): string[] {
  return props.node.key
}

function keyAsString(): string {
  return keyToString(key())
}

function isExpanded() {
  return isRoot || props.expandedNodes.has(keyAsString())
}

function expand() {
  props.expandedNodes.add(keyAsString())
}

function collapse() {
  props.expandedNodes.delete(keyAsString())
}

function toggle() {
  if (isExpanded()) {
    collapse()
  } else {
    expand()
  }
}

function registerElement(id: string, el: HTMLElement | null) {
  if (!el) {
    guiStore.elementRegistry.delete(id)
  } else {
    guiStore.elementRegistry.set(id, el)
  }
}

function registerNodeRef(el: Element | ComponentPublicInstance | null) {
  if (el instanceof HTMLElement) {
    registerElement(`node:${keyAsString()}`, el)
  }
}

function onKeyDown(e: KeyboardEvent) {
  if (e.key === "ArrowRight") expand()
  if (e.key === "ArrowLeft") collapse()
}

const isFocusedNode = computed(() =>
  guiStore.focusedItem?.type === "node" && guiStore.focusedItem.key === keyAsString()
)

const isFocusedContact = (c: Contact) =>
  guiStore.focusedItem?.type === "contact" && guiStore.focusedItem.contactId === c.id

function registerContactRef(id: bigint, el: Element | ComponentPublicInstance | null) {
  if (el instanceof HTMLElement) {
    registerElement(`contact:${id.toString()}`, el)
  }
}

const previewIds = props.node.contacts.filter(contact => contact.defaultIconId).map(contact => contact.defaultIconId!)

const somePreviewsLoading = computed<boolean>(() => {
  let result = false
  for (const previewId of previewIds) {
    if (!store.iconIdToPreviewUrl.get(previewId)) {
      result = true
      break
    }
  }
  return result
})

function setFocusedContact(contact: Contact) {
  guiStore.focusedItem = { type: 'contact', contactId: contact.id }
}

</script>

<template>

  <div
    class="tree-node"
  >
    <div
      class="category-header"
      v-if="!isRoot"
      :tabindex="isFocusedNode ? 0 : -1"
      @click="
        guiStore.focusedItem = { type: 'node', key: keyAsString() }
      "
      @keydown="onKeyDown"
      :ref="registerNodeRef"
    >
      <span
        class="arrow"
        @click="toggle()"
      >
        <template v-if="node.children.length || node.contacts.length">
          {{ isExpanded() ? "▾" : "▸" }}
        </template>
      </span>

      <span class="category-name">
        {{ node.name }}
      </span>
    </div>


    <div
      v-if="isExpanded()"
    >

      <CategoryNode
        :style="{ paddingLeft: `${(node.key.length) * 16}px` }"
        v-for="child in node.children"
        :key="keyToString(child.key)"
        :node="child"
        :focusedItem="guiStore.focusedItem"
        :expanded-nodes="expandedNodes"
        :alive="alive"
      />
      <div
        v-if="somePreviewsLoading"
      >
        <a>Loading...</a>
      </div>
      <div
        v-else
        v-for="contact in node.contacts"
        :key="contact.id.toString()"
        class="contact-row centeredVertically"
        @click="setFocusedContact(contact)"
        :tabindex="isFocusedContact(contact) ? 0 : -1"
        :ref="el => registerContactRef(contact.id, el)"
      >
        <span
          v-if="contact.defaultIconId"
        >
          <img
            :src="store.iconIdToPreviewUrl.get(contact.defaultIconId)"
            alt="Preview"
          >
        </span>
        <span>
          {{ contact.nameToActuallyDisplay }}
        </span>

        <span v-if="contact.birthdayDate">: {{ formatBirthdayDate(contact.birthdayDate!) }}</span>

        <span v-if="store.contactsWithBirthdaySoon.has(contact.id)">{{emojiBirthdaySoon}}</span>
        <span v-else-if="store.contactsWithBirthdayNow.has(contact.id)">{{emojiBirthdayNow}}</span>

      </div>

    </div>
  </div>
</template>

<style scoped>

.category-header {
  display: flex;
  align-items: center;
  user-select: none;
  outline: none;
}

.arrow {
  width: 1em;
  text-align: center;
  margin-right: 4px;
  cursor: pointer;
}

.contact-row {
  padding: 2px 0 2px 20px;
}

.category-header:focus,
.contact-row:focus {
  background: #eef;
}

</style>
