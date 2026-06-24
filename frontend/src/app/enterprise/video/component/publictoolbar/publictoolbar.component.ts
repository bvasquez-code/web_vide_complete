import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PublicAuthService } from '../../service/PublicAuthService';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';

@Component({
  selector: 'app-publictoolbar',
  templateUrl: './publictoolbar.component.html'
})
export class PublictoolbarComponent {
  constructor(public publicPreferenceService: PublicPreferenceService, public publicAuthService: PublicAuthService, private router: Router) {}

  logout(): void {
    this.publicAuthService.logout();
    this.router.navigate(['/']);
  }
}
