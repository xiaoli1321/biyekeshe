declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'bootstrap' {
  export class Modal {
    constructor(element: HTMLElement, options?: any)
    show(): void
    hide(): void
    dispose(): void
  }
}

interface ImportMeta {
  env: {
    VITE_API_BASE_URL: string
  }
}