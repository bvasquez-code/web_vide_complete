import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PublicPreferenceService {
  private readonly thumbnailStorageKey = 'public.showThumbnails';
  private readonly showThumbnailsSubject = new BehaviorSubject<boolean>(this.readShowThumbnails());
  showThumbnails$ = this.showThumbnailsSubject.asObservable();

  showThumbnails(): boolean {
    return this.showThumbnailsSubject.value;
  }

  toggleThumbnails(): void {
    this.setShowThumbnails(!this.showThumbnails());
  }

  setShowThumbnails(value: boolean): void {
    localStorage.setItem(this.thumbnailStorageKey, value ? 'true' : 'false');
    this.showThumbnailsSubject.next(value);
  }

  private readShowThumbnails(): boolean {
    const value = localStorage.getItem(this.thumbnailStorageKey);
    return value === null ? true : value === 'true';
  }
}
