import {LocalStore} from "@/utils/LocalStore.ts";
import type {Contact} from "@/contact/Contact.ts";

export class ContactLocalStore extends LocalStore<Contact> {
  constructor(private id: bigint) {
    super("contact-" + id.toString());
  }

  protected parse(raw: string): Contact {
    return (JSON.parse(raw, bigintReviver) as Contact)
  }

  protected stringify(value: Contact): string {
    return JSON.stringify(value, bigintReplacer)
  }

}

type BigIntJson =
  | { __type: 'bigint'; value: string }

function isBigIntJson(value: unknown): value is BigIntJson {
  return (
    typeof value === 'object' &&
    value !== null &&
    '__type' in value &&
    (value as Record<string, unknown>).__type === 'bigint'
  )
}

export function bigintReplacer(
  _key: string,
  value: unknown
): unknown {
  return typeof value === 'bigint'
    ? { __type: 'bigint', value: value.toString() }
    : value
}

function bigintReviver(
  _key: string,
  value: unknown
): unknown {
  return isBigIntJson(value)
    ? BigInt(value.value)
    : value
}
