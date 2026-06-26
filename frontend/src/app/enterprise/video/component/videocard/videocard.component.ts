import { Component, Input } from '@angular/core';
import { VideoCardDto } from '../../model/dto/VideoCardDto';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';

@Component({
  selector: 'app-videocard',
  templateUrl: './videocard.component.html'
})
export class VideocardComponent {
  @Input() video: VideoCardDto = new VideoCardDto();
  @Input() titleClass = 'h5';

  constructor(public publicPreferenceService: PublicPreferenceService) {}

  thumb(): string {
    return this.video.ThumbnailUrl || 'assets/default-video.svg';
  }

  resolutionLabel(): string {
    const resolution = this.readResolution();
    if (!resolution) {
      return '';
    }
    const width = resolution.width;
    const height = resolution.height;
    if (width >= 3840 || height >= 2160) return '4K';
    if (width >= 2560 || height >= 1440) return '2K';
    if (width >= 1920 || height >= 1080) return 'Full HD';
    if (width >= 1280 || height >= 720) return 'HD';
    return 'SD';
  }

  private readResolution(): { width: number; height: number } | null {
    if (this.video.ResolutionWidth && this.video.ResolutionHeight) {
      return { width: this.video.ResolutionWidth, height: this.video.ResolutionHeight };
    }
    const match = (this.video.ResolutionLabel || '').match(/(\d+)\s*x\s*(\d+)/i);
    if (match) {
      return { width: Number(match[1]), height: Number(match[2]) };
    }
    return null;
  }
}
