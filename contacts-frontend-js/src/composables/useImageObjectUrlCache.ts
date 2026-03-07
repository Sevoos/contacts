type Entry = {
  url: string
  refs: number
}

const cache = new Map<string, Entry>()

export function useImageObjectUrlCache() {
  function acquire(iconId: string, blob: Blob): string {
    const existing = cache.get(iconId)
    if (existing) {
      existing.refs++
      return existing.url
    }

    const url = URL.createObjectURL(blob)
    cache.set(iconId, { url, refs: 1 })
    return url
  }

  function release(id: string) {
    const entry = cache.get(id)
    if (!entry) return

    entry.refs--
    if (entry.refs <= 0) {
      URL.revokeObjectURL(entry.url)
      cache.delete(id)
    }
  }

  return { acquire, release }
}
