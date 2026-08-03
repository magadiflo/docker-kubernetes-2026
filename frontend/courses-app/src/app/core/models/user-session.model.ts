export interface UserSession {
  authenticated: boolean;
  username?: string;
  roles?: string[];
}
