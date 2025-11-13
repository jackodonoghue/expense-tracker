import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api-service';
import { environment } from '../../environments/environment';
import { Account } from '../models/account.interface';
import { Transaction } from '../models/transaction.interface';
import { Balance } from '../models/balance.interface';
import { HttpClient, provideHttpClient } from '@angular/common/http';

describe('ApiService', () => {
  let service: ApiService;
  let httpTestingController: HttpTestingController;
  const apiUrl = environment.apiBaseUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [ApiService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify(); // Verify that no outstanding requests are unmatched.
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAccounts', () => {
    it('should return a list of accounts', () => {
      const mockAccounts: Account[] = [
        { accountId: '1', displayName: 'Account 1', accountType: 'Current', currency: 'GBP', accountNumber: '123', sortCode: '123', provider: 'Provider 1' },
        { accountId: '2', displayName: 'Account 2', accountType: 'Savings', currency: 'GBP', accountNumber: '456', sortCode: '456', provider: 'Provider 2' }
      ];

      service.getAccounts().subscribe(accounts => {
        expect(accounts).toEqual(mockAccounts);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/accounts`);
      expect(req.request.method).toBe('GET');
      req.flush(mockAccounts);
    });

    it('should handle error when fetching accounts', () => {
      const errorMessage = 'Error fetching accounts';

      service.getAccounts().subscribe({
        next: () => fail('should have failed with the error'),
        error: (error) => {
          expect(error.status).toBe(500);
          expect(error.statusText).toBe('Internal Server Error');
        }
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/accounts`);
      expect(req.request.method).toBe('GET');
      req.error(new ErrorEvent('Internal Server Error'), { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getTransactions', () => {
    it('should return a list of transactions for no account IDs', () => {
      const mockTransactions: Transaction[] = [
        { transaction_id: 't1', timestamp: new Date(), description: 'Desc 1', amount: 10, currency: 'GBP', transaction_type: 'DEBIT', transaction_category: 'Bills', merchant_name: 'Merchant 1' },
        { transaction_id: 't2', timestamp: new Date(), description: 'Desc 2', amount: 20, currency: 'GBP', transaction_type: 'CREDIT', transaction_category: 'Food', merchant_name: 'Merchant 2' }
      ];

      service.getTransactions().subscribe(transactions => {
        expect(transactions).toEqual(mockTransactions);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/transactions?limit=100`);
      expect(req.request.method).toBe('GET');
      req.flush(mockTransactions);
    });

    it('should return a list of transactions for a single account ID', () => {
      const mockTransactions: Transaction[] = [
        { transaction_id: 't1', timestamp: new Date(), description: 'Desc 1', amount: 10, currency: 'GBP', transaction_type: 'DEBIT', transaction_category: 'Bills', merchant_name: 'Merchant 1' }
      ];
      const accountIds = ['acc1'];

      service.getTransactions(accountIds).subscribe(transactions => {
        expect(transactions).toEqual(mockTransactions);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/transactions?limit=100&account_id=acc1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockTransactions);
    });

    it('should return a list of transactions for multiple account IDs', () => {
      const mockTransactions: Transaction[] = [
        { transaction_id: 't1', timestamp: new Date(), description: 'Desc 1', amount: 10, currency: 'GBP', transaction_type: 'DEBIT', transaction_category: 'Bills', merchant_name: 'Merchant 1' },
        { transaction_id: 't2', timestamp: new Date(), description: 'Desc 2', amount: 20, currency: 'GBP', transaction_type: 'CREDIT', transaction_category: 'Food', merchant_name: 'Merchant 2' }
      ];
      const accountIds = ['acc1', 'acc2'];

      service.getTransactions(accountIds).subscribe(transactions => {
        expect(transactions).toEqual(mockTransactions);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/transactions?limit=100&account_id=acc1,acc2`);
      expect(req.request.method).toBe('GET');
      req.flush(mockTransactions);
    });

    it('should handle error when fetching transactions', () => {
      const errorMessage = 'Error fetching transactions';

      service.getTransactions().subscribe({
        next: () => fail('should have failed with the error'),
        error: (error) => {
          expect(error.status).toBe(500);
          expect(error.statusText).toBe('Internal Server Error');
        }
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/transactions?limit=100`);
      expect(req.request.method).toBe('GET');
      req.error(new ErrorEvent('Internal Server Error'), { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getBalances', () => {
    it('should return a list of balances', () => {
      const mockBalances: Balance[] = [
        { accountId: 'b1', accountName: 'Account 1', currency: 'GBP', current: 100, available: 90, overdraft: 0, lastUpdate: new Date() },
        { accountId: 'b2', accountName: 'Account 2', currency: 'GBP', current: 200, available: 180, overdraft: 0, lastUpdate: new Date() }
      ];

      service.getBalances().subscribe(balances => {
        expect(balances).toEqual(mockBalances);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/balances`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBalances);
    });

    it('should handle error when fetching balances', () => {
      const errorMessage = 'Error fetching balances';

      service.getBalances().subscribe({
        next: () => fail('should have failed with the error'),
        error: (error) => {
          expect(error.status).toBe(500);
          expect(error.statusText).toBe('Internal Server Error');
        }
      });

      const req = httpTestingController.expectOne(`${apiUrl}/expenses/balances`);
      expect(req.request.method).toBe('GET');
      req.error(new ErrorEvent('Internal Server Error'), { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
