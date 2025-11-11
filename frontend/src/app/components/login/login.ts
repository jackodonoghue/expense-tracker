import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent implements OnInit {
  isLoading = false;
  errorMessage = '';

  private authService: AuthService = inject(AuthService);
  private router: Router = inject(Router);

  ngOnInit(): void {
    // Check for error in URL params
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    if (error) {
      this.errorMessage = decodeURIComponent(error);
    }

    // Check if already authenticated
    
  }

  login(): void {
    // This will trigger the OAuth2 flow via the Spring server
    window.location.href = '/oauth2/authorization/truelayer';;
  }
}