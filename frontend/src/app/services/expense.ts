
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {inject} from '@angular/core';

export interface Expense {
  id?: number;
  description: string;
  amount: number;
}

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  // private apiUrl = 'https://redesigned-dollop-wrqxx95jxxj39vg-8080.app.github.dev/expenses';
  apiUrl = '';
  private http = inject(HttpClient);
  
  constructor() {}

  getExpenses(): Observable<Expense[]> {
    return this.http.get<Expense[]>(this.apiUrl);
  }

  addExpense(expense: Expense): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, expense);
  }
}
