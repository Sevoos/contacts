export class LocalStore<T> {
  constructor(protected key: string) {}

  protected parse(raw: string): T {
    return (JSON.parse(raw) as T)
  }

  protected stringify(value: T): string {
    return JSON.stringify(value)
  }

  get(): T | null {
    const raw = localStorage.getItem(this.key)
    return raw ? this.parse(raw) : null
  }

  set(value: T): void {
    localStorage.setItem(this.key, this.stringify(value))
  }

  clear(): void {
    localStorage.removeItem(this.key)
  }
}
