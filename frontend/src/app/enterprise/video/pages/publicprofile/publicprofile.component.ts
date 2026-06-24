import { Component, OnInit } from '@angular/core';
import { PublicSubscriberService } from '../../service/PublicSubscriberService';
import { VideoCardDto } from '../../model/dto/VideoCardDto';

@Component({
  selector: 'app-publicprofile',
  templateUrl: './publicprofile.component.html'
})
export class PublicprofileComponent implements OnInit {
  watchLater: VideoCardDto[] = [];
  liked: VideoCardDto[] = [];
  history: VideoCardDto[] = [];
  errorMessage = '';

  constructor(private subscriberService: PublicSubscriberService) {}

  async ngOnInit(): Promise<void> {
    const watchRpt = await this.subscriberService.watchLaterList();
    if (watchRpt.ErrorStatus) {
      this.errorMessage = watchRpt.Message;
      return;
    }
    this.watchLater = watchRpt.Data || [];
    const likedRpt = await this.subscriberService.liked();
    this.liked = likedRpt.Data || [];
    const historyRpt = await this.subscriberService.history();
    this.history = historyRpt.Data || [];
  }
}
