import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth';
import { ApiService } from '../../services/api-service';
import { Account } from '../../models/account.interface';
import { Transaction } from '../../models/transaction.interface';
import { Balance } from '../../models/balance.interface';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    MatButtonModule, 
    MatCardModule, 
    MatTableModule, 
    MatProgressSpinnerModule,
    MatIconModule,
    MatTabsModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class Dashboard implements OnInit, OnDestroy {
  accounts: Account[] = [];
  transactions: Transaction[] = [];
  balances: Balance[] = [];
  isLoading = false;
  errorMessage = '';
  displayedColumns: string[] = ['timestamp', 'description', 'category', 'amount'];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Verify authentication
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuth => {
        if (!isAuth) {
          this.router.navigate(['/login']);
          return;
        }
      });

    // Load initial data
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDashboardData(): void {
    const sessionId = this.authService.sessionId;
    if (!sessionId) {
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Load all data in parallel
    forkJoin({
      accounts: this.apiService.getAccounts(sessionId),
      balances: this.apiService.getBalances(sessionId),
      transactions: this.apiService.getTransactions(sessionId)
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        this.accounts = data.accounts;
        this.balances = data.balances;
        this.transactions = data.transactions;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load dashboard data:', error);
        this.errorMessage = 'Failed to load banking data. Please try refreshing.';
        this.isLoading = false;

        if (error.status === 401) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  refreshData(): void {
    this.loadDashboardData();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Logout error:', error);
        // Navigate to login anyway
        this.router.navigate(['/login']);
      }
    });
  }
}