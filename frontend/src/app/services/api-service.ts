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
  getAccounts(sessionId: string): Observable<Account[]> {
    const params = new HttpParams().set('sessionId', sessionId);

    return this.http.get<Account[]>(`${this.apiUrl}/expenses/accounts`, { params })
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
  getTransactions(sessionId: string, accountId?: string, limit: number = 100): Observable<Transaction[]> {
    let params = new HttpParams()
      .set('sessionId', sessionId)
      .set('limit', limit.toString());

    if (accountId) {
      params = params.set('accountId', accountId);
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
  getBalances(sessionId: string): Observable<Balance[]> {
    const params = new HttpParams().set('sessionId', sessionId);

    return this.http.get<Balance[]>(`${this.apiUrl}/expenses/balances`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to fetch balances:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Get user information
   */
  getUserInfo(sessionId: string): Observable<any> {
    const params = new HttpParams().set('sessionId', sessionId);

    return this.http.get<any>(`${this.apiUrl}/expenses/info`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to fetch user info:', error);
          return throwError(() => error);
        })
      );
  }
}