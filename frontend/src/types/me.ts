export type ReaderStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface MeUser {
  username: string
  role: 'ADMIN' | 'READER'
  readerStatus: ReaderStatus | null
}
