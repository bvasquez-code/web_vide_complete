import { Pipe, PipeTransform } from '@angular/core';
import { AppSetting } from '../../../config/AppSetting';

@Pipe({ name: 'mediaUrl' })
export class MediaUrlPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    const trimmed = value.trim();
    if (!trimmed) {
      return '';
    }
    if (trimmed.startsWith('data:') || trimmed.startsWith('blob:') || trimmed.startsWith('assets/')) {
      return trimmed;
    }

    const publicPath = this.extractPublicPath(trimmed);
    if (publicPath) {
      return `${AppSetting.API}${publicPath}`;
    }
    return trimmed;
  }

  private extractPublicPath(value: string): string {
    const marker = '/api/v1/public/';
    const markerIndex = value.indexOf(marker);
    if (markerIndex >= 0) {
      return value.substring(markerIndex);
    }
    return value.startsWith(marker) ? value : '';
  }
}
