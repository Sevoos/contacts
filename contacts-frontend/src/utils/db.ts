import { openDB } from "idb"

export function getStoreName(): string {
  return "icons"
}

export interface Picture {
  id: string;
  data: ArrayBufferLike;
}

export const db = await openDB("images-db", 1, {
  upgrade(db) {
    db.createObjectStore(getStoreName())
  }
})
