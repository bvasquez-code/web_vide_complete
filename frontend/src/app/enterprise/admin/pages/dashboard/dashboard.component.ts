import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
  constructor(private router: Router) {}

  logout(): void {
    sessionStorage.clear();
    this.router.navigate(['/admin/login']);
  }
}
