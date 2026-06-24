import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PublicAuthService } from '../../service/PublicAuthService';

@Component({
  selector: 'app-publiclogin',
  templateUrl: './publiclogin.component.html'
})
export class PublicloginComponent {
  mode: 'login' | 'register' = 'login';
  UserName = '';
  Email = '';
  Names = '';
  Password = '';
  errorMessage = '';

  constructor(private publicAuthService: PublicAuthService, private router: Router) {}

  async submit(): Promise<void> {
    this.errorMessage = '';
    const rpt = this.mode === 'login'
      ? await this.publicAuthService.login(this.UserName, this.Password)
      : await this.publicAuthService.register(this.Email, this.UserName, this.Password, this.Names);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.publicAuthService.saveSession(rpt.Data);
    await this.router.navigate(['/']);
  }
}
