import {bigintReplacer} from "@/utils/ContactLocalStore.ts";

export function sanitizeSpaces(value: string): string {
  let result = value
  while (result.endsWith(' ')) {
    result = result.substring(0, result.length - 1)
  }
  while (result.startsWith(' ')) {
    result = result.substring(1, result.length)
  }
  return result
}

export function removeAllSpaces(value: string): string {
  let result = value
  while (true) {
    let replaced = result.replace(" ", "")
    if (result === replaced) break
    result = replaced
  }
  return result
}

export function getTargetFromEvent(e: InputEvent): HTMLInputElement {
  return e.target as HTMLInputElement
}

export const nonDigitRegExp = /\D+/g

export function deletePatternFromInputEvent(e: InputEvent, pattern: string | RegExp): string {
  const target = getTargetFromEvent(e)
  const result = target.value.replace(pattern, "")
  target.value = result
  return result
}

export function sanitizeNonNegativeNumber(e: InputEvent, callback: (numberUnlessEmpty: number | null) => void) {
  const result = deletePatternFromInputEvent(e, nonDigitRegExp)
  callback(result === '' ? null : Number(result))
}

export async function isValidImage(file: File): Promise<boolean> {
  try {
    const bitmap = await createImageBitmap(file)
    bitmap.close()
    return true
  } catch {
    return false
  }
}

export function getFile(input: HTMLInputElement): File | undefined {
  return input.files?.[0]
}

export function doObjectsStringsEqual<T>(a: T, b: T): boolean {
  return stringifyBigint(a) === stringifyBigint(b)
}

export function stringifyBigint(value: any): string {
  return JSON.stringify(value, bigintReplacer)
}

const expandedNodesKey = "expandedNodes"
const multipleNodeKeysSeparator = "€"

export function readExpandedNodes(): string[] | null {
  const previousExpandedNodes = localStorage.getItem(expandedNodesKey)
  if (previousExpandedNodes === null) {
    return null
  }
  return previousExpandedNodes.split(multipleNodeKeysSeparator)
}

export function saveExpandedNodes(value: string[]) {
  localStorage.setItem(
    expandedNodesKey,
    value.reduce((previousValue, currentValue) => {
      let result = previousValue
      if (previousValue !== "") {
        result += multipleNodeKeysSeparator
      }
      result += currentValue
      return result
    }, "")
)
}
