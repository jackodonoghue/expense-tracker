import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Account } from '../models/account.interface';
import { Transaction } from '../models/transaction.interface';
import { Balance } from '../models/balance.interface';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly apiUrl = environment.apiBaseUrl;
  private http: HttpClient = inject(HttpClient);

  /**
   * Get user accounts
   */
  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}/expenses/accounts`)
      .pipe(
        catchError(error => {
          console.error('Failed to fetch accounts:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Get transactions
   */
  getTransactions(accountIds?: string[], limit: number = 100): Observable<Transaction[]> {
    let params = new HttpParams()
      .set('limit', limit.toString());

    if (accountIds && accountIds.length > 0) {
      params = params.set('account_id', accountIds.join(','));
    }

    return this.http.get<Transaction[]>(`${this.apiUrl}/expenses/transactions`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to fetch transactions:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Get account balances
   */
  getBalances(): Observable<Balance[]> {
    return this.http.get<Balance[]>(`${this.apiUrl}/expenses/balances`)
      .pipe(
        catchError(error => {
          console.error('Failed to fetch balances:', error);
          return throwError(() => error);
        })
      );
  }
}