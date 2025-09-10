import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { authGuard } from './guards/auth.guard';
import { App } from './app';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: App },
  { path: 'dashboard', component: Dashboard},// canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' }
];