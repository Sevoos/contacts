import {defineStore} from "pinia";
import type {ContactItemIdentifier, NodeItemIdentifier} from "@/utils/NavigationUtils.ts";

export const myGuiStore = defineStore("guiStore", {
  state: (): {
    elementRegistry: Map<string, HTMLElement>
    focusedItem: ContactItemIdentifier | NodeItemIdentifier | null
    iconIdToDimensionSize: Map<bigint, number>
    selectedCategory: string[] | null
  } => ({
    elementRegistry: new Map(),
    focusedItem: null,
    iconIdToDimensionSize: new Map(),
    selectedCategory: null,
  })
})
