export function sanitizeSpaces(input: string): string {
  let result = input
  while (result.endsWith(' ')) {
    result = result.substring(0, result.length - 1)
  }
  while (result.startsWith(' ')) {
    result = result.substring(1, result.length)
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

export function isImageMime(file: File) {
  return file.type.startsWith("image/")
}

export function getFile(input: HTMLInputElement): File | undefined {
  return input.files?.[0]
}
