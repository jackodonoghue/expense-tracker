export interface Transaction {
  transaction_id: string;
  timestamp: Date;
  description: string;
  amount: number;
  currency: string;
  transaction_type: string;
  transaction_category: string;
  merchant_name: string;
}