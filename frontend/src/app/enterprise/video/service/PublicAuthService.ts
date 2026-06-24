import { Injectable } from '@angular/core';
import { AppSetting } from '../../../config/AppSetting';
import { ApiService } from '../../shared/service/ApiService';
import { ResponseWsDto } from '../../shared/model/dto/ResponseWsDto';

@Injectable({ providedIn: 'root' })
export class PublicAuthService {
  constructor(private apiService: ApiService) {}

  isLogged(): boolean {
    return !!sessionStorage.getItem('ViewerToken');
  }

  viewerName(): string {
    return sessionStorage.getItem('ViewerName') || sessionStorage.getItem('ViewerUserName') || 'Usuario';
  }

  async login(UserName: string, Password: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/auth/login`, { UserName, Password });
  }

  async register(Email: string, UserName: string, Password: string, Names: string): Promise<ResponseWsDto> {
    return await this.apiService.ExecutePostService(`${AppSetting.API}/api/v1/public/auth/register`, { Email, UserName, Password, Names });
  }

  saveSession(data: any): void {
    sessionStorage.setItem('ViewerToken', data.Token);
    sessionStorage.setItem('ViewerUserCod', data.UserCod);
    sessionStorage.setItem('ViewerUserName', data.UserName);
    sessionStorage.setItem('ViewerName', data.Names || data.UserName);
  }

  logout(): void {
    sessionStorage.removeItem('ViewerToken');
    sessionStorage.removeItem('ViewerUserCod');
    sessionStorage.removeItem('ViewerUserName');
    sessionStorage.removeItem('ViewerName');
  }
}
