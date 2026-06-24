import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ResponseWsDto } from '../model/dto/ResponseWsDto';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  async ExecuteGetService(url: string, params: any = {}): Promise<ResponseWsDto> {
    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null) {
        httpParams = httpParams.set(key, params[key]);
      }
    });
    return await firstValueFrom(this.http.get<ResponseWsDto>(url, { headers: this.headers(url), params: httpParams }));
  }

  async ExecutePostService(url: string, body: any = {}): Promise<ResponseWsDto> {
    return await firstValueFrom(this.http.post<ResponseWsDto>(url, body, { headers: this.headers(url) }));
  }

  async ExecutePostFormDataService(url: string, body: FormData): Promise<ResponseWsDto> {
    return await firstValueFrom(this.http.post<ResponseWsDto>(url, body, { headers: this.formDataHeaders(url) }));
  }

  private headers(url: string): HttpHeaders {
    const token = this.tokenForUrl(url);
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  private formDataHeaders(url: string): HttpHeaders {
    const token = this.tokenForUrl(url);
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  private tokenForUrl(url: string): string | null {
    if (url.includes('/api/v1/admin')) {
      return sessionStorage.getItem('AdminToken');
    }
    return sessionStorage.getItem('ViewerToken') || sessionStorage.getItem('AdminToken');
  }
}
