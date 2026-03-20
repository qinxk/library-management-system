export interface PendingReader {
  id: number
  username: string
  readerStatus: string
}

export interface AdminLoan {
  id: number
  readerUsername: string
  bookId: number
  bookTitle: string
  bookAuthor: string
  borrowedAt: string
  dueAt: string
  returnedAt: string | null
}
