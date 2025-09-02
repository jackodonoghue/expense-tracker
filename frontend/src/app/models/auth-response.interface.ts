export interface AuthResponse {
  authUrl?: string;
  sessionId?: string;
  authenticated: boolean;
  message?: string;
}