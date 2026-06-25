import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthStorageService } from '../service/AuthStorageService';

@Injectable({ providedIn: 'root' })
export class AdminAuthGuard implements CanActivate {
  constructor(private router: Router, private authStorageService: AuthStorageService) {}

  canActivate(): boolean {
    if (this.authStorageService.getItem('AdminToken')) {
      return true;
    }
    this.router.navigate(['/admin/login']);
    return false;
  }
}
