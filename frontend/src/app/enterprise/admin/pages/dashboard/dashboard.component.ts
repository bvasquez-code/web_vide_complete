import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStorageService } from '../../../shared/service/AuthStorageService';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
  constructor(private router: Router, private authStorageService: AuthStorageService) {}

  logout(): void {
    this.authStorageService.removeItems(['AdminToken', 'AdminUserCod', 'AdminUserName']);
    this.router.navigate(['/admin/login']);
  }
}
