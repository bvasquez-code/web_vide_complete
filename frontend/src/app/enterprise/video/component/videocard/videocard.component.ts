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
}
