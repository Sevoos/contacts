import {defineStore} from "pinia";
import type {ContactItemIdentifier, NodeItemIdentifier} from "@/utils/NavigationUtils.ts";

export const myGuiStore = defineStore("guiStore", {
  state: (): {
    focusedItem: ContactItemIdentifier | NodeItemIdentifier | null
    iconIdToDimensionSize: Map<bigint, number>
    elementRegistry: Map<string, HTMLElement>
  } => ({
    focusedItem: null,
    iconIdToDimensionSize: new Map(),
    elementRegistry: new Map(),
  })
})
