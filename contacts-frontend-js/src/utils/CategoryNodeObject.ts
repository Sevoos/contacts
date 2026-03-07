import type {Contact} from "@/contact/Contact.ts";

export interface  CategoryNodeObject {
  name: string
  key: string[]
  children: CategoryNodeObject[]
  contacts: Contact[]
}

