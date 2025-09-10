import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule, JsonPipe } from '@angular/common';

@Component({
  imports: [JsonPipe, CommonModule],
  selector: 'app-root',
  template: `
    <div style="text-align:center">
      <h1>TrueLayer Expenses</h1>
      <button (click)="connectBank()">Connect to TrueLayer</button>
      <button (click)="fetchExpenses()">Fetch Expenses</button>
      <div *ngIf="expenses">
        <h2>Expenses:</h2>
        <pre>{{ expenses | json }}</pre>
      </div>
      <div *ngIf="error">
        <p style="color: red;">Error: {{ error }}</p>
      </div>
    </div>
  `
})
export class App implements OnInit {
  expenses: any;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    // Initial check for authentication status
    this.fetchExpenses();
  }

  connectBank(): void {
    // This will trigger the OAuth2 flow via the Spring server
    window.location.href = '/oauth2/authorization/truelayer';
  }

  fetchExpenses(): void {
    this.http.get('/expenses/accounts').subscribe({
      next: data => {
        this.expenses = data;
        this.error = null;
      },
      error: err => {
        this.error = 'Not authenticated or unable to fetch expenses. Please connect your bank.';
        this.expenses = null;
        console.error('Error fetching expenses:', err);
      }
    });
  }
}
