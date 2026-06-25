import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthStorageService {
  getItem(key: string): string | null {
    const localValue = localStorage.getItem(key);
    if (localValue) {
      return localValue;
    }

    const sessionValue = sessionStorage.getItem(key);
    if (sessionValue) {
      localStorage.setItem(key, sessionValue);
      return sessionValue;
    }

    return null;
  }

  setItem(key: string, value: any): void {
    if (value === undefined || value === null) {
      this.removeItem(key);
      return;
    }

    const textValue = `${value}`;
    localStorage.setItem(key, textValue);
    sessionStorage.setItem(key, textValue);
  }

  removeItem(key: string): void {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  }

  removeItems(keys: string[]): void {
    keys.forEach(key => this.removeItem(key));
  }
}
