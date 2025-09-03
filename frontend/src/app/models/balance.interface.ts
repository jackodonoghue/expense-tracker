export interface Balance {
  accountId: string;
  accountName: string;
  currency: string;
  current: number;
  available: number;
  overdraft: number;
  lastUpdate: Date;
}