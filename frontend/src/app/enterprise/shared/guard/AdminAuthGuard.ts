import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    if (sessionStorage.getItem('Token')) {
      return true;
    }
    this.router.navigate(['/admin/login']);
    return false;
  }
}
