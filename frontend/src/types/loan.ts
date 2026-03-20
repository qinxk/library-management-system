export interface Loan {
  id: number
  bookId: number
  bookTitle: string
  bookAuthor: string
  borrowedAt: string
  dueAt: string
  returnedAt: string | null
}
