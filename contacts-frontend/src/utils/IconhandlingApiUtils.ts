import {myStore} from "@/store";
import {
  deleteIconFullQuality, deleteIconPreview,
  listAllIcons, saveIconFullQuality, saveIconPreview
} from "@/composables/useImageCache.ts";
import {readConfig} from "@/config/settings.ts";
import {ResponseEntity, SetDefaultIconDto} from "@/lib";

async function getAllStoredIcons(): Promise<bigint[]> {
  const suffix = "-preview"
  return Array.from(
    new Set(
      (await listAllIcons())
        .map(stringId => {
          return stringId.endsWith(suffix)
            ? stringId.substring(0, stringId.length - suffix.length)
            : stringId
        })
    )
  ).map(
    stringId => {
      return BigInt(stringId)
    }
  )
}

export async function deleteNonexistentIcons() {
  const store = myStore()
  const promises: Promise<void>[] = []
  getAllStoredIcons().then(ids => {
    ids.forEach(id => {
      promises.push(
        store.iconApiService.checkIconExistence(id).then(iconExists => {
          if (!iconExists) {
            deleteIconFullQuality(id)
            deleteIconPreview(id)
          }
        }).catch(reason => {
          console.error(reason)
        })
      )
    })
  })
  await Promise.allSettled(promises)
}

export async function pullIconPreview(id: bigint) {
  const store = myStore()
  const previewResponse = await store.iconHandlingApiService.downloadIconPreview(id)
  if (previewResponse.status == 200) {
    await saveIconPreview(id, previewResponse.body!)
  }
}

export async function pullIconFullQuality(id: bigint) {
  const store = myStore()
  const fullQualityResponse = await store.iconHandlingApiService.downloadIconFullQuality(id)
  if (fullQualityResponse.status == 200) {
    await saveIconFullQuality(id, fullQualityResponse.body!)
  }
}

export async function uploadIcon(contactId: bigint, filename: string, fileBytes: Int8Array): Promise<ResponseEntity<bigint>> {
  const store = myStore()
  const response = await store.iconHandlingApiService.uploadIcon(contactId, filename, fileBytes)
  if (response.status === 200 && readConfig().setCreatedIconAsDefault) {
    await store.contactApiService.setDefaultIcon(new SetDefaultIconDto(contactId, response.body!))
  }
  return response
}
