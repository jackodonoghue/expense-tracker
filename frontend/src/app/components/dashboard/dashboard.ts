import { Component, OnInit, OnDestroy, inject } from '@angular/core';
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
import { User } from '../../models/user.interface';

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
  displayedColumns: string[] = ['timestamp', 'description', 'transaction_category', 'amount'];
  user?: User;

  private destroy$ = new Subject<void>();
  
  private authService: AuthService = inject(AuthService);
  private apiService: ApiService = inject(ApiService);
  private router: Router = inject(Router);
  
  ngOnInit(): void {
    this.authService.getUserInfo().subscribe((user: User) => {this.user = user});
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Load all data in parallel
    forkJoin({
      accounts: this.apiService.getAccounts(),
      // balances: this.apiService.getBalances(),
      transactions: this.apiService.getTransactions()
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        this.accounts = data.accounts;
        // this.balances = data.balances;
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
    this.authService.logout().subscribe();
  }
}