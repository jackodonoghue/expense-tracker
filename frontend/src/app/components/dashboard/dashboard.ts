import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
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
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
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
    MatTabsModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard implements OnInit, OnDestroy {
  accounts: Account[] = [];
  transactions: Transaction[] = [];
  balances: Balance[] = [];
  isLoading = false;
  errorMessage = '';
  displayedColumns: string[] = ['timestamp', 'description', 'transaction_category', 'amount'];
  user?: User;
  accountsControl = new FormControl<string[]>([]);

  private destroy$ = new Subject<void>();

  private authService: AuthService = inject(AuthService);
  private apiService: ApiService = inject(ApiService);
  private router: Router = inject(Router);

  ngOnInit(): void {
    this.authService.getUserInfo().subscribe((user: User) => {
      this.user = user;
    });
    this.loadAccounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.apiService
      .getAccounts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.accounts = data;
          this.isLoading = false;
        },
        error: (error) => {
          this.handleApiError(error);
        },
      });
  }

  getTransactions(): void {
    const selectedAccountIds = this.accountsControl.value;
    if (!selectedAccountIds || selectedAccountIds.length === 0) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.apiService
      .getTransactions(selectedAccountIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.transactions = data;
          this.isLoading = false;
        },
        error: (error) => {
          this.handleApiError(error);
        },
      });
  }

  private handleApiError(error: any): void {
    console.error('API Error:', error);
    this.errorMessage = 'Failed to load banking data. Please try again.';
    this.isLoading = false;

    if (error.status === 401) {
      this.router.navigate(['/login']);
    }
  }

  refreshData(): void {
    this.loadAccounts();
    this.transactions = [];
    this.accountsControl.reset();
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}