import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth';
import { environment } from '../../environments/environment';
import { User } from '../models/user.interface';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;
  let router: Router;
  const apiUrl = environment.apiBaseUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUserInfo', () => {
    it('should return user info on success', () => {
      const mockUser: User = { fullName: 'John Doe' };

      service.getUserInfo().subscribe(user => {
        expect(user).toEqual(mockUser);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/auth/info`);
      expect(req.request.method).toBe('GET');
      req.flush(mockUser);
    });

    it('should navigate to login on error', () => {
      spyOn(router, 'navigate');

      service.getUserInfo().subscribe({
        next: () => fail('should have failed with the error'),
        error: (error) => {
          expect(error.status).toBe(401);
          expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { error: 'Issue authenticating user' }, replaceUrl: true });
        }
      });

      const req = httpTestingController.expectOne(`${apiUrl}/auth/info`);
      expect(req.request.method).toBe('GET');
      req.error(new ErrorEvent('Unauthorized'), { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('logout', () => {
    it('should navigate to login on successful logout', () => {
      spyOn(router, 'navigate');

      service.logout().subscribe(() => {
        expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { logout: '' }, replaceUrl: true });
      });

      const req = httpTestingController.expectOne(`${apiUrl}/logout`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });

    it('should handle error during logout', () => {
      spyOn(router, 'navigate');

      service.logout().subscribe({
        next: () => fail('should have failed with the error'),
        error: (error) => {
          expect(error.status).toBe(500);
          expect(router.navigate).not.toHaveBeenCalled(); // Should not navigate on error
        }
      });

      const req = httpTestingController.expectOne(`${apiUrl}/logout`);
      expect(req.request.method).toBe('POST');
      req.error(new ErrorEvent('Internal Server Error'), { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
