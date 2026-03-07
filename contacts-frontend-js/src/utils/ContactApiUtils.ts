import { myStore } from "@/store";
import {
  getIconPreview

} from "@/composables/useImageCache.ts";
import {deleteNonexistentIcons, pullIconPreview} from "@/utils/IconhandlingApiUtils.ts";
import {Contact, saveContactFromDto} from "@/contact/Contact.ts";
import {deleteContactIfSaved, findContactById} from "@/utils/ContactUtils.ts";

async function deleteNonexistentContacts() {
  const store = myStore()
  const prefix = "contact-"
  const promises: Promise<void>[] =
    Object.keys(localStorage)
    .filter(key => key.startsWith(prefix))
    .map(async key => {
      const contactId = BigInt(
        key.substring(prefix.length)
      )
      return store.contactApiService.checkContactExistence(contactId).then(contactExists => {
        if (!contactExists) {
          deleteContactIfSaved(contactId)
        }
      })
    })

  await Promise.allSettled(promises)
}

export async function updateEverything() {
  await deleteNonexistentContacts()
  await deleteNonexistentIcons()
  await fetchAllContacts()
}

async function fetchAllContacts() {
  const store = myStore()
  const idsArray = (await store.contactApiService.getAllContactsIds()).asJsReadonlyArrayView()
  const promises: Promise<void>[] = []
  idsArray.forEach((contactId) => {
    promises.push(pullContactWithPreviewIfNeeded(contactId))
  })
  await Promise.allSettled(promises)
}

export async function pullContactDto(id: bigint) {
  const store = myStore()
  const response = await store.contactApiService.getContact(id)
  if (response.status === 200) {
    saveContactFromDto(response.body!)
  }
}

export async function pullContactWithPreview(id: bigint): Promise<Contact> {
  await pullContactDto(id)
  const contact = findContactById(id)
  if (contact === null) {
    throw new Error("Failed to pull and save contactDto")
  }
  const defaultIconId = contact.defaultIconId
  if (defaultIconId) {
    if (await getIconPreview(defaultIconId) === null) {
      await pullIconPreview(defaultIconId)
    }
  }
  return contact
}

export async function pullContactWithPreviewIfNeeded(id: bigint) {
  const contact = findContactById(id)
  if (!contact) {
    await pullContactWithPreview(id)
  } else {
    const response = await myStore().contactApiService.getModificationDate(id)
    const contactOutdated = response.body! > contact!.modificationDate
    if (contactOutdated) {
      await pullContactWithPreview(id)
    }
  }
}
