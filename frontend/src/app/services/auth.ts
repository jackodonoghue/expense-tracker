import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthResponse } from '../models/auth-response.interface';
import { User } from '../models/user.interface';

@Injectable({
	providedIn: 'root'
})
export class AuthService {
	private readonly apiUrl = environment.apiBaseUrl;

	private readonly http = inject(HttpClient);
	private readonly router = inject(Router);

	getUserInfo(): Observable<User> {
		return this.http.get<User>(`${this.apiUrl}/auth/info`).pipe(
			catchError((error) => {
				this.router.navigate(['/login'], { queryParams: { error: 'Issue authenticating user' }, replaceUrl: true });
				return throwError(() => error);
			}));
	}

	logout(): Observable<AuthResponse> {
		return this.http.post<AuthResponse>(`${this.apiUrl}/logout`, {}).pipe(
			tap(() => {

				this.router.navigate(['/login'], { queryParams: { logout: '' }, replaceUrl: true });
			}),
			catchError(error => {
				console.error('Logout failed:', error);
				return throwError(() => error);
			})
		);
	}
}