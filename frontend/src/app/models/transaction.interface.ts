export interface Transaction {
  transactionId: string;
  accountId: string;
  amount: number;
  currency: string;
  description: string;
  transactionType: string;
  transactionCategory: string;
  timestamp: Date;
}