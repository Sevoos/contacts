import {db, getStoreName} from "@/utils/db.ts";
import {pullIconFullQuality, pullIconPreview} from "@/utils/IconhandlingApiUtils.ts";
import {myStore} from "@/store";
import {useImageObjectUrlCache} from "@/composables/useImageObjectUrlCache.ts";

async function getIcon(id: string): Promise<Blob | null> {
  const blob = await db.get(getStoreName(), id);
  if (!blob) return null;
  return blob
}

export async function pullIconFullQualityIfNeeded(id: bigint): Promise<Blob | null> {
  const potentialResult = await getIconFullQuality(id)
  if (potentialResult) {
    return potentialResult
  }
  await pullIconFullQuality(id)
  return getIconFullQuality(id)
}

export async function pullIconPreviewIfNeeded(id: bigint): Promise<Blob | null> {
  const potentialResult = await getIconPreview(id)
  if (potentialResult) {
    return potentialResult
  }
  await pullIconPreview(id)
  return getIconPreview(id)
}

const {acquire} = useImageObjectUrlCache()

export async function acquireIconFullQualityIfNeeded(iconId: bigint) {
  const store = myStore()
  return pullIconFullQualityIfNeeded(iconId).then(blob => {
    if (!blob)
      return
    if (!store.iconIdToFullQualityUrl.get(iconId)) {
      store.iconIdToFullQualityUrl = new Map(
        store.iconIdToFullQualityUrl
      ).set(iconId, acquire(getIconFullQualityKey(iconId), blob))
    }
  })
}

export async function acquireIconPreviewIfNeeded(iconId: bigint) {
  const store = myStore()
  const blob = await pullIconPreviewIfNeeded(iconId)
  if (!blob)
    return
  if (!store.iconIdToPreviewUrl.get(iconId)) {
    store.iconIdToPreviewUrl.set(iconId, acquire(getIconPreviewKey(iconId), blob))
  }
}

export async function getIconFullQuality(id: bigint): Promise<Blob | null> {
  return getIcon(getIconFullQualityKey(id))
}

export async function getIconPreview(id: bigint): Promise<Blob | null> {
  return getIcon(getIconPreviewKey(id))
}

async function deleteIcon(id: string) {
  await db.delete(getStoreName(), id);
}

export async function deleteIconFullQuality(id: bigint) {
  return deleteIcon(getIconFullQualityKey(id))
}

export async function deleteIconPreview(id: bigint) {
  return deleteIcon(getIconPreviewKey(id))
}

export async function listAllIcons(): Promise<string[]> {
  return (await db.getAllKeys(getStoreName())).map(value => {
    return value.toString()
  })
}

export function getIconFullQualityKey(id: bigint): string {
  return id.toString()
}

export function getIconPreviewKey(id: bigint): string {
  return getIconFullQualityKey(id) + "-preview"
}

function getFullQualityMime(): string {
  return "image/png"
}

function getPreviewMime(): string {
  return "image/jpeg"
}

function int8ToBlob(
  data: Int8Array,
  mime: string
): Blob {
  return new Blob([data as unknown as BufferSource], { type: mime })
}

async function saveIcon(id: string, bytes: Int8Array, mime: string) {
  await db.put(
    getStoreName(),
    int8ToBlob(bytes, mime),
    id
  )
}

export async function saveIconFullQuality(id: bigint, bytes: Int8Array) {
  await saveIcon(getIconFullQualityKey(id), bytes, getFullQualityMime())
}

export async function saveIconPreview(id: bigint, bytes: Int8Array) {
  await saveIcon(getIconPreviewKey(id), bytes, getPreviewMime())
}
