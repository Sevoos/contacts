
import { useSessionStorage } from '@vueuse/core'

export interface AppState {
  user: null | {
    id: string
    name: string
  }
  cart: Array<{ id: string; qty: number }>
}

export const useAppState = () =>
  useSessionStorage<AppState>('appState', {
    user: null,
    cart: []
  })
