export interface Book {
  id: number
  title: string
  author: string
  isbn: string | null
  category: string | null
  description: string | null
  totalCopies: number
  availableCopies: number
}

export interface PageBook {
  content: Book[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
