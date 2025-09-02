import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthResponse } from '../models/auth-response.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = environment.apiBaseUrl;
  private sessionIdSubject = new BehaviorSubject<string | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);

  public sessionId$ = this.sessionIdSubject.asObservable();
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadSessionFromStorage();
  }

  /**
   * Get current session ID
   */
  get sessionId(): string | null {
    return this.sessionIdSubject.value;
  }

  /**
   * Check if user is authenticated
   */
  get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  /**
   * Load session ID from localStorage
   */
  private loadSessionFromStorage(): void {
    const sessionId = localStorage.getItem('truelayer_session_id');
    if (sessionId) {
      this.sessionIdSubject.next(sessionId);
      this.checkAuthStatus(sessionId).subscribe();
    }
  }

  /**
   * Save session ID to localStorage
   */
  private saveSessionToStorage(sessionId: string): void {
    localStorage.setItem('truelayer_session_id', sessionId);
    this.sessionIdSubject.next(sessionId);
  }

  /**
   * Remove session from localStorage
   */
  private removeSessionFromStorage(): void {
    localStorage.removeItem('truelayer_session_id');
    this.sessionIdSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Check authentication status
   */
  checkAuthStatus(sessionId?: string): Observable<AuthResponse> {
    const currentSessionId = sessionId || this.sessionId;
    if (!currentSessionId) {
      this.isAuthenticatedSubject.next(false);
      return throwError(() => new Error('No session ID'));
    }

    const params = new HttpParams().set('sessionId', currentSessionId);

    return this.http.get<AuthResponse>(`${this.apiUrl}/auth/status`, { params })
      .pipe(
        tap(response => {
          this.isAuthenticatedSubject.next(response.authenticated);
          if (!response.authenticated) {
            this.removeSessionFromStorage();
          }
        }),
        catchError(error => {
          console.error('Auth status check failed:', error);
          this.isAuthenticatedSubject.next(false);
          this.removeSessionFromStorage();
          return throwError(() => error);
        })
      );
  }

  /**
   * Initiate login flow
   */
  login(): Observable<AuthResponse> {
    const sessionId = this.generateSessionId();
    const params = new HttpParams().set('sessionId', sessionId);

    return this.http.get<AuthResponse>(`${this.apiUrl}/auth/login`, { params })
      .pipe(
        tap(response => {
          if (response.authUrl) {
            this.saveSessionToStorage(sessionId);
            // Redirect to TrueLayer authentication
            window.location.href = response.authUrl;
          }
        }),
        catchError(error => {
          console.error('Login initiation failed:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Logout user
   */
  logout(): Observable<AuthResponse> {
    const currentSessionId = this.sessionId;
    if (!currentSessionId) {
      this.removeSessionFromStorage();
      return throwError(() => new Error('No session to logout'));
    }

    const params = new HttpParams().set('sessionId', currentSessionId);

    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/logout`, {}, { params })
      .pipe(
        tap(() => {
          this.removeSessionFromStorage();
        }),
        catchError(error => {
          console.error('Logout failed:', error);
          // Still remove session locally even if server call fails
          this.removeSessionFromStorage();
          return throwError(() => error);
        })
      );
  }

  /**
   * Set session ID from OAuth callback
   */
  setSessionId(sessionId: string): void {
    this.saveSessionToStorage(sessionId);
  }

  /**
   * Generate a new session ID
   */
  private generateSessionId(): string {
    return 'sess_' + Math.random().toString(36).substr(2, 9) + Date.now().toString(36);
  }
}