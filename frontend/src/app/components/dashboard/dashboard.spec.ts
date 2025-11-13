import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Dashboard } from './dashboard';
import { AuthService } from '../../services/auth';
import { ApiService } from '../../services/api-service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Account } from '../../models/account.interface';
import { Transaction } from '../../models/transaction.interface';

describe('DashboardComponent', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserInfo', 'logout']);
    apiServiceSpy = jasmine.createSpyObj('ApiService', ['getAccounts', 'getTransactions']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ApiService, useValue: apiServiceSpy },
        { provide: Router, useValue: routerSpy },
        provideHttpClientTesting()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;

    authServiceSpy.getUserInfo.and.returnValue(of({ fullName: 'Test User' }));
    apiServiceSpy.getAccounts.and.returnValue(of([])); // Default empty accounts
    apiServiceSpy.getTransactions.and.returnValue(of([])); // Default empty transactions
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user info and accounts on ngOnInit', () => {
    const mockAccounts: Account[] = [{ accountId: '1', displayName: 'Account 1', accountType: 'Current', currency: 'GBP', accountNumber: '123', sortCode: '123', provider: 'Provider 1' }];
    apiServiceSpy.getAccounts.and.returnValue(of(mockAccounts));

    fixture.detectChanges(); // Calls ngOnInit

    expect(authServiceSpy.getUserInfo).toHaveBeenCalled();
    expect(apiServiceSpy.getAccounts).toHaveBeenCalled();
    expect(component.accounts).toEqual(mockAccounts);
    expect(component.isLoading).toBeFalse();
  });

  it('should fetch transactions for selected accounts', () => {
    const mockAccounts: Account[] = [{ accountId: '1', displayName: 'Account 1', accountType: 'Current', currency: 'GBP', accountNumber: '123', sortCode: '123', provider: 'Provider 1' }];
    const mockTransactions: Transaction[] = [{ transaction_id: 't1', timestamp: new Date(), description: 'Desc 1', amount: 10, currency: 'GBP', transaction_type: 'DEBIT', transaction_category: 'Bills', merchant_name: 'Merchant 1' }];

    apiServiceSpy.getAccounts.and.returnValue(of(mockAccounts));
    fixture.detectChanges(); // Calls ngOnInit

    component.accountsControl.setValue(['1']);
    apiServiceSpy.getTransactions.and.returnValue(of(mockTransactions));
    component.getTransactions();

    expect(apiServiceSpy.getTransactions).toHaveBeenCalledWith(['1']);
    expect(component.transactions).toEqual(mockTransactions);
    expect(component.isLoading).toBeFalse();
  });

  it('should not fetch transactions if no accounts are selected', () => {
    component.accountsControl.setValue([]);
    component.getTransactions();
    expect(apiServiceSpy.getTransactions).not.toHaveBeenCalled();
  });

  it('should refresh data by loading accounts and clearing transactions', () => {
    component.transactions = [{ transaction_id: 't1', timestamp: new Date(), description: 'Desc 1', amount: 10, currency: 'GBP', transaction_type: 'DEBIT', transaction_category: 'Bills', merchant_name: 'Merchant 1' }];
    component.accountsControl.setValue(['1']);

    component.refreshData();

    expect(apiServiceSpy.getAccounts).toHaveBeenCalled();
    expect(component.transactions).toEqual([]);
    expect(component.accountsControl.value).toEqual(null); // reset sets value to null
  });

  it('should logout the user', () => {
    authServiceSpy.logout.and.returnValue(of({ authenticated: false }));
    component.logout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
  });

  it('should handle API error and set error message', () => {
    apiServiceSpy.getAccounts.and.returnValue(throwError(() => ({ status: 500 })));
    fixture.detectChanges(); // Calls ngOnInit, which calls loadAccounts

    expect(component.errorMessage).toBe('Failed to load banking data. Please try again.');
    expect(component.isLoading).toBeFalse();
  });

  it('should navigate to login on 401 API error', () => {
    apiServiceSpy.getAccounts.and.returnValue(throwError(() => ({ status: 401 })));
    fixture.detectChanges(); // Calls ngOnInit, which calls loadAccounts

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
